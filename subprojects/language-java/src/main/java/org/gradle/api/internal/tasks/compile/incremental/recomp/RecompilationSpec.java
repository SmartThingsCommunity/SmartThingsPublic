/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.internal.tasks.compile.incremental.recomp;

import org.gradle.api.internal.tasks.compile.incremental.processing.GeneratedResource;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class RecompilationSpec {
    private final Collection<String> classesToCompile = new NormalizingClassNamesSet();
    private final Collection<String> classesToProcess = new NormalizingClassNamesSet();
    private final Collection<GeneratedResource> resourcesToGenerate = new LinkedHashSet<GeneratedResource>();
    private final Set<String> relativeSourcePathsToCompile = new LinkedHashSet<>();
    private String fullRebuildCause;

    @Override
    public String toString() {
        return "RecompilationSpec{" +
            "classesToCompile=" + classesToCompile +
            ", classesToProcess=" + classesToProcess +
            ", resourcesToGenerate=" + resourcesToGenerate +
            ", relativeSourcePathsToCompile=" + relativeSourcePathsToCompile +
            ", fullRebuildCause='" + fullRebuildCause + '\'' +
            ", buildNeeded=" + isBuildNeeded() +
            ", fullRebuildNeeded=" + isFullRebuildNeeded() +
            '}';
    }

    public Collection<String> getClassesToCompile() {
        return classesToCompile;
    }

    /**
     * @return the relative paths of files we clearly know to recompile
     */
    public Set<String> getRelativeSourcePathsToCompile() {
        return relativeSourcePathsToCompile;
    }

    public Collection<String> getClassesToProcess() {
        return classesToProcess;
    }

    public Collection<GeneratedResource> getResourcesToGenerate() {
        return resourcesToGenerate;
    }

    public boolean isBuildNeeded() {
        return isFullRebuildNeeded() || !classesToCompile.isEmpty() || !classesToProcess.isEmpty() || !relativeSourcePathsToCompile.isEmpty();
    }

    public boolean isFullRebuildNeeded() {
        return fullRebuildCause != null;
    }

    public String getFullRebuildCause() {
        return fullRebuildCause;
    }

    public void setFullRebuildCause(String description, File file) {
        fullRebuildCause = description != null ? description : "'" + file.getName() + "' was changed";
    }

    private static class NormalizingClassNamesSet extends LinkedHashSet<String> {
        @Override
        public boolean add(String className) {
            return super.add(maybeClassName(className));
        }

        @Override
        public boolean addAll(Collection<? extends String> classNames) {
            boolean added = false;
            for (String cn : classNames) {
                added |= add(cn);
            }
            return added;
        }
    }

    /**
     * Given a class name, try to extract the file name portion of it.
     *
     * TODO: This is unreliable since there's not a strong connection between
     * the name of a class and the name of the file that needs recompilation.
     *
     * For some languages, the compiler can provide this.  We should be doing that
     * here instead of guessing.
     */
    static String maybeClassName(String fullyQualifiedName) {
        final String packageName;
        final String className;
        int packageIdx = fullyQualifiedName.lastIndexOf('.');
        if (packageIdx > 0) {
            // com.example.<classname>
            packageName = fullyQualifiedName.substring(0, packageIdx+1);
            className = fullyQualifiedName.substring(packageIdx+1);
        } else {
            // must be in the default package?
            packageName = "";
            className = fullyQualifiedName;
        }

        final String guessClassName;
        int innerClassIdx = className.indexOf('$');
        if (innerClassIdx > 1) {
            // Classes can start with a $
            guessClassName = className.substring(0, innerClassIdx);
        } else {
            guessClassName = className;
        }
        return packageName + guessClassName;
    }
}
