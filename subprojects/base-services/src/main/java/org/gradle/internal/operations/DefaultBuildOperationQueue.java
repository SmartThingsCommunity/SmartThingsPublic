/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.internal.operations;

import org.gradle.internal.Factory;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.work.WorkerLeaseRegistry;
import org.gradle.internal.work.WorkerLeaseService;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class DefaultBuildOperationQueue<T extends BuildOperation> implements BuildOperationQueue<T> {
    private enum QueueState {
        Working, Finishing, Cancelled, Done
    }

    private final WorkerLeaseService workerLeases;
    private final WorkerLeaseRegistry.WorkerLease parentWorkerLease;
    private final Executor executor;
    private final QueueWorker<T> queueWorker;
    private String logLocation;

    // Lock protects the following state, using an intentionally simple locking strategy
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition workAvailable = lock.newCondition();
    private final Condition operationsComplete = lock.newCondition();
    private QueueState queueState = QueueState.Working;
    private int workerCount;
    private int pendingOperations;
    private final Deque<T> workQueue = new LinkedList<T>();
    private final LinkedList<Throwable> failures = new LinkedList<Throwable>();

    DefaultBuildOperationQueue(WorkerLeaseService workerLeases, Executor executor, QueueWorker<T> queueWorker) {
        this.workerLeases = workerLeases;
        this.parentWorkerLease = workerLeases.getWorkerLease();
        this.executor = executor;
        this.queueWorker = queueWorker;
    }

    @Override
    public void add(final T operation) {
        lock.lock();
        try {
            if (queueState == QueueState.Done) {
                throw new IllegalStateException("BuildOperationQueue cannot be reused once it has completed.");
            }
            if (queueState == QueueState.Cancelled) {
                return;
            }
            workQueue.add(operation);
            pendingOperations++;
            workAvailable.signalAll();
            if (workerCount == 0 || workerCount < workerLeases.getMaxWorkerCount() - 1) {
                // `getMaxWorkerCount() - 1` because main thread executes work as well. See https://github.com/gradle/gradle/issues/3273
                // TODO This could be more efficient, so that we only start a worker when there are none idle _and_ there is a worker lease available
                executor.execute(new WorkerRunnable());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cancel() {
        lock.lock();
        try {
            if (queueState == QueueState.Cancelled || queueState == QueueState.Done) {
                return;
            }
            queueState = QueueState.Cancelled;
            completeOperations(workQueue.size());
            workQueue.clear();
            workAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void waitForCompletion() throws MultipleBuildOperationFailures {
        lock.lock();
        try {
            if (queueState == QueueState.Done) {
                throw new IllegalStateException("Cannot wait for completion more than once.");
            }
            queueState = QueueState.Finishing;
            workAvailable.signalAll();
        } finally {
            lock.unlock();
        }

        // Use this thread to process any work - this allows work to be executed using the
        // worker lease acquired by this thread even if the executor thread pool is full of
        // workers from other threads.  In other words, it ensures that all worker leases
        // are being utilized, regardless of the bounds of the thread pool.
        try {
            new WorkerRunnable().run();
        } catch (Throwable t) {
            addFailure(t);
        }

        lock.lock();
        try {
            // Wait for any work still running in other threads
            while (pendingOperations > 0) {
                try {
                    operationsComplete.await();
                } catch (InterruptedException e) {
                    throw UncheckedException.throwAsUncheckedException(e);
                }
            }

            queueState = QueueState.Done;
            if (!failures.isEmpty()) {
                throw new MultipleBuildOperationFailures(getFailureMessage(failures), failures, logLocation);
            }
        } finally {
            lock.unlock();
        }
    }

    private void addFailure(Throwable failure) {
        lock.lock();
        try {
            failures.add(failure);
        } finally {
            lock.unlock();
        }
    }

    private void completeOperations(int count) {
        lock.lock();
        try {
            pendingOperations = pendingOperations - count;
            operationsComplete.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    private static String getFailureMessage(Collection<? extends Throwable> failures) {
        if (failures.size() == 1) {
            return "A build operation failed.";
        }
        return "Multiple build operations failed.";
    }

    private class WorkerRunnable implements Runnable {
        @Override
        public void run() {
            T operation;
            while ((operation = waitForNextOperation()) != null) {
                runBatch(operation);
            }
            shutDown();
        }

        private T waitForNextOperation() {
            lock.lock();
            try {
                while (queueState == QueueState.Working && workQueue.isEmpty()) {
                    try {
                        workAvailable.await();
                    } catch (InterruptedException e) {
                        throw UncheckedException.throwAsUncheckedException(e);
                    }
                }
                return getNextOperation();
            } finally {
                lock.unlock();
            }
        }

        private void runBatch(final T firstOperation) {
            // We need to update pending count outside of withLocks() so that we don't have a race
            // condition where the pending count is 0, but a child worker lease is still held when
            // the parent lease is released.
            completeOperations(
                workerLeases.withLocks(Collections.singleton(parentWorkerLease.createChild()), new Factory<Integer>() {
                    @Override
                    public Integer create() {
                        int operationCount = 0;
                        T operation = firstOperation;
                        while (operation != null) {
                            runOperation(operation);
                            operationCount++;
                            operation = getNextOperation();
                        }
                        return operationCount;
                    }
                })
            );
        }

        private T getNextOperation() {
            lock.lock();
            try {
                return workQueue.pollFirst();
            } finally {
                lock.unlock();
            }
        }

        private void runOperation(T operation) {
            try {
                queueWorker.execute(operation);
            } catch (Throwable t) {
                addFailure(t);
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
