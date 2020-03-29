/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.internal.resources;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.gradle.api.Action;
import org.gradle.internal.UncheckedException;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public abstract class AbstractResourceLockRegistry<K, T extends ResourceLock> implements ResourceLockRegistry {
    private final Cache<K, T> resourceLocks = CacheBuilder.newBuilder().weakValues().build();
    private final Multimap<Long, ResourceLock> threadResourceLockMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.<Long, ResourceLock>create());
    private final ResourceLockCoordinationService coordinationService;

    public AbstractResourceLockRegistry(final ResourceLockCoordinationService coordinationService) {
        this.coordinationService = coordinationService;
    }

    protected T getOrRegisterResourceLock(final K key, final ResourceLockProducer<K, T> producer) {
        try {
            return resourceLocks.get(key, new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return createResourceLock(key, producer);
                }
            });
        } catch (ExecutionException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    protected T createResourceLock(final K key, final ResourceLockProducer<K, T> producer) {
        return producer.create(key, coordinationService, getLockAction(), getUnlockAction());
    }

    @Override
    public Collection<? extends ResourceLock> getResourceLocksByCurrentThread() {
        final Long threadId = Thread.currentThread().getId();
        return ImmutableList.copyOf(threadResourceLockMap.get(threadId));
    }

    @Override
    public boolean hasOpenLocks() {
        for (ResourceLock resourceLock : resourceLocks.asMap().values()) {
            if (resourceLock.isLocked()) {
                return true;
            }
        }
        return false;
    }

    private Action<ResourceLock> getLockAction() {
        return new Action<ResourceLock>() {
            @Override
            public void execute(ResourceLock resourceLock) {
                associateResourceLock(resourceLock);
            }
        };
    }

    private Action<ResourceLock> getUnlockAction() {
        return new Action<ResourceLock>() {
            @Override
            public void execute(ResourceLock resourceLock) {
                unassociateResourceLock(resourceLock);
            }
        };
    }

    public void associateResourceLock(ResourceLock resourceLock) {
        threadResourceLockMap.put(Thread.currentThread().getId(), resourceLock);
    }

    public void unassociateResourceLock(ResourceLock resourceLock) {
        threadResourceLockMap.remove(Thread.currentThread().getId(), resourceLock);
    }

    public interface ResourceLockProducer<K, T extends ResourceLock> {
        T create(K key, ResourceLockCoordinationService coordinationService, Action<ResourceLock> lockAction, Action<ResourceLock> unlockAction);
    }
}
