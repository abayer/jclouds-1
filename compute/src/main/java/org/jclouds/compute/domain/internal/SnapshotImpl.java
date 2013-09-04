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
package org.jclouds.compute.domain.internal;

import static com.google.common.base.Objects.equal;

import java.util.Date;

import org.jclouds.compute.domain.Snapshot;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * @author Andrew Bayer
 */
public class SnapshotImpl implements Snapshot {
   private final String id;
   @Nullable
   private final Float size;
   @Nullable
   private final String name;
   @Nullable
   private final String locationId;
   @Nullable
   private final String volumeId;
   @Nullable
   private final Date created;

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      SnapshotImpl that = SnapshotImpl.class.cast(o);
      return equal(this.id, that.id)
              && equal(this.size, that.size)
              && equal(this.name, that.name)
              && equal(this.locationId, that.locationId)
              && equal(this.volumeId, that.volumeId)
              && equal(this.created, that.created);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(id, size, name, locationId, volumeId, created);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   protected Objects.ToStringHelper string() {
      return Objects.toStringHelper("").omitNullValues()
              .add("id", id)
              .add("size", size)
              .add("name", name)
              .add("locationId", locationId)
              .add("volumeId", volumeId)
              .add("created", created);
   }

   public SnapshotImpl(@Nullable String id, @Nullable Float size, @Nullable String name,
                       @Nullable String locationId, @Nullable String volumeId, @Nullable Date created) {
      this.id = id;
      this.size = size;
      this.name = name;
      this.locationId = locationId;
      this.volumeId = volumeId;
      this.created = created;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getId() {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Float getSize() {
      return size;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName() {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getLocationId() {
      return locationId;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getVolumeId() {
      return volumeId;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Date getCreated() {
      return created;
   }
}


