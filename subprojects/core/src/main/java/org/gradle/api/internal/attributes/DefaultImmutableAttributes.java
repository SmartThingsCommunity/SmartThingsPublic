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

package org.gradle.api.internal.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.internal.Cast;
import org.gradle.internal.isolation.Isolatable;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

final class DefaultImmutableAttributes implements ImmutableAttributes, AttributeValue<Object> {
    private static final Comparator<Attribute<?>> ATTRIBUTE_NAME_COMPARATOR = new Comparator<Attribute<?>>() {
        @Override
        public int compare(Attribute<?> o1, Attribute<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    // Coercion is an expensive process, so we cache the result of coercing to other attribute types.
    // We can afford using a hashmap here because attributes are interned, and their lifetime doesn't
    // exceed a build
    private final Map<Attribute<?>, Object> coercionCache = Maps.newConcurrentMap();

    final Attribute<?> attribute;
    final Isolatable<?> value;
    private final ImmutableMap<Attribute<?>, DefaultImmutableAttributes> hierarchy;
    private final ImmutableMap<String, DefaultImmutableAttributes> hierarchyByName;
    private final int hashCode;

    // Optimize for the single entry case, makes findEntry faster
    private final String singleEntryName;
    private final DefaultImmutableAttributes singleEntryValue;

    DefaultImmutableAttributes() {
        this.attribute = null;
        this.value = null;
        this.hashCode = 0;
        this.hierarchy = ImmutableMap.of();
        this.hierarchyByName = ImmutableMap.of();
        this.singleEntryName = null;
        this.singleEntryValue = null;
    }

    DefaultImmutableAttributes(DefaultImmutableAttributes parent, Attribute<?> key, Isolatable<?> value) {
        this.attribute = key;
        this.value = value;
        Map<Attribute<?>, DefaultImmutableAttributes> hierarchy = Maps.newLinkedHashMap();
        hierarchy.putAll(parent.hierarchy);
        hierarchy.put(attribute, this);
        this.hierarchy = ImmutableMap.copyOf(hierarchy);
        Map<String, DefaultImmutableAttributes> hierarchyByName = Maps.newLinkedHashMap();
        hierarchyByName.putAll(parent.hierarchyByName);
        hierarchyByName.put(attribute.getName(), this);
        this.hierarchyByName = ImmutableMap.copyOf(hierarchyByName);
        int hashCode = parent.hashCode();
        hashCode = 31 * hashCode + attribute.hashCode();
        hashCode = 31 * hashCode + value.hashCode();
        this.hashCode = hashCode;
        if (hierarchyByName.size() == 1) {
            Map.Entry<String, DefaultImmutableAttributes> entry = hierarchyByName.entrySet().iterator().next();
            singleEntryName = entry.getKey();
            singleEntryValue = entry.getValue();
        } else {
            singleEntryName = null;
            singleEntryValue = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultImmutableAttributes that = (DefaultImmutableAttributes) o;

        if (hierarchy.size() != that.hierarchy.size()) {
            return false;
        }

        for (Map.Entry<Attribute<?>, DefaultImmutableAttributes> entry : hierarchy.entrySet()) {
            if (!entry.getValue().value.isolate().equals(that.getAttribute(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public ImmutableSet<Attribute<?>> keySet() {
        return hierarchy.keySet();
    }

    @Override
    public <T> AttributeContainer attribute(Attribute<T> key, T value) {
        throw new UnsupportedOperationException("Mutation of attributes is not allowed");
    }

    @Override
    public <T> T getAttribute(Attribute<T> key) {
        Isolatable<T> isolatable = getIsolatableAttribute(key);
        return isolatable == null ? null : isolatable.isolate();
    }

    protected <T> Isolatable<T> getIsolatableAttribute(Attribute<T> key) {
        DefaultImmutableAttributes attributes = hierarchy.get(key);
        return (Isolatable<T>) (attributes == null ? null : attributes.value);
    }

    /**
     * Locates the entry for the given attribute. Returns a 'missing' value when not present.
     */
    @Override
    public <T> AttributeValue<T> findEntry(Attribute<T> key) {
        DefaultImmutableAttributes attributes = hierarchy.get(key);
        return (AttributeValue<T>) (attributes == null ? MISSING : attributes);
    }

    /**
     * Locates the entry for the attribute with the given name. Returns a 'missing' value when not present.
     */
    @Override
    public AttributeValue<?> findEntry(String key) {
        if (singleEntryName == key) {
            // The identity check is intentional here, do not replace with .equals()
            return singleEntryValue;
        }
        DefaultImmutableAttributes attributes = hierarchyByName.get(key);
        return attributes == null ? MISSING : attributes;
    }

    @Override
    public Object get() {
        return value.isolate();
    }

    private String desugar() {
        // We support desugaring for all non-primitive types supported in GradleModuleMetadataWriter.writeAttributes(), which are:
        // - Named
        // - Enum
        if (Named.class.isAssignableFrom(attribute.getType())) {
            return ((Named) get()).getName();
        }
        if (Enum.class.isAssignableFrom(attribute.getType())) {
            return ((Enum<?>) get()).name();
        }
        return null;
    }

    @Nullable
    private <S> S coerce(Class<S> type) {
        if (value != null) {
            return value.coerce(type);
        }
        return null;
    }

    @Override
    public <S> S coerce(Attribute<S> otherAttribute) {
        S s = Cast.uncheckedCast(coercionCache.get(otherAttribute));
        if (s == null) {
            s = uncachedCoerce(otherAttribute);
            coercionCache.put(otherAttribute, s);
        }
        return s;
    }

    private <S> S uncachedCoerce(Attribute<S> otherAttribute) {
        Class<S> otherAttributeType = otherAttribute.getType();
        // If attribute types are already compatible, go with it. There are two cases covered here:
        // 1) Both attributes are strongly typed and match, usually the case if both are sourced from the local build
        // 2) Both attributes are desugared, usually the case if both are sourced from published metadata
        if (otherAttributeType.isAssignableFrom(attribute.getType())) {
            return (S) get();
        }

        // Attempt to coerce myself into the other attribute's type
        // - I am desugared and the other attribute is strongly typed, usually the case if I am sourced from published metadata and the other from the local build
        S converted = coerce(otherAttributeType);
        if (converted != null) {
            return converted;
        } else if (otherAttributeType.isAssignableFrom(String.class)) {
            // Attempt to desugar myself
            // - I am strongly typed and the other is desugared, usually the case if I am sourced from the local build and the other is sourced from published metadata
            converted = (S) desugar();
            if (converted != null) {
                return converted;
            }
        }
        String foundType = get().getClass().getName();
        if (foundType.equals(otherAttributeType.getName())) {
            foundType += " with a different ClassLoader";
        }
        throw new IllegalArgumentException(String.format("Unexpected type for attribute '%s' provided. Expected a value of type %s but found a value of type %s.", attribute.getName(), otherAttributeType.getName(), foundType));
    }

    @Override
    public boolean isPresent() {
        return attribute != null;
    }

    @Override
    public boolean isEmpty() {
        return attribute == null;
    }

    @Override
    public boolean contains(Attribute<?> key) {
        return hierarchy.containsKey(key);
    }

    @Override
    public ImmutableAttributes asImmutable() {
        return this;
    }

    @Override
    public AttributeContainer getAttributes() {
        return this;
    }

    @Override
    public String toString() {
        Map<Attribute<?>, Object> sorted = new TreeMap<Attribute<?>, Object>(ATTRIBUTE_NAME_COMPARATOR);
        for (Map.Entry<Attribute<?>, DefaultImmutableAttributes> entry : hierarchy.entrySet()) {
            sorted.put(entry.getKey(), entry.getValue().value.isolate());
        }
        return sorted.toString();
    }

}
