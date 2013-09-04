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
package org.jclouds.compute.domain;

import java.util.Date;

import org.jclouds.compute.domain.internal.SnapshotImpl;
import org.jclouds.javax.annotation.Nullable;

import com.google.inject.ImplementedBy;

/**
 * Describes a snapshot of a {@link Volume}
 *
 * @author Andrew Bayer
 */
@ImplementedBy(SnapshotImpl.class)
public interface Snapshot {

   /**
    * Unique identifier.
    *
    */
   @Nullable
   String getId();

   /**
    * @return size of the snapshot in gigabytes, if available
    *
    */
   @Nullable
   Float getSize();

   /**
    *
    * @return the name of the snapshot, if available.
    */
   @Nullable
   String getName();

   /**
    * @return the location for this snapshot, if available
    */
   @Nullable
   String getLocationId();

   /**
    * @return the volume associated with this snapshot, if available
    */
   @Nullable
   String getVolumeId();

   /**
    * @return the creation time of this snapshot, if available
    */
   @Nullable
   Date getCreated();

}
