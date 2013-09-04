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
 * Options supported by {@code VolumeExtension#attachVolume}.
 *
 * APIs/providers implementing @{link VolumeExtension} will generally wish to
 * extend AttachVolumeOptions, as is done with @{link TemplateOptions}
 */
public class AttachVolumeOptions implements Cloneable {

   public static class ImmutableAttachVolumeOptions extends AttachVolumeOptions {
      private final AttachVolumeOptions delegate;

      @Override
      public AttachVolumeOptions clone() {
         return delegate.clone();
      }

      @Override
      public String getDevice() {
         return delegate.getDevice();
      }

      @Override
      public void copyTo(AttachVolumeOptions to) {
         delegate.copyTo(to);
      }

      @Override
      public String toString() {
         return delegate.toString();
      }

      public ImmutableAttachVolumeOptions(AttachVolumeOptions delegate) {
         this.delegate = delegate;
      }
   }

   protected String device;

   /**
    * @return The device to attach this volume to.
    */
   public String getDevice() {
      return device;
   }

   /**
    * Sets the device to attach the volume to
    *
    * @param device
    *
    * @return this object with the device set.
    */
   public AttachVolumeOptions device(String device) {
      this.device = device;
      return this;
   }

   public static class Builder {

      public static AttachVolumeOptions device(String device) {
         AttachVolumeOptions options = new AttachVolumeOptions();
         return options.device(device);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      AttachVolumeOptions that = AttachVolumeOptions.class.cast(o);
      return equal(this.device, that.device);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(device);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   protected Objects.ToStringHelper string() {
      Objects.ToStringHelper toString = Objects.toStringHelper("").omitNullValues();
      toString.add("device", device);

      return toString;
   }

   @Override
   public AttachVolumeOptions clone() {
      AttachVolumeOptions options = new AttachVolumeOptions();
      copyTo(options);
      return options;
   }

   public void copyTo(AttachVolumeOptions to) {
      if (this.getDevice() != null) {
         to.device(this.getDevice());
      }
   }
}