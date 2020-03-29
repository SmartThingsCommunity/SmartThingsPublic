/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.launcher.daemon.client;

import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.specs.ExplainingSpec;
import org.gradle.api.internal.specs.ExplainingSpecs;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.logging.events.OutputEventListener;
import org.gradle.internal.nativeintegration.ProcessEnvironment;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.launcher.daemon.configuration.DaemonParameters;
import org.gradle.launcher.daemon.context.DaemonContext;

import java.io.InputStream;
import java.util.UUID;

public class SingleUseDaemonClientServices extends DaemonClientServices {
    public SingleUseDaemonClientServices(ServiceRegistry loggingServices, DaemonParameters daemonParameters, InputStream buildStandardInput) {
        super(loggingServices, daemonParameters, buildStandardInput);
    }

    @Override
    protected DaemonClient createDaemonClient(IdGenerator<UUID> idGenerator) {
        ExplainingSpec<DaemonContext> matchNone = ExplainingSpecs.satisfyNone();
        return new SingleUseDaemonClient(
                get(DaemonConnector.class),
                get(OutputEventListener.class),
                matchNone,
                getBuildStandardInput(),
                get(ExecutorFactory.class),
                idGenerator,
                get(DocumentationRegistry.class),
                get(ProcessEnvironment.class));
    }
}
