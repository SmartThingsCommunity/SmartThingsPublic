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
package org.gradle.api.internal.artifacts.verification.verifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.verification.ArtifactVerificationOperation;
import org.gradle.api.internal.artifacts.verification.model.ArtifactVerificationMetadata;
import org.gradle.api.internal.artifacts.verification.model.Checksum;
import org.gradle.api.internal.artifacts.verification.model.ChecksumKind;
import org.gradle.api.internal.artifacts.verification.model.ComponentVerificationMetadata;
import org.gradle.api.internal.artifacts.verification.model.IgnoredKey;
import org.gradle.api.internal.artifacts.verification.signatures.SignatureVerificationResultBuilder;
import org.gradle.api.internal.artifacts.verification.signatures.SignatureVerificationService;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;
import org.gradle.internal.hash.ChecksumService;
import org.gradle.internal.hash.HashCode;
import org.gradle.security.internal.Fingerprint;
import org.gradle.security.internal.PublicKeyService;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyVerifier {
    private final Map<ComponentIdentifier, ComponentVerificationMetadata> verificationMetadata;
    private final DependencyVerificationConfiguration config;

    DependencyVerifier(Map<ComponentIdentifier, ComponentVerificationMetadata> verificationMetadata, DependencyVerificationConfiguration config) {
        this.verificationMetadata = ImmutableMap.copyOf(verificationMetadata);
        this.config = config;
    }

    public void verify(ChecksumService checksumService,
                       SignatureVerificationService signatureVerificationService,
                       ArtifactVerificationOperation.ArtifactKind kind,
                       ModuleComponentArtifactIdentifier foundArtifact,
                       File artifactFile,
                       File signatureFile,
                       ArtifactVerificationResultBuilder builder) {
        if (shouldSkipVerification(kind)) {
            return;
        }
        performVerification(foundArtifact,
            checksumService,
            signatureVerificationService,
            artifactFile,
            signatureFile, failure -> {
                if (isTrustedArtifact(foundArtifact)) {
                    return;
                }
                builder.failWith(failure);
            });
    }

    private boolean shouldSkipVerification(ArtifactVerificationOperation.ArtifactKind kind) {
        if (kind == ArtifactVerificationOperation.ArtifactKind.METADATA && !config.isVerifyMetadata()) {
            return true;
        }
        return false;
    }

    private boolean isTrustedArtifact(ModuleComponentArtifactIdentifier id) {
        if (config.getTrustedArtifacts().stream().anyMatch(artifact -> artifact.matches(id))) {
            return true;
        }
        return false;
    }

    private void performVerification(ModuleComponentArtifactIdentifier foundArtifact, ChecksumService checksumService, SignatureVerificationService signatureVerificationService, File file, File signature, ArtifactVerificationResultBuilder builder) {
        if (!file.exists()) {
            builder.failWith(new DeletedArtifact(file));
            return;
        }
        doVerifyArtifact(foundArtifact, checksumService, signatureVerificationService, file, signature, builder);
    }

    private void doVerifyArtifact(ModuleComponentArtifactIdentifier foundArtifact, ChecksumService checksumService, SignatureVerificationService signatureVerificationService, File file, File signature, ArtifactVerificationResultBuilder builder) {
        PublicKeyService publicKeyService = signatureVerificationService.getPublicKeyService();
        ComponentVerificationMetadata componentVerification = verificationMetadata.get(foundArtifact.getComponentIdentifier());
        if (componentVerification != null) {
            String foundArtifactFileName = foundArtifact.getFileName();
            List<ArtifactVerificationMetadata> verifications = componentVerification.getArtifactVerifications();
            for (ArtifactVerificationMetadata verification : verifications) {
                String verifiedArtifact = verification.getArtifactName();
                if (verifiedArtifact.equals(foundArtifactFileName)) {
                    if (signature == null && config.isVerifySignatures()) {
                        builder.failWith(new MissingSignature(file));
                    }
                    if (signature != null) {
                        DefaultSignatureVerificationResultBuilder result = new DefaultSignatureVerificationResultBuilder(file, signature);
                        verifySignature(signatureVerificationService, file, signature, allTrustedKeys(foundArtifact, verification.getTrustedPgpKeys()), allIgnoredKeys(verification.getIgnoredPgpKeys()), result);
                        if (result.hasOnlyIgnoredKeys()) {
                            builder.failWith(new OnlyIgnoredKeys(file));
                            if (verification.getChecksums().isEmpty()) {
                                builder.failWith(new MissingChecksums(file));
                                return;
                            } else {
                                verifyChecksums(checksumService, file, verification, builder);
                                return;
                            }
                        }
                        if (result.hasError()) {
                            builder.failWith(result.asError(publicKeyService));
                            return;
                        }
                    }
                    verifyChecksums(checksumService, file, verification, builder);
                    return;
                }
            }
        }
        if (signature != null) {
            // it's possible that the artifact is not listed explicitly but we can still verify signatures
            DefaultSignatureVerificationResultBuilder result = new DefaultSignatureVerificationResultBuilder(file, signature);
            verifySignature(signatureVerificationService, file, signature, allTrustedKeys(foundArtifact, Collections.emptySet()), allIgnoredKeys(Collections.emptySet()), result);
            if (result.hasError()) {
                builder.failWith(result.asError(publicKeyService));
                return;
            } else if (!result.hasOnlyIgnoredKeys()) {
                return;
            }
        }
        builder.failWith(new MissingChecksums(file));
    }

    private Set<String> allTrustedKeys(ModuleComponentArtifactIdentifier id, Set<String> artifactSpecificKeys) {
        if (config.getTrustedKeys().isEmpty()) {
            return artifactSpecificKeys;
        } else {
            Set<String> allKeys = Sets.newHashSet(artifactSpecificKeys);
            config.getTrustedKeys()
                .stream()
                .filter(trustedKey -> trustedKey.matches(id))
                .forEach(trustedKey -> allKeys.add(trustedKey.getKeyId()));
            return allKeys;
        }
    }

    private Set<String> allIgnoredKeys(Set<IgnoredKey> artifactSpecificKeys) {
        if (config.getIgnoredKeys().isEmpty()) {
            return artifactSpecificKeys.stream().map(IgnoredKey::getKeyId).collect(Collectors.toSet());
        } else {
            if (artifactSpecificKeys.isEmpty()) {
                return config.getIgnoredKeys().stream().map(IgnoredKey::getKeyId).collect(Collectors.toSet());
            }
            Set<String> allKeys = Sets.newHashSet();
            artifactSpecificKeys.stream()
                .map(IgnoredKey::getKeyId)
                .forEach(allKeys::add);
            config.getIgnoredKeys()
                .stream()
                .map(IgnoredKey::getKeyId)
                .forEach(allKeys::add);
            return allKeys;
        }
    }

    private void verifySignature(SignatureVerificationService signatureVerificationService, File file, File signature, Set<String> trustedKeys, Set<String> ignoredKeys, SignatureVerificationResultBuilder result) {
        signatureVerificationService.verify(file, signature, trustedKeys, ignoredKeys, result);
    }

    private void verifyChecksums(ChecksumService checksumService, File file, ArtifactVerificationMetadata verification, ArtifactVerificationResultBuilder builder) {
        List<Checksum> checksums = verification.getChecksums();
        for (Checksum checksum : checksums) {
            verifyChecksum(checksum.getKind(), file, checksum.getValue(), checksum.getAlternatives(), checksumService, builder);
        }
    }

    private static void verifyChecksum(ChecksumKind algorithm, File file, String expected, Set<String> alternatives, ChecksumService cache, ArtifactVerificationResultBuilder builder) {
        String actualChecksum = checksumOf(algorithm, file, cache);
        if (expected.equals(actualChecksum)) {
            return;
        }
        if (alternatives != null) {
            for (String alternative : alternatives) {
                if (actualChecksum.equals(alternative)) {
                    return;
                }
            }
        }
        builder.failWith(new ChecksumVerificationFailure(file, algorithm, expected, actualChecksum));
    }

    private static String checksumOf(ChecksumKind algorithm, File file, ChecksumService cache) {
        HashCode hashValue = null;
        switch (algorithm) {
            case md5:
                hashValue = cache.md5(file);
                break;
            case sha1:
                hashValue = cache.sha1(file);
                break;
            case sha256:
                hashValue = cache.sha256(file);
                break;
            case sha512:
                hashValue = cache.sha512(file);
                break;
        }
        return hashValue.toString();
    }

    public Collection<ComponentVerificationMetadata> getVerificationMetadata() {
        return verificationMetadata.values();
    }

    public DependencyVerificationConfiguration getConfiguration() {
        return config;
    }

    public List<String> getSuggestedWriteFlags() {
        Set<String> writeFlags = Sets.newLinkedHashSet();
        if (config.isVerifySignatures()) {
            writeFlags.add("pgp");
        }
        getVerificationMetadata().forEach(md -> {
            md.getArtifactVerifications().forEach(av -> {
                av.getChecksums().forEach(checksum -> writeFlags.add(checksum.getKind().name()));
            });
        });
        if (Collections.singleton("pgp").equals(writeFlags)) {
            // need to suggest at least one checksum so we use the most secure
            writeFlags.add("sha512");
        }
        return ImmutableList.copyOf(writeFlags);
    }

    private static class DefaultSignatureVerificationResultBuilder implements SignatureVerificationResultBuilder {
        private final File file;
        private final File signatureFile;
        private List<String> missingKeys = null;
        private List<PGPPublicKey> trustedKeys = null;
        private List<PGPPublicKey> validNotTrusted = null;
        private List<PGPPublicKey> failedKeys = null;
        private List<String> ignoredKeys = null;

        private DefaultSignatureVerificationResultBuilder(File file, File signatureFile) {
            this.file = file;
            this.signatureFile = signatureFile;
        }

        @Override
        public void missingKey(String keyId) {
            if (missingKeys == null) {
                missingKeys = Lists.newArrayList();
            }
            missingKeys.add(keyId);
        }

        @Override
        public void verified(PGPPublicKey key, boolean trusted) {
            if (trusted) {
                if (trustedKeys == null) {
                    trustedKeys = Lists.newArrayList();
                }
                trustedKeys.add(key);
            } else {
                if (validNotTrusted == null) {
                    validNotTrusted = Lists.newArrayList();
                }
                validNotTrusted.add(key);
            }
        }

        @Override
        public void failed(PGPPublicKey pgpPublicKey) {
            if (failedKeys == null) {
                failedKeys = Lists.newArrayList();
            }
            failedKeys.add(pgpPublicKey);
        }

        @Override
        public void ignored(String keyId) {
            if (ignoredKeys == null) {
                ignoredKeys = Lists.newArrayList();
            }
            ignoredKeys.add(keyId);
        }

        boolean hasOnlyIgnoredKeys() {
            return ignoredKeys != null
                && trustedKeys == null
                && validNotTrusted == null
                && missingKeys == null
                && failedKeys == null;
        }

        public SignatureVerificationFailure asError(PublicKeyService publicKeyService) {
            Map<String, SignatureVerificationFailure.SignatureError> errors = Maps.newHashMap();
            if (missingKeys != null) {
                for (String missingKey : missingKeys) {
                    errors.put(missingKey, error(null, SignatureVerificationFailure.FailureKind.MISSING_KEY));
                }
            }
            if (failedKeys != null) {
                for (PGPPublicKey failedKey : failedKeys) {
                    errors.put(Fingerprint.of(failedKey).toString(), error(failedKey, SignatureVerificationFailure.FailureKind.FAILED));
                }
            }
            if (validNotTrusted != null) {
                for (PGPPublicKey trustedKey : validNotTrusted) {
                    errors.put(Fingerprint.of(trustedKey).toString(), error(trustedKey, SignatureVerificationFailure.FailureKind.PASSED_NOT_TRUSTED));
                }
            }
            if (ignoredKeys != null) {
                for (String ignoredKey : ignoredKeys) {
                    errors.put(ignoredKey, error(null, SignatureVerificationFailure.FailureKind.IGNORED_KEY));
                }
            }
            return new SignatureVerificationFailure(file, signatureFile, ImmutableMap.copyOf(errors), publicKeyService);
        }

        public boolean hasError() {
            return failedKeys != null || validNotTrusted != null || missingKeys != null;
        }
    }

    private static SignatureVerificationFailure.SignatureError error(@Nullable PGPPublicKey key, SignatureVerificationFailure.FailureKind kind) {
        return new SignatureVerificationFailure.SignatureError(key, kind);
    }
}
