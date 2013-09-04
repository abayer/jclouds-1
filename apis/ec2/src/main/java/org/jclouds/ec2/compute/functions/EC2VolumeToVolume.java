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
package org.jclouds.ec2.compute.functions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.getFirst;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.domain.VolumeBuilder;
import org.jclouds.compute.predicates.VolumePredicates;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.ec2.compute.domain.RegionAndName;
import org.jclouds.ec2.domain.Attachment;
import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.google.inject.Inject;

/**
 * TODO Tests!
 * A function for transforming an EC2-specific Volume into a generic Volume object.
 *
 * @author Andrew Bayer
 */
@Singleton
public class EC2VolumeToVolume implements Function<org.jclouds.ec2.domain.Volume, Volume> {
   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   protected final ComputeService computeService;

   @Inject
   public EC2VolumeToVolume(ComputeService computeService) {
      this.computeService = checkNotNull(computeService, "computeService");
   }

   @Override
   public Volume apply(org.jclouds.ec2.domain.Volume volume) {
      VolumeBuilder builder = new VolumeBuilder();
      builder.locationId(volume.getRegion());
      builder.type(Volume.Type.SAN);
      builder.id(new RegionAndName(volume.getRegion(), volume.getId()).slashEncode());
      builder.size(new Float(volume.getSize()));
      builder.durable(true);

      // We're only going to look at the first attachment.
      Attachment attach = getFirst(volume.getAttachments(), null);
      if (attach != null) {
         builder.device(attach.getDevice());
         NodeMetadata node = computeService.getNodeMetadata(new RegionAndName(volume.getRegion(),
                 attach.getId()).slashEncode());
         if (node != null && node.getHardware() != null
                 && node.getHardware().getVolumes().size() > 0) {
            builder.bootDevice(any(node.getHardware().getVolumes(),
                    and(VolumePredicates.idEquals(new RegionAndName(volume.getRegion(),
                            volume.getId()).slashEncode()),
                            VolumePredicates.isBootDevice())));
         }
      }

      return builder.build();
   }

}
