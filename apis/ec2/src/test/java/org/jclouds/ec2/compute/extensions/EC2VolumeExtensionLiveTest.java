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
package org.jclouds.ec2.compute.extensions;

import org.jclouds.compute.domain.Template;
import org.jclouds.compute.extensions.internal.BaseVolumeExtensionLiveTest;
import org.jclouds.compute.extensions.options.AttachVolumeOptions;
import org.jclouds.compute.extensions.options.CreateSnapshotOptions;
import org.jclouds.domain.Location;
import org.jclouds.domain.LocationScope;
import org.jclouds.ec2.compute.extensions.options.EC2DetachVolumeOptions;
import org.jclouds.ec2.compute.extensions.options.EC2VolumeOptions;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Live test for ec2 {@link org.jclouds.compute.extensions.VolumeExtension} implementation
 *
 * @author Andrew Bayer
 *
 */
@Test(groups = "live", singleThreaded = true, testName = "EC2VolumeExtensionLiveTest")
public class EC2VolumeExtensionLiveTest extends BaseVolumeExtensionLiveTest {

   public EC2VolumeExtensionLiveTest() {
      provider = "ec2";

      attachOptions = AttachVolumeOptions.Builder.device("/dev/sdh");

      detachOptions = EC2DetachVolumeOptions.Builder.device("/dev/sdh");

      snapshotOptions = CreateSnapshotOptions.Builder.name("");

   }

   @Override
   public Template getNodeTemplate() {
      final Template template = view.getComputeService().templateBuilder().build();

      Location zone = Iterables.find(view.getComputeService().listAssignableLocations(), new Predicate<Location>() {

         @Override
         public boolean apply(Location arg0) {
            return arg0.getScope() == LocationScope.ZONE
                    && arg0.getParent().getId().equals(template.getLocation().getId());
         }

      });

      createOptions = EC2VolumeOptions.Builder.availabilityZone(zone.getId()).sizeGb(1f);

      return view.getComputeService().templateBuilder().fromTemplate(template).locationId(zone.getId()).build();
   }
}
