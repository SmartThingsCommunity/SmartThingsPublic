/*
 * Copyright 2020 the original author or authors.
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
package org.gradle.integtests.fixtures;

import org.gradle.test.fixtures.dsl.GradleDsl;
import org.junit.runner.notification.RunNotifier;

public class PolyglotDslTestRunner extends AbstractMultiTestRunner {
    private final static String DSL_LANGUAGE = "org.gradle.internal.runner.dsl";

    public PolyglotDslTestRunner(Class<?> target) {
        super(target);
    }

    public static GradleDsl getCurrentDsl() {
        String property = System.getProperty(DSL_LANGUAGE);
        if (property == null) {
            return null;
        }
        return GradleDsl.valueOf(property);
    }

    @Override
    protected void createExecutions() {
        for (GradleDsl dsl : GradleDsl.values()) {
            add(new DslExecution(dsl));
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }

    private static class DslExecution extends AbstractMultiTestRunner.Execution {
        private final GradleDsl dsl;

        private DslExecution(GradleDsl dsl) {
            this.dsl = dsl;
        }

        @Override
        protected String getDisplayName() {
            return dsl.getLanguageCodeName();
        }

        @Override
        protected void before() {
            System.setProperty(DSL_LANGUAGE, dsl.toString());
        }

        @Override
        protected void after() {
            System.clearProperty(DSL_LANGUAGE);
        }
    }
}
