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

import org.jclouds.compute.extensions.options.DetachVolumeOptions;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * EC2-specific extensions to {@link org.jclouds.compute.extensions.options.DetachVolumeOptions}
 */
public class EC2DetachVolumeOptions extends DetachVolumeOptions implements Cloneable {

   @Override
   public EC2DetachVolumeOptions clone() {
      EC2DetachVolumeOptions options = new EC2DetachVolumeOptions();
      copyTo(options);

      return options;
   }

   @Override
   public void copyTo(DetachVolumeOptions to) {
      super.copyTo(to);

      if (to instanceof EC2DetachVolumeOptions) {
         EC2DetachVolumeOptions eTo = EC2DetachVolumeOptions.class.cast(to);
         eTo.force(force);
      }
   }

   protected boolean force;

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;

      EC2DetachVolumeOptions that = EC2DetachVolumeOptions.class.cast(o);

      return super.equals(that)
              && equal(this.force, that.force);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(super.hashCode(), force);
   }

   @Override
   public ToStringHelper string() {
      ToStringHelper toString = super.string();
      toString.add("force", force);

      return toString;
   }

   /**
    * Specifies whether to force detach.
    */
   public EC2DetachVolumeOptions force(boolean force) {
      this.force = force;
      return this;
   }

   public static class Builder extends DetachVolumeOptions.Builder {
      /**
       * @see EC2DetachVolumeOptions#force
       */
      public static EC2DetachVolumeOptions force(boolean force) {
         EC2DetachVolumeOptions options = new EC2DetachVolumeOptions();
         return options.force(force);
      }

      /**
       * @see org.jclouds.compute.extensions.options.DetachVolumeOptions#instanceId
       */
      public static EC2DetachVolumeOptions instanceId(String instanceId) {
         EC2DetachVolumeOptions options = new EC2DetachVolumeOptions();
         return EC2DetachVolumeOptions.class.cast(options.instanceId(instanceId));
      }

      /**
       * @see org.jclouds.compute.extensions.options.DetachVolumeOptions#device
       */
      public static EC2DetachVolumeOptions device(String device) {
         EC2DetachVolumeOptions options = new EC2DetachVolumeOptions();
         return EC2DetachVolumeOptions.class.cast(options.device(device));
      }
   }

   @Override
   public EC2DetachVolumeOptions instanceId(String instanceId) {
      return EC2DetachVolumeOptions.class.cast(super.instanceId(instanceId));
   }

   @Override
   public EC2DetachVolumeOptions device(String device) {
      return EC2DetachVolumeOptions.class.cast(super.device(device));
   }

   /**
    * @return whether to force-detach the volume
    */
   public boolean shouldForce() {
      return force;
   }

}
