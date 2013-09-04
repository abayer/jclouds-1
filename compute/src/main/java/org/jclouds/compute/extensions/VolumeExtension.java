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
package org.jclouds.compute.extensions;

import java.util.Set;

import org.jclouds.compute.domain.Snapshot;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.extensions.options.AttachVolumeOptions;
import org.jclouds.compute.extensions.options.CreateSnapshotOptions;
import org.jclouds.compute.extensions.options.DetachVolumeOptions;
import org.jclouds.compute.extensions.options.VolumeOptions;

/**
 * An extension to compute services to provide an abstraction for listing,
 * creating, attaching, detaching and destroying volumes/disks.
 *
 * @author Andrew Bayer
 */
public interface VolumeExtension {

   /**
    * List volumes.
    *
    * @return The set of @{link Volume}s we have access to.
    */
   Set<Volume> listVolumes();

   /**
    * List the volumes in a given @{link Location}.
    *
    * @param locationId The location to search in.
    * @return the set of @{link Volume}s we have access to in the given @{link Location}.
    */
   Set<Volume> listVolumesInLocation(String locationId);

   /**
    * List the volumes associated with a given node.
    *
    * @param id The id of the node to look at.
    * @return the set of @{link Volume}s associated with this node, if any.
    */
   Set<Volume> listVolumesForNode(String id);

   /**
    * Get a volume by its id.
    *
    * @param id The volume's id.
    * @return the volume, or null if not found.
    */
   Volume getVolumeById(String id);

   /**
    * Create a volume with the given name and options.
    *
    * @param name Name for the volume
    * @param options @{link VolumeOptions} for this volume
    * @return the created volume
    */
   Volume createVolume(String name, VolumeOptions options);

   /**
    * Remove a given volume.
    *
    * @param id The id of the volume to remove.
    * @return true if the volume was successfully removed, false otherwise.
    */
   boolean removeVolume(String id);

   /**
    * Attaches a volume to a node.
    *
    * @param volumeId The id of the volume to attach.
    * @param nodeId The id of the node to attach the volume to.
    * @param options Options for attaching the volume.
    * @return True if we've successfully attached the volume, false otherwise.
    */
   boolean attachVolume(String volumeId, String nodeId, AttachVolumeOptions options);

   /**
    * Detaches a volume from a node
    *
    * @param id The id of the volume to detach.
    * @param options Options for detaching the volume.
    *
    * @return true if we've successfully detached the volume, false otherwise.
    */
   boolean detachVolume(String id, DetachVolumeOptions options);

   /**
    * List snapshots.
    *
    * @return The set of @{link Snapshot}s we have access to.
    */
   Set<Snapshot> listSnapshots();

   /**
    * List the snapshots in a given @{link Location}.
    *
    * @param locationId The location to search in.
    * @return the set of @{link Snapshot}s we have access to in the given @{link Location}.
    */
   Set<Snapshot> listSnapshotsInLocation(String locationId);

   /**
    * List the snapshots associated with a given volume.
    *
    * @param id The id of the volume to look at.
    * @return the set of @{link Snapshot}s associated with this volume, if any.
    */
   Set<Snapshot> listSnapshotsForVolume(String id);

   /**
    * Get a snapshot by its id.
    *
    * @param id The snapshot's id.
    * @return the snapshot, or null if not found.
    */
   Snapshot getSnapshotById(String id);

   /**
    * Create a snapshot of a given volume
    *
    * @param volumeId the id of the volume to snapshot
    * @param options Options for the snapshot
    *
    * @return the snapshot we've created
    */
   Snapshot createSnapshot(String volumeId, CreateSnapshotOptions options);


   /**
    * Deletes a given snapshot.
    *
    * @param id The id of the snapshot to delete.
    *
    * @return true if we successfully deleted the snapshot, false otherwise.
    */
   boolean deleteSnapshot(String id);

}
