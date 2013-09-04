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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.jclouds.ec2.compute.domain.EC2HardwareBuilder.m1_small;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Date;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.domain.VolumeBuilder;
import org.jclouds.ec2.compute.EC2ComputeService;
import org.jclouds.ec2.domain.Attachment;
import org.jclouds.ec2.domain.BlockDevice;
import org.jclouds.ec2.domain.InstanceState;
import org.jclouds.ec2.domain.RootDeviceType;
import org.jclouds.ec2.domain.RunningInstance;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

/**
 * @author Andrew Bayer
 */
@Test(groups = "unit", testName = "EC2VolumeToVolumeTest")
public class EC2VolumeToVolumeTest {

   static EC2ComputeService computeService = createMock(EC2ComputeService.class);

   @Test
   public void testApply() {
      org.jclouds.ec2.domain.Volume origVolume = org.jclouds.ec2.domain.Volume.builder()
              .id("abcd")
              .availabilityZone("us-east-1a")
              .createTime(new Date())
              .region("us-east-1")
              .status(org.jclouds.ec2.domain.Volume.Status.AVAILABLE)
              .size(100)
              .build();

      EC2VolumeToVolume parser = createVolumeParser(computeService);

      Volume volume = parser.apply(origVolume);

      assertEquals(volume.getType(), Volume.Type.SAN);
      assertEquals(volume.getId(), origVolume.getRegion() + "/" + origVolume.getId());
      assertEquals(volume.getLocationId(), origVolume.getRegion());
      assertEquals(volume.getSize().intValue(), origVolume.getSize());
      assertNull(volume.getDevice());
      assertNull(volume.getName());
      assertTrue(volume.isDurable());
      assertFalse(volume.isBootDevice());
   }

   @Test
   public void testApplyAttached() {
      org.jclouds.ec2.domain.Volume origVolume = org.jclouds.ec2.domain.Volume.builder()
              .id("abcd")
              .availabilityZone("us-east-1a")
              .createTime(new Date())
              .region("us-east-1")
              .status(org.jclouds.ec2.domain.Volume.Status.AVAILABLE)
              .size(100)
              .attachments(Attachment.builder()
                      .device("/dev/xvda")
                      .attachTime(new Date())
                      .instanceId("id")
                      .region("us-east-1")
                      .status(Attachment.Status.ATTACHED)
                      .volumeId("abcd")
                      .build())
              .build();

      RunningInstance instance = RunningInstance.builder()
              .instanceId("id")
              .imageId("image")
              .instanceType("m1.small")
              .instanceState(InstanceState.RUNNING)
              .rawState("running")
              .region("us-east-1")
              .ipAddress("10.1.1.1")
              .device("/dev/xvda", new BlockDevice("abcd", Attachment.Status.ATTACHED, new Date(), true))
              .rootDeviceType(RootDeviceType.EBS)
              .rootDeviceName("/dev/xvda")
              .build();

      NodeMetadata node = new NodeMetadataBuilder()
              .status(NodeMetadata.Status.RUNNING)
              .backendStatus("running")
              .publicAddresses(ImmutableSet.<String>of())
              .privateAddresses(ImmutableSet.of("10.1.1.1"))
              .id("us-east-1/id")
              .imageId("us-east-1/image")
              .hardware(m1_small()
                      .volumes(ImmutableSet.of(new VolumeBuilder()
                              .bootDevice(true)
                              .device("/dev/xvda")
                              .id("us-east-1/abcd")
                              .durable(true)
                              .type(Volume.Type.SAN)
                              .build()))
                      .build())
              .providerId("id").build();

      expect(computeService.getNodeMetadata("us-east-1/id")).andReturn(node);

      replay(computeService);

      EC2VolumeToVolume parser = createVolumeParser(computeService);

      Volume volume = parser.apply(origVolume);

      verify(computeService);

      assertEquals(volume.getType(), Volume.Type.SAN);
      assertEquals(volume.getId(), origVolume.getRegion() + "/" + origVolume.getId());
      assertEquals(volume.getLocationId(), origVolume.getRegion());
      assertEquals(volume.getSize().intValue(), origVolume.getSize());
      assertEquals(volume.getDevice(), "/dev/xvda");
      assertNull(volume.getName());
      assertTrue(volume.isDurable());
      assertTrue(volume.isBootDevice());
   }

   private EC2VolumeToVolume createVolumeParser(final EC2ComputeService ec2Compute) {
      EC2VolumeToVolume parser = new EC2VolumeToVolume(ec2Compute);

      return parser;
   }
}
