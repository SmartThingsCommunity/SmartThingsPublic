/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.work;

import com.google.common.collect.Lists;
import org.gradle.api.Transformer;
import org.gradle.internal.MutableReference;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.internal.concurrent.ManagedExecutor;
import org.gradle.internal.resources.ResourceLockCoordinationService;
import org.gradle.internal.resources.ResourceLockState;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.gradle.internal.resources.DefaultResourceLockCoordinationService.unlock;

/**
 * A queueing mechanism that only executes items once certain conditions are reached.
 */
// TODO This class, DefaultBuildOperationQueue and ExecutionPlan have many of the same
// behavior and concerns - we should look for a way to generalize this pattern.
public class DefaultConditionalExecutionQueue<T> implements ConditionalExecutionQueue<T> {
    public static final int KEEP_ALIVE_TIME_MS = 2000;
    private enum QueueState {
        Working, Stopped
    }

    private final int maxWorkers;
    private final ResourceLockCoordinationService coordinationService;
    private final ManagedExecutor executor;
    private final Deque<ConditionalExecution<T>> queue = Lists.newLinkedList();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition workAvailable = lock.newCondition();
    private QueueState queueState = QueueState.Working;
    private volatile int workerCount;

    public DefaultConditionalExecutionQueue(String displayName, int maxWorkers, ExecutorFactory executorFactory, ResourceLockCoordinationService coordinationService) {
        this.maxWorkers = maxWorkers;
        this.executor = executorFactory.create(displayName);
        this.coordinationService = coordinationService;

        executor.setKeepAlive(KEEP_ALIVE_TIME_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void submit(ConditionalExecution<T> execution) {
        if (queueState == QueueState.Stopped) {
            throw new IllegalStateException("DefaultConditionalExecutionQueue cannot be reused once it has been stopped.");
        }

        lock.lock();
        try {
            // expand the thread pool until we hit max workers
            if (workerCount < maxWorkers) {
                expand(true);
            }

            queue.add(execution);
            workAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void expand() {
        expand(false);
    }

    /**
     * Expanding the thread pool is necessary when work items submit other work.  We want to avoid a starvation scenario where
     * the thread pool is full of work items that are waiting on other queued work items.  The queued work items cannot execute
     * because the thread pool is already full with their parent work items.  We use expand() to allow the thread pool to temporarily
     * expand when work items have to wait on other work.  The thread pool will shrink below max workers again once the queue is
     * drained.
     */
    private void expand(boolean force) {
        lock.lock();
        try {
            // Only expand the thread pool if there is work in the queue or we know that work is about to be submitted (i.e. force == true)
            if (force || !queue.isEmpty()) {
                executor.submit(new ExecutionRunner());
                workerCount++;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() {
        lock.lock();
        try {
            queueState = QueueState.Stopped;
            workAvailable.signalAll();
        } finally {
            lock.unlock();
        }
        executor.stop();
    }

    /**
     * ExecutionRunners process items from the queue until there are no items left, at which point it will either wait for
     * new items to arrive (if there are < max workers threads running) or exit, finishing the thread.
     */
    private class ExecutionRunner implements Runnable {
        @Override
        public void run() {
            try {
                ConditionalExecution operation;
                while ((operation = waitForNextOperation()) != null) {
                    runBatch(operation);
                }
            } finally {
                shutDown();
            }
        }

        private ConditionalExecution waitForNextOperation() {
            lock.lock();
            try {
                // Wait for work to be submitted if the queue is empty and our worker count is under max workers
                // This attempts to keep up to max workers threads alive once they've been started.
                while (queueState == QueueState.Working && queue.isEmpty() && (workerCount <= maxWorkers)) {
                    try {
                        workAvailable.await();
                    } catch (InterruptedException e) {
                        throw UncheckedException.throwAsUncheckedException(e);
                    }
                }

            } finally {
                lock.unlock();
            }

            return getReadyExecution();
        }

        /**
         * Run executions until there are none ready to be executed.
         */
        private void runBatch(final ConditionalExecution firstOperation) {
            ConditionalExecution operation = firstOperation;
            while (operation != null) {
                runExecution(operation);
                operation = getReadyExecution();
            }
        }

        /**
         * Gets the next ConditionalExecution object that is ready to be executed.  It does this by
         * attempting to acquire the associated resource lock of each execution.  If successful, the
         * execution is removed from the queue and returned.  If unsuccessful, it continues to iterate
         * the queue looking for an execution that is ready to execute.
         */
        private ConditionalExecution getReadyExecution() {
            final MutableReference<ConditionalExecution> execution = MutableReference.empty();
            coordinationService.withStateLock(new Transformer<ResourceLockState.Disposition, ResourceLockState>() {
                @Override
                public ResourceLockState.Disposition transform(ResourceLockState resourceLockState) {
                    if (queue.isEmpty()) {
                        return ResourceLockState.Disposition.FINISHED;
                    }

                    lock.lock();
                    try {
                        Iterator<ConditionalExecution<T>> itr = queue.iterator();
                        while (itr.hasNext()) {
                            ConditionalExecution next = itr.next();
                            if (next.getResourceLock().tryLock()) {
                                execution.set(next);
                                itr.remove();
                                break;
                            }
                        }
                    } finally {
                        lock.unlock();
                    }

                    if (execution.get() == null && !queue.isEmpty()) {
                        return ResourceLockState.Disposition.RETRY;
                    } else {
                        return ResourceLockState.Disposition.FINISHED;
                    }
                }
            });

            return execution.get();
        }

        /**
         * Executes a conditional execution and then releases it's resource lock
         */
        private void runExecution(ConditionalExecution execution) {
            try {
                execution.getExecution().run();
            } finally {
                coordinationService.withStateLock(unlock(execution.getResourceLock()));
                execution.complete();
            }
        }

        private void shutDown() {
            lock.lock();
            try {
                workerCount--;
            } finally {
                lock.unlock();
            }
        }
    }
}
