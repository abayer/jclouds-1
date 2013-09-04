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
package org.jclouds.compute.extensions.internal;

import static com.google.common.collect.Iterables.find;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Named;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Snapshot;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.extensions.VolumeExtension;
import org.jclouds.compute.extensions.options.AttachVolumeOptions;
import org.jclouds.compute.extensions.options.CreateSnapshotOptions;
import org.jclouds.compute.extensions.options.DetachVolumeOptions;
import org.jclouds.compute.extensions.options.VolumeOptions;
import org.jclouds.compute.internal.BaseComputeServiceContextLiveTest;
import org.jclouds.compute.predicates.SnapshotPredicates;
import org.jclouds.compute.predicates.VolumePredicates;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.logging.Logger;
import org.jclouds.rest.ResourceNotFoundException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

/**
 * Base test for {@link VolumeExtension} implementations.
 */
public abstract class BaseVolumeExtensionLiveTest extends BaseComputeServiceContextLiveTest {

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   protected VolumeOptions createOptions;
   protected AttachVolumeOptions attachOptions;
   protected DetachVolumeOptions detachOptions;
   protected CreateSnapshotOptions snapshotOptions;

   protected String volumeId;
   protected String nodeId;
   protected String snapshotId;

   /**
    * Returns the template for the base node, override to test different templates.
    *
    * @return
    */
   public Template getNodeTemplate() {
      return view.getComputeService().templateBuilder().build();
   }

   private VolumeExtension getVolumeExtension() {
      ComputeService computeService = view.getComputeService();

      Optional<VolumeExtension> volumeExtension = computeService.getVolumeExtension();

      assertTrue(volumeExtension.isPresent(), "volume extension was not present");

      return volumeExtension.get();
   }


   @Test(groups = { "integration", "live" }, singleThreaded = true)
   public void testCreateVolume() {
      VolumeExtension volumeExtension = getVolumeExtension();

      Template template = getNodeTemplate();

      createOptions.locationId(template.getLocation().getId());

      Volume volume = volumeExtension.createVolume("test-create-volume", createOptions);

      logger.info("volume created: %s", volume);

      assertNotNull(volume, "Volume is null.");

      assertNotNull(volume.getId(), "Volume ID is null");

      volumeId = volume.getId();
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testCreateVolume")
   public void testGetVolumeById() {
      VolumeExtension volumeExtension = getVolumeExtension();

      Volume volume = volumeExtension.getVolumeById(volumeId);

      logger.info("Volume found: %s", volume);

      assertEquals(volume.getId(), volumeId);
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testGetVolumeById")
   public void testListVolumes() {
      VolumeExtension volumeExtension = getVolumeExtension();

      Set<Volume> volumes = volumeExtension.listVolumes();

      assertNotNull(find(volumes, VolumePredicates.idEquals(volumeId)),
              "Volume with id " + volumeId + " not found in list.");
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testListVolumes")
   public void testListVolumesInLocation() {
      VolumeExtension volumeExtension = getVolumeExtension();

      String locationId = createOptions.getLocationId();

      Set<Volume> volumes = volumeExtension.listVolumesInLocation(locationId);

      assertNotNull(find(volumes, VolumePredicates.idEquals(volumeId)),
              "Volume with id " + volumeId + " not found in list.");
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testListVolumesInLocation")
   public void testAttachVolume() throws RunNodesException {
      VolumeExtension volumeExtension = getVolumeExtension();

      ComputeService computeService = view.getComputeService();

      Template template = getNodeTemplate();

      NodeMetadata node = Iterables.getOnlyElement(computeService.createNodesInGroup("test-attach-volume", 1, template));

      assertNotNull(node, "Node is null");

      nodeId = node.getId();

      assertTrue(volumeExtension.attachVolume(volumeId, nodeId, attachOptions),
              "Volume was not attached");
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testAttachVolume")
   public void testListVolumesForNode() {
      VolumeExtension volumeExtension = getVolumeExtension();

      ComputeService computeService = view.getComputeService();

      NodeMetadata node = computeService.getNodeMetadata(nodeId);

      assertNotNull(node, "No node found for id " + nodeId);

      Set<Volume> volumes = volumeExtension.listVolumesForNode(nodeId);

      assertNotNull(find(volumes, VolumePredicates.idEquals(volumeId)),
              "Volume with id " + volumeId + "not found in list.");
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testListVolumesForNode")
   public void testDetachVolume() {
      VolumeExtension volumeExtension = getVolumeExtension();

      ComputeService computeService = view.getComputeService();

      detachOptions.instanceId(nodeId);

      assertTrue(volumeExtension.detachVolume(volumeId, detachOptions),
              "Failed to detach volume.");

      NodeMetadata node = computeService.getNodeMetadata(nodeId);

      assertNotNull(node, "No node found for id " + nodeId);

      Set<Volume> volumes = volumeExtension.listVolumesForNode(nodeId);

      if (volumes.size() > 0) {
         assertNull(find(volumes, VolumePredicates.idEquals(volumeId)),
                 "Found the volume attached to the node, but shouldn't have.");
      }
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testDetachVolume")
   public void testCreateSnapshot() {
      VolumeExtension volumeExtension = getVolumeExtension();

      Snapshot snapshot = volumeExtension.createSnapshot(volumeId, snapshotOptions);

      assertNotNull(snapshot, "Snapshot is null");

      snapshotId = snapshot.getId();
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testCreateSnapshot")
   public void testGetSnapshotById() {
      VolumeExtension volumeExtension = getVolumeExtension();

      Snapshot snapshot = volumeExtension.getSnapshotById(snapshotId);

      logger.info("Snapshot found: %s", snapshot);

      assertEquals(snapshot.getId(), snapshotId);
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testGetSnapshotById")
   public void testListSnapshots() {
      VolumeExtension volumeExtension = getVolumeExtension();

      Set<Snapshot> snapshots = volumeExtension.listSnapshots();

      assertNotNull(find(snapshots, SnapshotPredicates.idEquals(snapshotId)),
              "Snapshot with id " + snapshotId + " not found in list.");
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testListSnapshots")
   public void testListSnapshotsInLocation() {
      VolumeExtension volumeExtension = getVolumeExtension();

      String locationId = createOptions.getLocationId();

      Set<Snapshot> snapshots = volumeExtension.listSnapshotsInLocation(locationId);

      assertNotNull(find(snapshots, SnapshotPredicates.idEquals(snapshotId)),
              "Snapshot with id " + snapshotId + " not found in list.");
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testListSnapshotsInLocation")
   public void testListSnapshotsForVolume() {
      VolumeExtension volumeExtension = getVolumeExtension();

      Set<Snapshot> snapshots = volumeExtension.listSnapshotsForVolume(volumeId);

      assertNotNull(find(snapshots, SnapshotPredicates.idEquals(snapshotId)),
              "Snapshot with id " + snapshotId + "not found in list.");
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testListSnapshotsForVolume")
   public void testDeleteSnapshot() {
      VolumeExtension volumeExtension = getVolumeExtension();

      if (volumeExtension.getSnapshotById(snapshotId) != null) {
         assertTrue(volumeExtension.deleteSnapshot(snapshotId),
                 "Snapshot was not destroyed.");
      }
   }

   @Test(groups = { "integration", "live" }, singleThreaded = true, dependsOnMethods = "testDeleteSnapshot")
   public void testRemoveVolume() {
      VolumeExtension volumeExtension = getVolumeExtension();

      if (volumeExtension.getVolumeById(volumeId) != null) {
         assertTrue(volumeExtension.removeVolume(volumeId),
                 "Volume was not destroyed.");
      }
   }

   @AfterClass(groups = { "integration", "live" })
   @Override
   protected void tearDownContext() {
      try {
         if (nodeId != null) {
            ComputeService computeService = view.getComputeService();
            computeService.destroyNode(nodeId);
         }

         if (volumeId != null) {
            testRemoveVolume();
         }

         if (snapshotId != null) {
            testDeleteSnapshot();
         }
      } catch (ResourceNotFoundException e) {
         // We really don't care about this.
      }

      super.tearDownContext();
   }
}
