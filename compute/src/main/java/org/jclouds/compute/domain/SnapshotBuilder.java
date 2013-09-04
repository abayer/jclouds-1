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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import org.jclouds.compute.domain.internal.SnapshotImpl;
import org.jclouds.javax.annotation.Nullable;

/**
 * @author Andrew Bayer
 */
public class SnapshotBuilder {

   private String id;
   @Nullable
   private Float size;
   @Nullable
   private String name;
   @Nullable
   private String locationId;
   @Nullable
   private String volumeId;
   @Nullable
   private Date created;

   public SnapshotBuilder id(String id) {
      this.id = checkNotNull(id, "id");
      return this;
   }

   public SnapshotBuilder size(Float size) {
      this.size = size;
      return this;
   }

   public SnapshotBuilder name(String name) {
      this.name = name;
      return this;
   }

   public SnapshotBuilder locationId(String locationId) {
      this.locationId = locationId;
      return this;
   }

   public SnapshotBuilder volumeId(String volumeId) {
      this.volumeId = volumeId;
      return this;
   }

   public SnapshotBuilder created(Date created) {
      this.created = created;
      return this;
   }

   public Snapshot build() {
      return new SnapshotImpl(id, size, name, locationId, volumeId, created);
   }

   public static Snapshot fromSnapshot(Snapshot in) {
      return new SnapshotImpl(in.getId(), in.getSize(), in.getName(), in.getLocationId(),
              in.getVolumeId(), in.getCreated());
   }

}
