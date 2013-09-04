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
package org.jclouds.compute.extensions.options;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * Volume options supported by {@code VolumeExtension#createVolume}.
 *
 * APIs/providers implementing @{link VolumeExtension} will generally wish to
 * extend VolumeOptions, as is done with @{link TemplateOptions}
 */
public class VolumeOptions implements Cloneable {

   public static class ImmutableVolumeOptions extends VolumeOptions {
      private final VolumeOptions delegate;

      @Override
      public VolumeOptions clone() {
         return delegate.clone();
      }

      @Override
      public Float getSizeGb() {
         return delegate.getSizeGb();
      }

      @Override
      public String getLocationId() {
         return delegate.getLocationId();
      }

      @Override
      public void copyTo(VolumeOptions to) {
         delegate.copyTo(to);
      }

      @Override
      public String toString() {
         return delegate.toString();
      }

      public ImmutableVolumeOptions(VolumeOptions delegate) {
         this.delegate = delegate;
      }
   }

   protected Float sizeGb;

   protected String locationId;

   /**
    * @return Desired disk size, in gigabytes.
    */
   public Float getSizeGb() {
      return sizeGb;
   }

   /**
    * Sets the desired disk size, in gigabytes.
    *
    * @param sizeGb
    *
    * @return this object with the sizeGb set.
    */
   public VolumeOptions sizeGb(Float sizeGb) {
      this.sizeGb = sizeGb;
      return this;
   }

   /**
    * @return the location this volume will be created in.
    */
   public String getLocationId() {
      return locationId;
   }

   /**
    * Sets the location this volume will be created in.
    *
    * @param locationId
    *
    * @return this object with the location set.
    */
   public VolumeOptions locationId(String locationId) {
      this.locationId = locationId;
      return this;
   }

   public static class Builder {

      public static VolumeOptions sizeGb(Float sizeGb) {
         VolumeOptions options = new VolumeOptions();
         return options.sizeGb(sizeGb);
      }

      public static VolumeOptions locationId(String locationId) {
         VolumeOptions options = new VolumeOptions();
         return options.locationId(locationId);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      VolumeOptions that = VolumeOptions.class.cast(o);
      return equal(this.sizeGb, that.sizeGb)
              && equal(this.locationId, that.locationId);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(sizeGb, locationId);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   protected ToStringHelper string() {
      ToStringHelper toString = Objects.toStringHelper("").omitNullValues();
      toString.add("sizeGb", sizeGb);
      toString.add("locationId", locationId);

      return toString;
   }

   @Override
   public VolumeOptions clone() {
      VolumeOptions options = new VolumeOptions();
      copyTo(options);
      return options;
   }

   public void copyTo(VolumeOptions to) {
      if (this.getSizeGb() > 0) {
         to.sizeGb(this.getSizeGb());
      }
      if (this.getLocationId() != null) {
         to.locationId(this.getLocationId());
      }
   }
}
