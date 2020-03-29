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

package org.gradle.caching.internal.controller.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Interner;
import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.internal.CacheableEntity;
import org.gradle.caching.internal.controller.BuildCacheCommandFactory;
import org.gradle.caching.internal.controller.BuildCacheLoadCommand;
import org.gradle.caching.internal.controller.BuildCacheStoreCommand;
import org.gradle.caching.internal.origin.OriginMetadata;
import org.gradle.caching.internal.origin.OriginMetadataFactory;
import org.gradle.caching.internal.packaging.BuildCacheEntryPacker;
import org.gradle.internal.file.FileType;
import org.gradle.internal.fingerprint.CurrentFileCollectionFingerprint;
import org.gradle.internal.fingerprint.FingerprintingStrategy;
import org.gradle.internal.fingerprint.impl.AbsolutePathFingerprintingStrategy;
import org.gradle.internal.fingerprint.impl.DefaultCurrentFileCollectionFingerprint;
import org.gradle.internal.snapshot.CompleteFileSystemLocationSnapshot;
import org.gradle.internal.snapshot.FileSystemSnapshot;
import org.gradle.internal.snapshot.MissingFileSnapshot;
import org.gradle.internal.vfs.VirtualFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultBuildCacheCommandFactory implements BuildCacheCommandFactory {

    private final BuildCacheEntryPacker packer;
    private final OriginMetadataFactory originMetadataFactory;
    private final VirtualFileSystem virtualFileSystem;
    private final Interner<String> stringInterner;

    public DefaultBuildCacheCommandFactory(BuildCacheEntryPacker packer, OriginMetadataFactory originMetadataFactory, VirtualFileSystem virtualFileSystem, Interner<String> stringInterner) {
        this.packer = packer;
        this.originMetadataFactory = originMetadataFactory;
        this.virtualFileSystem = virtualFileSystem;
        this.stringInterner = stringInterner;
    }

    @Override
    public BuildCacheLoadCommand<LoadMetadata> createLoad(BuildCacheKey cacheKey, CacheableEntity entity) {
        return new LoadCommand(cacheKey, entity);
    }

    @Override
    public BuildCacheStoreCommand createStore(BuildCacheKey cacheKey, CacheableEntity entity, Map<String, ? extends FileSystemSnapshot> snapshots, long executionTime) {
        return new StoreCommand(cacheKey, entity, snapshots, executionTime);
    }

    private class LoadCommand implements BuildCacheLoadCommand<LoadMetadata> {

        private final BuildCacheKey cacheKey;
        private final CacheableEntity entity;

        private LoadCommand(BuildCacheKey cacheKey, CacheableEntity entity) {
            this.cacheKey = cacheKey;
            this.entity = entity;
        }

        @Override
        public BuildCacheKey getKey() {
            return cacheKey;
        }

        @Override
        public BuildCacheLoadCommand.Result<LoadMetadata> load(InputStream input) throws IOException {
            ImmutableList.Builder<String> roots = ImmutableList.builder();
            entity.visitOutputTrees((name, type, root) -> roots.add(root.getAbsolutePath()));
            // TODO: Actually unpack the roots inside of the action
            virtualFileSystem.update(roots.build(), () -> {});
            BuildCacheEntryPacker.UnpackResult unpackResult = packer.unpack(entity, input, originMetadataFactory.createReader(entity));
            // TODO: Update the snapshots from the action
            ImmutableSortedMap<String, CurrentFileCollectionFingerprint> snapshots = snapshotUnpackedData(unpackResult.getSnapshots());
            return new Result<LoadMetadata>() {
                @Override
                public long getArtifactEntryCount() {
                    return unpackResult.getEntries();
                }

                @Override
                public LoadMetadata getMetadata() {
                    return new LoadMetadata() {
                        @Override
                        public OriginMetadata getOriginMetadata() {
                            return unpackResult.getOriginMetadata();
                        }

                        @Override
                        public ImmutableSortedMap<String, CurrentFileCollectionFingerprint> getResultingSnapshots() {
                            return snapshots;
                        }
                    };
                }
            };
        }

        private ImmutableSortedMap<String, CurrentFileCollectionFingerprint> snapshotUnpackedData(Map<String, ? extends CompleteFileSystemLocationSnapshot> treeSnapshots) {
            ImmutableSortedMap.Builder<String, CurrentFileCollectionFingerprint> builder = ImmutableSortedMap.naturalOrder();
            FingerprintingStrategy fingerprintingStrategy = AbsolutePathFingerprintingStrategy.IGNORE_MISSING;
            entity.visitOutputTrees((treeName, type, root) -> {
                CompleteFileSystemLocationSnapshot treeSnapshot = treeSnapshots.get(treeName);
                String internedAbsolutePath = stringInterner.intern(root.getAbsolutePath());
                List<FileSystemSnapshot> roots = new ArrayList<>();

                if (treeSnapshot == null) {
                    MissingFileSnapshot missingFileSnapshot = new MissingFileSnapshot(internedAbsolutePath);
                    virtualFileSystem.updateWithKnownSnapshot(missingFileSnapshot);
                    builder.put(treeName, fingerprintingStrategy.getEmptyFingerprint());
                    return;
                }

                switch (type) {
                    case FILE:
                        if (treeSnapshot.getType() != FileType.RegularFile) {
                            throw new IllegalStateException(String.format("Only a regular file should be produced by unpacking tree '%s', but saw a %s", treeName, treeSnapshot.getType()));
                        }
                        roots.add(treeSnapshot);
                        virtualFileSystem.updateWithKnownSnapshot(treeSnapshot);
                        break;
                    case DIRECTORY:
                        roots.add(treeSnapshot);
                        virtualFileSystem.updateWithKnownSnapshot(treeSnapshot);
                        break;
                    default:
                        throw new AssertionError();
                }
                builder.put(treeName, DefaultCurrentFileCollectionFingerprint.from(roots, fingerprintingStrategy));
            });
            return builder.build();
        }
    }

    private class StoreCommand implements BuildCacheStoreCommand {

        private final BuildCacheKey cacheKey;
        private final CacheableEntity entity;
        private final Map<String, ? extends FileSystemSnapshot> fingerprints;
        private final long executionTime;

        private StoreCommand(BuildCacheKey cacheKey, CacheableEntity entity, Map<String, ? extends FileSystemSnapshot> fingerprints, long executionTime) {
            this.cacheKey = cacheKey;
            this.entity = entity;
            this.fingerprints = fingerprints;
            this.executionTime = executionTime;
        }

        @Override
        public BuildCacheKey getKey() {
            return cacheKey;
        }

        @Override
        public BuildCacheStoreCommand.Result store(OutputStream output) throws IOException {
            final BuildCacheEntryPacker.PackResult packResult = packer.pack(entity, fingerprints, output, originMetadataFactory.createWriter(entity, executionTime));
            return packResult::getEntries;
        }
    }
}
