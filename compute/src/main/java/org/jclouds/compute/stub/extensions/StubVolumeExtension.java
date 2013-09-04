/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.compute.stub.extensions;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.jclouds.Constants;
import org.jclouds.compute.domain.Snapshot;
import org.jclouds.compute.domain.SnapshotBuilder;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.domain.VolumeBuilder;
import org.jclouds.compute.extensions.VolumeExtension;
import org.jclouds.compute.extensions.options.AttachVolumeOptions;
import org.jclouds.compute.extensions.options.CreateSnapshotOptions;
import org.jclouds.compute.extensions.options.DetachVolumeOptions;
import org.jclouds.compute.extensions.options.VolumeOptions;
import org.jclouds.compute.predicates.SnapshotPredicates;
import org.jclouds.compute.predicates.VolumePredicates;
import org.jclouds.domain.Location;
import org.jclouds.location.suppliers.all.JustProvider;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * An extension to compute service to allow for the manipulation of {@link VolumeExtension}s. Implementation
 * is optional by providers.
 *
 * @author Andrew Bayer
 */
public class StubVolumeExtension implements VolumeExtension {
   private final Supplier<Location> location;
   private final Provider<Integer> volumeIdProvider;
   private final Provider<Integer> snapshotIdProvider;
   private final Supplier<Set<? extends Location>> locationSupplier;
   private final ListeningExecutorService ioExecutor;
   private final ConcurrentMap<String, Volume> volumes;
   private final Multimap<String, Volume> volumesForNodes;
   private final Multimap<String, Volume> volumesForLocations;
   private final ConcurrentMap<String, Snapshot> snapshots;
   private final Multimap<String, Snapshot> snapshotsForVolumes;
   private final Multimap<String, Snapshot> snapshotsForLocations;

   @Inject
   public StubVolumeExtension(ConcurrentMap<String, Volume> volumes,
                              @Named(Constants.PROPERTY_IO_WORKER_THREADS) ListeningExecutorService ioExecutor,
                              Supplier<Location> location,
                              @Named("VOLUME_ID") Provider<Integer> volumeIdProvider,
                              @Named("SNAPSHOT_ID") Provider<Integer> snapshotIdProvider,
                              JustProvider locationSupplier,
                              @Named("VOLUME_NODE") Multimap<String, Volume> volumesForNodes,
                              @Named("VOLUME_LOCATION") Multimap<String, Volume> volumesForLocations,
                              @Named("SNAPSHOT_VOLUME") Multimap<String, Snapshot> snapshotsForVolumes,
                              @Named("SNAPSHOT_LOCATION") Multimap<String, Snapshot> snapshotsForLocations,
                              ConcurrentMap<String, Snapshot> snapshots) {
      this.volumes = volumes;
      this.ioExecutor = ioExecutor;
      this.location = location;
      this.volumeIdProvider = volumeIdProvider;
      this.snapshotIdProvider = snapshotIdProvider;
      this.locationSupplier = locationSupplier;
      this.volumesForNodes = volumesForNodes;
      this.volumesForLocations = volumesForLocations;
      this.snapshotsForVolumes = snapshotsForVolumes;
      this.snapshotsForLocations = snapshotsForLocations;
      this.snapshots = snapshots;
   }

   @Override
   public Set<Volume> listVolumes() {
      return ImmutableSet.copyOf(volumes.values());
   }

   @Override
   public Set<Volume> listVolumesInLocation(final String locationId) {
      return ImmutableSet.copyOf(volumesForLocations.get(locationId));
   }

   @Override
   public Set<Volume> listVolumesForNode(String nodeId) {
      return ImmutableSet.copyOf(volumesForNodes.get(nodeId));
   }

   @Override
   public Volume getVolumeById(String id) {
      return volumes.get(id);
   }

   @Override
   public Volume createVolume(String volumeName, VolumeOptions options) {
      VolumeBuilder builder = new VolumeBuilder();

      String id = volumeIdProvider.get() + "";
      builder.id(id);
      builder.name(volumeName);
      builder.size(options.getSizeGb());
      builder.type(Volume.Type.LOCAL);
      builder.locationId(options.getLocationId());

      Volume volume = builder.build();

      volumes.put(volume.getId(), volume);
      volumesForLocations.put(options.getLocationId(), volume);

      return volume;
   }

   @Override
   public boolean removeVolume(final String id) {
      if (volumes.containsKey(id)) {
         Volume volume = volumes.get(id);
         volumes.remove(id);

         if (volumesForLocations.containsValue(volume)) {
            volumesForLocations.remove(volume.getLocationId(), volume);
         }

         if (volumesForNodes.size() > 0) {
            String nodeId = Iterables.find(volumesForNodes.keySet(), new Predicate<String>() {

               @Override
               public boolean apply(String input) {
                  return Iterables.any(volumesForNodes.get(input), VolumePredicates.idEquals(id));
               }
            });
            if (nodeId != null) {
               volumesForNodes.remove(nodeId, volume);
            }
         }

         return true;
      }
      return false;
   }

   @Override
   public boolean attachVolume(String volumeId, String nodeId, AttachVolumeOptions options) {
      if (volumes.containsKey(volumeId)) {
         Volume volume = volumes.get(volumeId);

         volumesForNodes.put(nodeId, volume);
         return true;
      }

      return false;
   }

   @Override
   public boolean detachVolume(final String volumeId, DetachVolumeOptions options) {
      if (volumes.containsKey(volumeId)) {
         Volume volume = volumes.get(volumeId);

         if (volumesForNodes.size() > 0) {
            String nodeId = Iterables.find(volumesForNodes.keySet(), new Predicate<String>() {

               @Override
               public boolean apply(String input) {
                  return Iterables.any(volumesForNodes.get(input), VolumePredicates.idEquals(volumeId));
               }
            });
            if (nodeId != null) {
               volumesForNodes.remove(nodeId, volume);
            }
            return true;
         }

      }
      return false;
   }

   @Override
   public Set<Snapshot> listSnapshots() {
      return ImmutableSet.copyOf(snapshots.values());
   }

   @Override
   public Set<Snapshot> listSnapshotsInLocation(final String locationId) {
      return ImmutableSet.copyOf(snapshotsForLocations.get(locationId));
   }

   @Override
   public Set<Snapshot> listSnapshotsForVolume(String volumeId) {
      return ImmutableSet.copyOf(snapshotsForVolumes.get(volumeId));
   }

   @Override
   public Snapshot getSnapshotById(String id) {
      return snapshots.get(id);
   }

   @Override
   public Snapshot createSnapshot(final String volumeId, CreateSnapshotOptions options) {
      if (volumes.containsKey(volumeId)) {
         Volume volume = volumes.get(volumeId);

         SnapshotBuilder builder = new SnapshotBuilder();

         String id = snapshotIdProvider.get() + "";
         builder.id(id);
         builder.size(volume.getSize());
         builder.locationId(volume.getLocationId());
         builder.volumeId(volumeId);

         Snapshot snapshot = builder.build();

         snapshots.put(snapshot.getId(), snapshot);
         snapshotsForLocations.put(snapshot.getLocationId(), snapshot);
         snapshotsForVolumes.put(volume.getId(), snapshot);

         return snapshot;
      }

      return null;
   }

   @Override
   public boolean deleteSnapshot(final String id) {
      if (snapshots.containsKey(id)) {
         Snapshot snapshot = snapshots.get(id);
         snapshots.remove(id);

         if (snapshotsForLocations.containsValue(snapshot)) {
            snapshotsForLocations.remove(snapshot.getLocationId(), snapshot);
         }

         if (snapshotsForVolumes.size() > 0) {
            String volumeId = Iterables.find(snapshotsForVolumes.keySet(), new Predicate<String>() {

               @Override
               public boolean apply(String input) {
                  return Iterables.any(snapshotsForVolumes.get(input), SnapshotPredicates.idEquals(id));
               }
            });
            if (volumeId != null) {
               snapshotsForVolumes.remove(volumeId, snapshot);
            }
         }

         return true;
      }
      return false;
   }
}
