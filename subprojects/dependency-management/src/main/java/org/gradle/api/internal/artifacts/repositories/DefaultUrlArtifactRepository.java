/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.api.internal.artifacts.repositories;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.repositories.UrlArtifactRepository;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.internal.verifier.HttpRedirectVerifier;
import org.gradle.internal.verifier.HttpRedirectVerifierFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URI;
import java.util.function.Supplier;

public class DefaultUrlArtifactRepository implements UrlArtifactRepository {

    private Object url;
    private boolean allowInsecureProtocol;
    private final String repositoryType;
    private final FileResolver fileResolver;
    private final Supplier<String> displayNameSupplier;

    DefaultUrlArtifactRepository(
        final FileResolver fileResolver,
        final String repositoryType,
        final Supplier<String> displayNameSupplier
    ) {
        this.fileResolver = fileResolver;
        this.repositoryType = repositoryType;
        this.displayNameSupplier = displayNameSupplier;
    }

    @Override
    public URI getUrl() {
        return url == null ? null : fileResolver.resolveUri(url);
    }

    @Override
    public void setUrl(URI url) {
        this.url = url;
    }

    @Override
    public void setUrl(Object url) {
        this.url = url;
    }

    @Override
    public void setAllowInsecureProtocol(boolean allowInsecureProtocol) {
        this.allowInsecureProtocol = allowInsecureProtocol;
    }

    @Override
    public boolean isAllowInsecureProtocol() {
        return allowInsecureProtocol;
    }

    @Nonnull
    public URI validateUrl() {
        URI rootUri = getUrl();
        if (rootUri == null) {
            throw new InvalidUserDataException(String.format(
                "You must specify a URL for a %s repository.",
                repositoryType
            ));
        }
        return rootUri;
    }

    private void nagUserOfInsecureProtocol() {
        DeprecationLogger
            .deprecate("Using insecure protocols with repositories")
            .withAdvice(String.format(
                "Switch %s repository '%s' to a secure protocol (like HTTPS) or allow insecure protocols.",
                repositoryType,
                displayNameSupplier.get()))
            .willBeRemovedInGradle7()
            .withDslReference(UrlArtifactRepository.class, "allowInsecureProtocol")
            .nagUser();
    }

    private void nagUserOfInsecureRedirect(@Nullable URI redirectFrom, URI redirectLocation) {
        String contextualAdvice = null;
        if (redirectFrom != null) {
            contextualAdvice = String.format(
                "'%s' is redirecting to '%s'.",
                redirectFrom,
                redirectLocation
            );
        }
        DeprecationLogger
            .deprecate("Following insecure redirects")
            .withAdvice(String.format(
                "Switch %s repository '%s' to redirect to a secure protocol (like HTTPS) or allow insecure protocols.",
                repositoryType,
                displayNameSupplier.get()))
            .withContext(contextualAdvice)
            .willBeRemovedInGradle7()
            .withDslReference(UrlArtifactRepository.class, "allowInsecureProtocol")
            .nagUser();
    }

    HttpRedirectVerifier createRedirectVerifier() {
        @Nullable
        URI uri = getUrl();
        return HttpRedirectVerifierFactory
            .create(
                uri,
                allowInsecureProtocol,
                this::nagUserOfInsecureProtocol,
                redirection -> nagUserOfInsecureRedirect(uri, redirection)
            );
    }

    public static class Factory {
        private final FileResolver fileResolver;

        @Inject
        public Factory(FileResolver fileResolver) {
            this.fileResolver = fileResolver;
        }

        DefaultUrlArtifactRepository create(String repositoryType, Supplier<String> displayNameSupplier) {
            return new DefaultUrlArtifactRepository(fileResolver, repositoryType, displayNameSupplier);
        }
    }
}
