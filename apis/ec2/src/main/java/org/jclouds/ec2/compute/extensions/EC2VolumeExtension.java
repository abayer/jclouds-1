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
package org.jclouds.ec2.compute.extensions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.transform;
import static org.jclouds.util.Predicates2.retry;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;

import org.jclouds.Constants;
import org.jclouds.aws.util.AWSUtils;
import org.jclouds.collect.Memoized;
import org.jclouds.compute.domain.Snapshot;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.extensions.VolumeExtension;
import org.jclouds.compute.extensions.options.AttachVolumeOptions;
import org.jclouds.compute.extensions.options.CreateSnapshotOptions;
import org.jclouds.compute.extensions.options.DetachVolumeOptions;
import org.jclouds.compute.extensions.options.VolumeOptions;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.domain.Location;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.compute.extensions.options.EC2DetachVolumeOptions;
import org.jclouds.ec2.compute.extensions.options.EC2VolumeOptions;
import org.jclouds.ec2.domain.Attachment;
import org.jclouds.ec2.domain.BlockDevice;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.ec2.features.ElasticBlockStoreApi;
import org.jclouds.ec2.options.DescribeSnapshotsOptions;
import org.jclouds.ec2.predicates.SnapshotCompleted;
import org.jclouds.ec2.predicates.VolumeAttached;
import org.jclouds.ec2.predicates.VolumeAvailable;
import org.jclouds.ec2.predicates.VolumeDeleted;
import org.jclouds.ec2.predicates.VolumeDetached;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.location.Region;
import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * TODO live and expect tests
 * An extension to compute service to allow for the manipulation of {@link org.jclouds.compute.domain.Volume}s. Implementation
 * is optional by providers.
 *
 * @author Andrew Bayer
 */
public class EC2VolumeExtension implements VolumeExtension {

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   protected final EC2Api api;
   protected final ListeningExecutorService userExecutor;
   protected final Supplier<Set<String>> regions;
   protected final Function<org.jclouds.ec2.domain.Volume, Volume> volumeConverter;
   protected final Function<org.jclouds.ec2.domain.Snapshot, Snapshot> snapshotConverter;
   protected final Supplier<Set<? extends Location>> locations;

   @Inject
   public EC2VolumeExtension(EC2Api api,
                             @Named(Constants.PROPERTY_USER_THREADS) ListeningExecutorService userExecutor,
                             @Region Supplier<Set<String>> regions,
                             Function<org.jclouds.ec2.domain.Volume, Volume> volumeConverter,
                             Function<org.jclouds.ec2.domain.Snapshot, Snapshot> snapshotConverter,
                             @Memoized Supplier<Set<? extends Location>> locations) {
      this.api = checkNotNull(api, "api");
      this.userExecutor = checkNotNull(userExecutor, "userExecutor");
      this.regions = checkNotNull(regions, "regions");
      this.volumeConverter = checkNotNull(volumeConverter, "volumeConverter");
      this.snapshotConverter = checkNotNull(snapshotConverter, "snapshotConverter");
      this.locations = checkNotNull(locations, "locations");
   }

   @Override
   public Set<Volume> listVolumes() {
      Iterable<? extends org.jclouds.ec2.domain.Volume> rawVolumes = pollVolumes();
      Iterable<Volume> volumes = transform(filter(rawVolumes, notNull()),
              volumeConverter);
      return ImmutableSet.copyOf(volumes);
   }

   @Override
   public Set<Volume> listVolumesInLocation(final String region) {
      Iterable<? extends org.jclouds.ec2.domain.Volume> rawVolumes = pollVolumesByRegion(region);
      Iterable<Volume> volumes = transform(filter(rawVolumes, notNull()),
              volumeConverter);
      return ImmutableSet.copyOf(volumes);
   }

   @Override
   public Set<Volume> listVolumesForNode(String id) {
      checkNotNull(id, "id");
      String[] parts = AWSUtils.parseHandle(id);
      String region = parts[0];
      String instanceId = parts[1];

      RunningInstance instance = getOnlyElement(Iterables.concat(api.getInstanceApi().get().describeInstancesInRegion(region, instanceId)));

      if (instance == null) {
         return ImmutableSet.of();
      }

      if (instance.getEbsBlockDevices().size() == 0) {
         return ImmutableSet.of();
      }

      Set<String> volumeIds = ImmutableSet.copyOf(transform(instance.getEbsBlockDevices().values(),
              new Function<BlockDevice,String>() {

                 @Override
                 public String apply(BlockDevice device) {
                    return device.getVolumeId();
                 }
              }));

      Set<? extends org.jclouds.ec2.domain.Volume> rawVolumes = api.getElasticBlockStoreApi().get()
              .describeVolumesInRegion(region, Iterables.toArray(volumeIds, String.class));

      return ImmutableSet.copyOf(transform(filter(rawVolumes, notNull()), volumeConverter));
   }

   @Override
   public Volume getVolumeById(String id) {
      checkNotNull(id, "id");
      String[] parts = AWSUtils.parseHandle(id);
      String region = parts[0];
      String volumeId = parts[1];

      Set<? extends org.jclouds.ec2.domain.Volume> rawVolumes =
              api.getElasticBlockStoreApi().get().describeVolumesInRegion(region, volumeId);

      return getOnlyElement(transform(filter(rawVolumes, notNull()), volumeConverter));
   }

   @Override
   public Volume createVolume(String name, VolumeOptions volumeOptions) {
      checkNotNull(volumeOptions, "options should not be null");
      checkArgument(volumeOptions instanceof EC2VolumeOptions, "options must be EC2VolumeOptions");
      EC2VolumeOptions options = EC2VolumeOptions.class.cast(volumeOptions);
      checkNotNull(options.getAvailabilityZone(), "availability zone should not be null");

      org.jclouds.ec2.domain.Volume ec2Volume;

      ElasticBlockStoreApi ebsApi = api.getElasticBlockStoreApi().get();
      Predicate<org.jclouds.ec2.domain.Volume> available = retry(new VolumeAvailable(ebsApi), 600, 10, TimeUnit.SECONDS);

      if (options.getSnapshotId() != null) {
         String[] parts = AWSUtils.parseHandle(options.getSnapshotId());
         String region = parts[0];
         String snapshotId = parts[1];
         if (ebsApi.describeSnapshotsInRegion(region,
                 DescribeSnapshotsOptions.Builder.snapshotIds(snapshotId)) == null) {
            logger.error("No snapshot with id %s found in region %s", snapshotId,
                    region);

            return null;
         }

         if (options.getSizeGb() != null && options.getSizeGb() > 0) {
            ec2Volume = ebsApi.createVolumeFromSnapshotInAvailabilityZone(options.getAvailabilityZone(),
                            options.getSizeGb().intValue(), snapshotId);
         } else {
            ec2Volume = ebsApi.createVolumeFromSnapshotInAvailabilityZone(options.getAvailabilityZone(),
                    snapshotId);
         }
      } else {
         checkNotNull(options.getSizeGb(), "volume size must be specified");
         ec2Volume = ebsApi.createVolumeInAvailabilityZone(options.getAvailabilityZone(),
                 options.getSizeGb().intValue());
      }

      available.apply(ec2Volume);

      Volume volume = getOnlyElement(transform(ebsApi.describeVolumesInRegion(ec2Volume.getRegion(),
              ec2Volume.getId()), volumeConverter), null);

      return volume;
   }

   @Override
   public boolean attachVolume(String regionAndVolumeId, String regionAndNodeId, AttachVolumeOptions options) {
      checkNotNull(regionAndVolumeId, "regionAndVolumeId");
      String[] parts = AWSUtils.parseHandle(regionAndVolumeId);
      String region = parts[0];
      String volumeId = parts[1];

      checkNotNull(regionAndNodeId, "regionAndNodeId");
      String[] nodeParts = AWSUtils.parseHandle(regionAndNodeId);
      String nodeId = nodeParts[1];

      checkNotNull(options, "options must not be null");
      checkNotNull(options.getDevice(), "options.getDevice() must not be null");

      ElasticBlockStoreApi ebsApi = api.getElasticBlockStoreApi().get();
      Predicate<Attachment> attached = retry(new VolumeAttached(ebsApi), 600, 10, TimeUnit.SECONDS);

      Attachment attachment = ebsApi.attachVolumeInRegion(region, volumeId, nodeId, options.getDevice());

      return attached.apply(attachment);
   }

   @Override
   public boolean detachVolume(String regionAndVolumeId, DetachVolumeOptions options) {
      checkNotNull(regionAndVolumeId, "regionAndVolumeId");
      String[] parts = AWSUtils.parseHandle(regionAndVolumeId);
      String region = parts[0];
      String volumeId = parts[1];

      checkNotNull(options, "options must not be null");
      checkArgument(options instanceof EC2DetachVolumeOptions, "options must be of type EC2DetachVolumeOptions");

      EC2DetachVolumeOptions ec2Options = EC2DetachVolumeOptions.class.cast(options);

      org.jclouds.ec2.options.DetachVolumeOptions apiOptions = new org.jclouds.ec2.options.DetachVolumeOptions();

      final String device;
      final String instanceId;

      if (options.getDevice() != null) {
         device = ec2Options.getDevice();
         apiOptions.fromDevice(device);
      } else {
         device = null;
      }

      if (options.getInstanceId() != null) {
         String[] nodeParts = AWSUtils.parseHandle(ec2Options.getInstanceId());
         instanceId = nodeParts[1];
         apiOptions.fromInstance(instanceId);
      } else {
         instanceId = null;
      }

      ElasticBlockStoreApi ebsApi = api.getElasticBlockStoreApi().get();
      Predicate<Attachment> detached = retry(new VolumeDetached(ebsApi), 600, 10, TimeUnit.SECONDS);

      org.jclouds.ec2.domain.Volume volume = getOnlyElement(ebsApi.describeVolumesInRegion(region,
              volumeId), null);

      if (volume == null) {
         logger.error("volume %s in region %s does not exist", volumeId, region);
         return false;
      }

      if (volume.getAttachments().size() == 0) {
         logger.error("volume %s in region %s does not have any attachments", volumeId, region);
         return false;
      }

      Attachment attachment = find(volume.getAttachments(), new Predicate<Attachment>() {
         @Override
         public boolean apply(@Nullable Attachment input) {
            return (device == null || device.equals(input.getDevice()))
                    && (instanceId == null || instanceId.equals(input.getId()));
         }
      });

      ebsApi.detachVolumeInRegion(region, volumeId, ec2Options.shouldForce(), apiOptions);

      return detached.apply(attachment);
   }

   @Override
   public boolean removeVolume(String id) {
      checkNotNull(id, "id");
      String[] parts = AWSUtils.parseHandle(id);
      String region = parts[0];
      String volumeId = parts[1];

      ElasticBlockStoreApi ebsApi = api.getElasticBlockStoreApi().get();

      org.jclouds.ec2.domain.Volume volume = getOnlyElement(ebsApi.describeVolumesInRegion(region,
              volumeId), null);

      if (volume == null) {
         logger.error("No volume found for volume id %s in region %s", volumeId, region);
         return false;
      }

      Predicate<org.jclouds.ec2.domain.Volume> deleted = retry(new VolumeDeleted(ebsApi), 600, 10, TimeUnit.SECONDS);

      ebsApi.deleteVolumeInRegion(region, volumeId);

      return deleted.apply(volume);
   }

   @Override
   public Set<Snapshot> listSnapshots() {
      Iterable<? extends org.jclouds.ec2.domain.Snapshot> rawSnapshots = pollSnapshots();
      Iterable<Snapshot> snapshots = transform(filter(rawSnapshots, notNull()),
              snapshotConverter);
      return ImmutableSet.copyOf(snapshots);
   }

   @Override
   public Set<Snapshot> listSnapshotsInLocation(String region) {
      Iterable<? extends org.jclouds.ec2.domain.Snapshot> rawSnapshots = pollSnapshotsByRegion(region);
      Iterable<Snapshot> snapshots = transform(filter(rawSnapshots, notNull()),
              snapshotConverter);
      return ImmutableSet.copyOf(snapshots);
   }

   @Override
   public Set<Snapshot> listSnapshotsForVolume(String id) {
      checkNotNull(id, "id");
      String[] parts = AWSUtils.parseHandle(id);
      String region = parts[0];
      String volumeId = parts[1];

      if (getVolumeById(id) == null) {
         return ImmutableSet.of();
      }

      Set<? extends org.jclouds.ec2.domain.Snapshot> rawSnapshots = api.getElasticBlockStoreApi().get()
              .describeSnapshotsInRegion(region,
                      ImmutableMultimap.<String, String> builder().put("volume-id", volumeId).build());

      return ImmutableSet.copyOf(transform(filter(rawSnapshots, notNull()), snapshotConverter));
   }

   @Override
   public Snapshot getSnapshotById(String id) {
      checkNotNull(id, "id");
      String[] parts = AWSUtils.parseHandle(id);
      String region = parts[0];
      String snapshotId = parts[1];

      Set<? extends org.jclouds.ec2.domain.Snapshot> rawSnapshots =
              api.getElasticBlockStoreApi().get()
                      .describeSnapshotsInRegion(region,
                              DescribeSnapshotsOptions.Builder.snapshotIds(snapshotId));

      return getOnlyElement(transform(filter(rawSnapshots, notNull()), snapshotConverter));
   }

   @Override
   public Snapshot createSnapshot(String id, CreateSnapshotOptions options) {
      checkNotNull(id, "id");
      String[] parts = AWSUtils.parseHandle(id);
      String region = parts[0];
      String volumeId = parts[1];

      ElasticBlockStoreApi ebsApi = api.getElasticBlockStoreApi().get();
      Predicate<org.jclouds.ec2.domain.Snapshot> completed = retry(new SnapshotCompleted(ebsApi), 600, 10, TimeUnit.SECONDS);

      if (getVolumeById(id) == null) {
         logger.error("No volume found for volume id %s in region %s", volumeId, region);
         return null;
      }

      org.jclouds.ec2.domain.Snapshot rawSnapshot = ebsApi.createSnapshotInRegion(region, volumeId);

      completed.apply(rawSnapshot);

      Set<? extends org.jclouds.ec2.domain.Snapshot> rawSnapshots =
              api.getElasticBlockStoreApi().get()
                      .describeSnapshotsInRegion(region,
                              DescribeSnapshotsOptions.Builder.snapshotIds(rawSnapshot.getId()));

      return getOnlyElement(transform(filter(rawSnapshots, notNull()), snapshotConverter));
   }

   @Override
   public boolean deleteSnapshot(String id) {
      checkNotNull(id, "id");
      String[] parts = AWSUtils.parseHandle(id);
      String region = parts[0];
      String snapshotId = parts[1];

      ElasticBlockStoreApi ebsApi = api.getElasticBlockStoreApi().get();

      org.jclouds.ec2.domain.Snapshot snapshot =
              getOnlyElement(ebsApi.describeSnapshotsInRegion(region,
                      DescribeSnapshotsOptions.Builder.snapshotIds(snapshotId)),
                      null);

      if (snapshot == null) {
         logger.error("No snapshot found for snapshot id %s in region %s", snapshotId, region);
         return false;
      }

      ebsApi.deleteSnapshotInRegion(region, snapshotId);

      return ebsApi.describeSnapshotsInRegion(region,
              DescribeSnapshotsOptions.Builder.snapshotIds(snapshotId)).size() == 0;
   }

   protected Iterable<? extends org.jclouds.ec2.domain.Volume> pollVolumes() {
      Iterable<? extends Set<? extends org.jclouds.ec2.domain.Volume>> volumes
              = transform(regions.get(), allVolumesInRegion());

      return concat(volumes);
   }

   protected Iterable<? extends org.jclouds.ec2.domain.Volume> pollVolumesByRegion(String region) {
      return allVolumesInRegion().apply(region);
   }

   protected Function<String, Set<? extends org.jclouds.ec2.domain.Volume>> allVolumesInRegion() {
      return new Function<String, Set<? extends org.jclouds.ec2.domain.Volume>>() {

         @Override
         public Set<? extends org.jclouds.ec2.domain.Volume> apply(String from) {
            return api.getElasticBlockStoreApi().get().describeVolumesInRegion(from);
         }

      };
   }

   protected Iterable<? extends org.jclouds.ec2.domain.Snapshot> pollSnapshots() {
      Iterable<? extends Set<? extends org.jclouds.ec2.domain.Snapshot>> snapshots
              = transform(regions.get(), allSnapshotsInRegion());

      return concat(snapshots);
   }

   protected Iterable<? extends org.jclouds.ec2.domain.Snapshot> pollSnapshotsByRegion(String region) {
      return allSnapshotsInRegion().apply(region);
   }

   protected Function<String, Set<? extends org.jclouds.ec2.domain.Snapshot>> allSnapshotsInRegion() {
      return new Function<String, Set<? extends org.jclouds.ec2.domain.Snapshot>>() {

         @Override
         public Set<? extends org.jclouds.ec2.domain.Snapshot> apply(String from) {
            return api.getElasticBlockStoreApi().get().describeSnapshotsInRegion(from);
         }

      };
   }
}