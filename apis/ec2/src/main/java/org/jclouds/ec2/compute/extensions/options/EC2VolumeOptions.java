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
package org.jclouds.ec2.compute.extensions.options;

import static com.google.common.base.Objects.equal;

import org.jclouds.compute.extensions.options.VolumeOptions;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * EC2-specific extensions to {@link VolumeOptions}
 */
public class EC2VolumeOptions extends VolumeOptions implements Cloneable {

   @Override
   public EC2VolumeOptions clone() {
      EC2VolumeOptions options = new EC2VolumeOptions();
      copyTo(options);

      return options;
   }

   @Override
   public void copyTo(VolumeOptions to) {
      super.copyTo(to);

      if (to instanceof EC2VolumeOptions) {
         EC2VolumeOptions eTo = EC2VolumeOptions.class.cast(to);
         if (snapshotId != null) {
            eTo.snapshotId(snapshotId);
         }
         if (availabilityZone != null) {
            eTo.availabilityZone(availabilityZone);
         }
      }
   }

   protected String snapshotId;
   protected String availabilityZone;

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;

      EC2VolumeOptions that = EC2VolumeOptions.class.cast(o);

      return super.equals(that)
              && equal(this.snapshotId, that.snapshotId)
              && equal(this.availabilityZone, that.availabilityZone);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(super.hashCode(), snapshotId, availabilityZone);
   }

   @Override
   public ToStringHelper string() {
      ToStringHelper toString = super.string();
      if (snapshotId != null) {
         toString.add("snapshotId", snapshotId);
      }
      if (availabilityZone != null) {
         toString.add("availabilityZone", availabilityZone);
      }

      return toString;
   }

   /**
    * Specifies the snapshot to create this volume from, if given.
    */
   public EC2VolumeOptions snapshotId(String snapshotId) {
      this.snapshotId = snapshotId;
      return this;
   }

   /**
    * Specifies the availability zone to create this volume in.
    */
   public EC2VolumeOptions availabilityZone(String availabilityZone) {
      this.availabilityZone = availabilityZone;
      return this;
   }

   public static class Builder extends VolumeOptions.Builder {
      /**
       * @see EC2VolumeOptions#snapshotId
       */
      public static EC2VolumeOptions snapshotId(String snapshotId) {
         EC2VolumeOptions options = new EC2VolumeOptions();
         return options.snapshotId(snapshotId);
      }

      /**
       * @see EC2VolumeOptions#availabilityZone
       */
      public static EC2VolumeOptions availabilityZone(String availabilityZone) {
         EC2VolumeOptions options = new EC2VolumeOptions();
         return options.availabilityZone(availabilityZone);
      }

      /**
       * @see VolumeOptions#locationId
       */
      public static EC2VolumeOptions locationId(String locationId) {
         EC2VolumeOptions options = new EC2VolumeOptions();
         return EC2VolumeOptions.class.cast(options.locationId(locationId));
      }

      /**
       * @see VolumeOptions#sizeGb
       */
      public static EC2VolumeOptions sizeGb(Float sizeGb) {
         EC2VolumeOptions options = new EC2VolumeOptions();
         return EC2VolumeOptions.class.cast(options.sizeGb(sizeGb));
      }

   }

   @Override
   public EC2VolumeOptions locationId(String locationId) {
      return EC2VolumeOptions.class.cast(super.locationId(locationId));
   }

   @Override
   public EC2VolumeOptions sizeGb(Float sizeGb) {
      return EC2VolumeOptions.class.cast(super.sizeGb(sizeGb));
   }

   /**
    * @return the snapshot this volume will be created from, if specified.
    */
   public String getSnapshotId() {
      return snapshotId;
   }

   /**
    * @return the availability zone to create this volume in.
    */
   public String getAvailabilityZone() {
      return availabilityZone;
   }

}
