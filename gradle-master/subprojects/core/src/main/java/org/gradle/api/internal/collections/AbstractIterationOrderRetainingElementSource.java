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

package org.gradle.api.internal.collections;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.gradle.api.Action;
import org.gradle.api.internal.DefaultMutationGuard;
import org.gradle.api.internal.MutationGuard;
import org.gradle.api.internal.provider.ChangingValue;
import org.gradle.api.internal.provider.CollectionProviderInternal;
import org.gradle.api.internal.provider.Collector;
import org.gradle.api.internal.provider.Collectors.ElementFromProvider;
import org.gradle.api.internal.provider.Collectors.ElementsFromCollectionProvider;
import org.gradle.api.internal.provider.Collectors.SingleElement;
import org.gradle.api.internal.provider.Collectors.TypedCollector;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.specs.Spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

abstract public class AbstractIterationOrderRetainingElementSource<T> implements ElementSource<T> {
    // This set represents the order in which elements are inserted to the store, either actual
    // or provided.  We construct a correct iteration order from this set.
    private final List<Element<T>> inserted = new ArrayList<Element<T>>();

    private final MutationGuard mutationGuard = new DefaultMutationGuard();

    private Action<T> realizeAction;

    protected int modCount;

    List<Element<T>> getInserted() {
        return inserted;
    }

    @Override
    public boolean isEmpty() {
        return inserted.isEmpty();
    }

    @Override
    public boolean constantTimeIsEmpty() {
        return inserted.isEmpty();
    }

    @Override
    public int size() {
        int count = 0;
        for (Element<T> element : inserted) {
            count += element.size();
        }
        return count;
    }

    @Override
    public int estimatedSize() {
        return size();
    }

    @Override
    public boolean contains(Object element) {
        return Iterators.contains(iterator(), element);
    }

    @Override
    public boolean containsAll(Collection<?> elements) {
        for (Object e : elements) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        Iterator<T> iterator = iteratorNoFlush();
        while (iterator.hasNext()) {
            T value = iterator.next();
            if (value.equals(o)) {
                iterator.remove();
                modCount++;
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        modCount++;
        inserted.clear();
    }

    @Override
    public void realizeExternal(ProviderInternal<? extends T> provider) {

    }

    @Override
    public void realizePending() {
        for (Element<T> element : inserted) {
            if (!element.isRealized()) {
                modCount++;
                element.realize();
            }
        }
    }

    @Override
    public void realizePending(Class<?> type) {
        for (Element<T> element : inserted) {
            if (!element.isRealized() && (element.getType() == null || type.isAssignableFrom(element.getType()))) {
                modCount++;
                element.realize();
            }
        }
    }

    protected void clearCachedElement(Element<T> element) {
        modCount++;
        element.clearCache();
    }

    Element<T> cachingElement(ProviderInternal<? extends T> provider) {
        final Element<T> element = new Element<T>(provider.getType(), new ElementFromProvider<T>(provider), realizeAction);
        if (provider instanceof ChangingValue) {
            ((ChangingValue<T>) provider).onValueChange(new Action<T>() {
                @Override
                public void execute(T previousValue) {
                    clearCachedElement(element);
                }
            });
        }
        return element;
    }

    Element<T> cachingElement(CollectionProviderInternal<T, ? extends Iterable<T>> provider) {
        final Element<T> element = new Element<T>(provider.getElementType(), new ElementsFromCollectionProvider<T>(provider), realizeAction);
        if (provider instanceof ChangingValue) {
            ((ChangingValue<Iterable<T>>) provider).onValueChange(new Action<Iterable<T>>() {
                @Override
                public void execute(Iterable<T> previousValues) {
                    clearCachedElement(element);
                }
            });
        }
        return element;
    }

    @Override
    public boolean removePending(ProviderInternal<? extends T> provider) {
        return removeByProvider(provider);
    }

    private boolean removeByProvider(ProviderInternal<?> provider) {
        Iterator<Element<T>> iterator = inserted.iterator();
        while (iterator.hasNext()) {
            Element<T> next = iterator.next();
            if (!next.isRealized() && next.isProvidedBy(provider)) {
                modCount++;
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removePendingCollection(CollectionProviderInternal<T, ? extends Iterable<T>> provider) {
        return removeByProvider(provider);
    }

    @Override
    public void onRealize(final Action<T> action) {
        this.realizeAction = action;
    }

    @Override
    public MutationGuard getMutationGuard() {
        return mutationGuard;
    }

    protected class RealizedElementCollectionIterator implements Iterator<T> {
        final List<Element<T>> backingList;
        final Spec<ValuePointer<T>> acceptanceSpec;
        int nextIndex = -1;
        int nextSubIndex = -1;
        int previousIndex = -1;
        int previousSubIndex = -1;
        T next;
        int expectedModCount = modCount;

        RealizedElementCollectionIterator(List<Element<T>> backingList, Spec<ValuePointer<T>> acceptanceSpec) {
            this.backingList = backingList;
            this.acceptanceSpec = acceptanceSpec;
            updateNext();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        private void updateNext() {
            if (nextIndex == -1) {
                nextIndex = 0;
            }

            int i = nextIndex;
            while (i < backingList.size()) {
                Element<T> candidate = backingList.get(i);
                if (candidate.isRealized()) {
                    List<T> collected = candidate.getValues();
                    int j = nextSubIndex + 1;
                    while (j < collected.size()) {
                        T value = collected.get(j);
                        if (acceptanceSpec.isSatisfiedBy(new ValuePointer<T>(candidate, j))) {
                            nextIndex = i;
                            nextSubIndex = j;
                            next = value;
                            return;
                        }
                        j++;
                    }
                    nextSubIndex = -1;
                }
                i++;
            }
            nextIndex = i;
            next = null;
        }

        @Override
        public T next() {
            checkForComodification();
            if (next == null) {
                throw new NoSuchElementException();
            }
            T thisNext = next;
            previousIndex = nextIndex;
            previousSubIndex = nextSubIndex;
            updateNext();
            return thisNext;
        }

        @Override
        public void remove() {
            if (previousIndex > -1) {
                checkForComodification();
                Element<T> element = backingList.get(previousIndex);
                List<T> collected = element.getValues();
                if (collected.size() > 1) {
                    element.remove(collected.get(previousSubIndex));
                    nextSubIndex--;
                } else {
                    backingList.remove(previousIndex);
                    nextIndex--;
                }
                previousIndex = -1;
                previousSubIndex = -1;
            } else {
                throw new IllegalStateException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    protected static class Element<T> extends TypedCollector<T> {
        private List<T> cache;
        private final List<T> removedValues = Lists.newArrayList();
        private final Set<T> realizedValues = Sets.newHashSet();
        private final Set<Integer> duplicates = Sets.newHashSet(); // TODO IntSet
        private boolean realized;
        private final Action<T> realizeAction;

        Element(Class<? extends T> type, Collector<T> delegate, Action<T> realizeAction) {
            super(type, delegate);
            this.realizeAction = realizeAction;
        }

        Element(T value) {
            super(null, new SingleElement<T>(value));
            this.realizeAction = null;
            realize();
        }

        public boolean isRealized() {
            return realized;
        }

        public void realize() {
            if (cache == null) {
                ImmutableList.Builder<T> builder = ImmutableList.builderWithExpectedSize(delegate.size());
                super.collectInto(builder);
                cache = new ArrayList<>(builder.build());
                cache.removeAll(removedValues);
                realized = true;
                if (realizeAction != null) {
                    for (T value : cache) {
                        if (!realizedValues.contains(value)) {
                            realizeAction.execute(value);
                            realizedValues.add(value);
                        }
                    }
                }
            }
        }

        @Override
        public void collectInto(ImmutableCollection.Builder<T> builder) {
            if (!realized) {
                realize();
            }
            builder.addAll(cache);
        }

        List<T> getValues() {
            if (!realized) {
                realize();
            }
            return cache;
        }

        public boolean remove(T value) {
            removedValues.add(value);
            if (cache != null) {
                return cache.remove(value);
            }
            return true;
        }

        boolean isDuplicate(int index) {
            return duplicates.contains(index);
        }

        void setDuplicate(int index) {
            duplicates.add(index);
        }

        void clearCache() {
            cache = null;
            realized = false;
            duplicates.clear();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Element that = (Element) o;
            return Objects.equal(delegate, that.delegate) &&
                Objects.equal(cache, that.cache);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(delegate, cache);
        }
    }

    protected static class ValuePointer<T> {
        private final Element<T> element;
        private final Integer index;

        public ValuePointer(Element<T> element, Integer index) {
            this.element = element;
            this.index = index;
        }

        public Element<T> getElement() {
            return element;
        }

        public Integer getIndex() {
            return index;
        }
    }
}
