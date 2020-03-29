/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.util;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.ObjectUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiffUtil {
    public static <T> void diff(Set<? extends T> newSet, Set<? extends T> oldSet, ChangeListener<T> changeListener) {
        Set<T> added = new HashSet<T>(newSet);
        added.removeAll(oldSet);
        for (T t : added) {
            changeListener.added(t);
        }

        Set<T> removed = new HashSet<T>(oldSet);
        removed.removeAll(newSet);
        for (T t : removed) {
            changeListener.removed(t);
        }
    }

    public static <K, V> void diff(Map<? extends K, ? extends V> newMap, Map<? extends K, ? extends V> oldMap, ChangeListener<? super Map.Entry<K, V>> changeListener) {
        Map<K, V> added = new HashMap<K, V>(newMap);
        added.keySet().removeAll(oldMap.keySet());
        for (Map.Entry<K, V> entry : added.entrySet()) {
            changeListener.added(entry);
        }

        Map<K, V> removed = new HashMap<K, V>(oldMap);
        removed.keySet().removeAll(newMap.keySet());
        for (Map.Entry<K, V> entry : removed.entrySet()) {
            changeListener.removed(entry);
        }

        Map<K, V> same = new HashMap<K, V>(newMap);
        same.keySet().retainAll(oldMap.keySet());
        for (Map.Entry<K, V> entry : same.entrySet()) {
            if (!checkEquality(entry.getValue(), oldMap.get(entry.getKey()))) {
                changeListener.changed(entry);
            }
        }
    }

    @VisibleForTesting
    static boolean checkEquality(Object obj1, Object obj2) {
        return ObjectUtils.equals(obj1, obj2) || checkEnumEquality(obj1, obj2);
    }

    private static boolean checkEnumEquality(Object obj1, Object obj2) {
        if (!(obj1 instanceof Enum) || !(obj2 instanceof Enum)) {
            return false;
        }

        Enum e1 = (Enum) obj1;
        Enum e2 = (Enum) obj2;

        // Check enum equality without checking loading ClassLoader.
        // There is a slight risk that two versions of the same enum class are compared,
        // (that's why classloaders are used in equality checks), but checking both name
        // and ordinal should make this very unlikely.
        return e1.getDeclaringClass().getCanonicalName().equals(e2.getDeclaringClass().getCanonicalName())
            && e1.ordinal() == e2.ordinal()
            && e1.name().equals(e2.name());
    }
}
