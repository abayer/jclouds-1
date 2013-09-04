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

/**
 * Options supported by {@code VolumeExtension#detachVolume}.
 *
 * APIs/providers implementing @{link VolumeExtension} will generally wish to
 * extend DetachVolumeOptions, as is done with @{link TemplateOptions}
 */
public class DetachVolumeOptions implements Cloneable {

   public static class ImmutableDetachVolumeOptions extends DetachVolumeOptions {
      private final DetachVolumeOptions delegate;

      @Override
      public DetachVolumeOptions clone() {
         return delegate.clone();
      }

      @Override
      public String getDevice() {
         return delegate.getDevice();
      }

      @Override
      public String getInstanceId() {
         return delegate.getInstanceId();
      }

      @Override
      public void copyTo(DetachVolumeOptions to) {
         delegate.copyTo(to);
      }

      @Override
      public String toString() {
         return delegate.toString();
      }

      public ImmutableDetachVolumeOptions(DetachVolumeOptions delegate) {
         this.delegate = delegate;
      }
   }

   protected String device;
   protected String instanceId;

   /**
    * @return The device this volume is currently attached on.
    */
   public String getDevice() {
      return device;
   }

   /**
    * @return the ID of the instance to detach this volume from.
    */
   public String getInstanceId() {
      return instanceId;
   }

   /**
    * Sets the device this volume is currently attached on.
    *
    * @param device
    *
    * @return this object with the device set.
    */
   public DetachVolumeOptions device(String device) {
      this.device = device;
      return this;
   }

   /**
    * Sets the ID of the instance to detach this volume from.
    *
    * @param instanceId
    *
    * @return this object with the instance ID set.
    */
   public DetachVolumeOptions instanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
   }

   public static class Builder {

      public static DetachVolumeOptions device(String device) {
         DetachVolumeOptions options = new DetachVolumeOptions();
         return options.device(device);
      }

      public static DetachVolumeOptions instanceId(String instanceId) {
         DetachVolumeOptions options = new DetachVolumeOptions();
         return options.instanceId(instanceId);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      DetachVolumeOptions that = DetachVolumeOptions.class.cast(o);
      return equal(this.device, that.device)
              && equal(this.instanceId, that.instanceId);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(device, instanceId);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   protected Objects.ToStringHelper string() {
      Objects.ToStringHelper toString = Objects.toStringHelper("").omitNullValues();
      toString.add("device", device);
      toString.add("instanceId", instanceId);

      return toString;
   }

   @Override
   public DetachVolumeOptions clone() {
      DetachVolumeOptions options = new DetachVolumeOptions();
      copyTo(options);
      return options;
   }

   public void copyTo(DetachVolumeOptions to) {
      if (this.getDevice() != null) {
         to.device(this.getDevice());
      }
   }
}