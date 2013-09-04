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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.jclouds.compute.domain.Snapshot;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.extensions.VolumeExtension;
import org.jclouds.compute.extensions.options.AttachVolumeOptions;
import org.jclouds.compute.extensions.options.CreateSnapshotOptions;
import org.jclouds.ec2.compute.domain.RegionAndName;
import org.jclouds.ec2.compute.extensions.options.EC2DetachVolumeOptions;
import org.jclouds.ec2.compute.extensions.options.EC2VolumeOptions;
import org.jclouds.ec2.compute.internal.BaseEC2ComputeServiceExpectTest;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Test(groups = "unit", testName = "EC2VolumeExtensionExpectTest")
public class EC2VolumeExtensionExpectTest extends BaseEC2ComputeServiceExpectTest {

   protected String volumeId;
   protected String nodeId;
   protected String snapshotId;
   protected String availabilityZone;
   protected String device;

   protected HttpRequest describeVolumesAllRequest;
   protected HttpResponse describeVolumesAllResponse;
   protected HttpRequest describeVolumesNodeRequest;
   protected HttpResponse describeVolumesNodeResponse;
   protected HttpRequest describeSnapshotsAllRequest;
   protected HttpResponse describeSnapshotsAllResponse;
   protected HttpRequest describeSnapshotsVolumeRequest;
   protected HttpRequest describeSnapshotsSingleRequest;
   protected HttpResponse describeSnapshotsSingleResponse;

   @BeforeClass
   @Override
   protected void setupDefaultRequests() {
      super.setupDefaultRequests();
      volumeId = "vol-4282672b";
      nodeId = "i-2baa5550";
      snapshotId = "snap-78a54011";
      availabilityZone = "us-east-1a";
      device = "/dev/sdh";

      describeVolumesAllRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DescribeVolumes").build());

      describeVolumesAllResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_volumes_extension.xml", MediaType.APPLICATION_XML)).build();

      describeSnapshotsAllRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DescribeSnapshots").build());

      describeSnapshotsAllResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_snapshots.xml", MediaType.APPLICATION_XML)).build();

      describeVolumesNodeRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DescribeVolumes")
                      .addFormParam("VolumeId.1", volumeId).build());

      describeVolumesNodeResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_volumes_extension_node.xml", MediaType.APPLICATION_XML)).build();

      describeSnapshotsVolumeRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DescribeSnapshots")
                      .addFormParam("Filter.1.Name", "volume-id")
                      .addFormParam("Filter.1.Value.1", volumeId)
                      .build());

      describeSnapshotsSingleRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DescribeSnapshots")
                      .addFormParam("SnapshotId.1", snapshotId)
                      .build());

      describeSnapshotsSingleResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_snapshots.xml", MediaType.APPLICATION_XML)).build();

      describeInstanceResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_instances_running_volume_extension.xml", MediaType.APPLICATION_XML)).build();

      describeRegionsResponse = HttpResponse.builder().statusCode(200)
              .payload(payloadFromResourceWithContentType("/regionEndpoints-single-region.xml", MediaType.APPLICATION_XML))
              .build();
   }

   public void testListVolumes() {
      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesAllRequest, describeVolumesAllResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Set<Volume> volumes = extension.listVolumes();
      assertEquals(2, volumes.size());
   }

   public void testListSnapshots() {
      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeSnapshotsAllRequest, describeSnapshotsAllResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);
      requestResponseMap.put(describeVolumesAllRequest, describeVolumesAllResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Set<Snapshot> snapshots = extension.listSnapshots();
      assertEquals(1, snapshots.size());
   }

   public void testListVolumesInLocation() {
      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesAllRequest, describeVolumesAllResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Set<Volume> volumes = extension.listVolumesInLocation(region);
      assertEquals(2, volumes.size());
   }

   public void testListSnapshotsInLocation() {
      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeSnapshotsAllRequest, describeSnapshotsAllResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);
      requestResponseMap.put(describeVolumesAllRequest, describeVolumesAllResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Set<Snapshot> snapshots = extension.listSnapshotsInLocation(region);
      assertEquals(1, snapshots.size());
   }

   public void testListVolumesForNode() {
      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesNodeRequest, describeVolumesNodeResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Set<Volume> volumes = extension.listVolumesForNode(nodeId);
      assertEquals(1, volumes.size());
   }

   public void testListSnapshotsForVolume() {
      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesNodeRequest, describeVolumesNodeResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);
      requestResponseMap.put(describeSnapshotsVolumeRequest, describeSnapshotsSingleResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Set<Snapshot> snapshots = extension.listSnapshotsForVolume(volumeId);
      assertEquals(1, snapshots.size());

   }

   public void testGetVolumeById() {
      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesNodeRequest, describeVolumesNodeResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Volume volume = extension.getVolumeById(new RegionAndName(region, volumeId).slashEncode());
      assertEquals(volume.getId(), new RegionAndName(region, volumeId).slashEncode());
      assertEquals(volume.getLocationId(), region);
   }

   public void testGetSnapshotById() {
      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesNodeRequest, describeVolumesNodeResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);
      requestResponseMap.put(describeSnapshotsSingleRequest, describeSnapshotsSingleResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Snapshot snapshot = extension.getSnapshotById(new RegionAndName(region, snapshotId).slashEncode());
      assertEquals(snapshot.getId(), new RegionAndName(region, snapshotId).slashEncode());
      assertEquals(snapshot.getLocationId(), region);

   }

   public void testCreateVolumeEmpty() {
      HttpRequest createVolumeRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "CreateVolume")
                      .addFormParam("AvailabilityZone", availabilityZone)
                      .addFormParam("Size", "1")
                      .build());

      HttpResponse createVolumeResponse =
              HttpResponse.builder()
                      .statusCode(200)
                      .payload(payloadFromResource("/created_volume.xml")).build();

      HttpRequest describeVolumeNewRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DescribeVolumes")
                      .addFormParam("VolumeId.1", "vol-2a21e543").build());

      HttpResponse describeVolumeNewResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_volumes_extension_new_empty.xml", MediaType.APPLICATION_XML)).build();

      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumeNewRequest, describeVolumeNewResponse);
      requestResponseMap.put(createVolumeRequest, createVolumeResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      EC2VolumeOptions options = EC2VolumeOptions.Builder.sizeGb(1f).availabilityZone(availabilityZone);

      Volume volume = extension.createVolume("some-volume", options);

      assertEquals(volume.getLocationId(), region);
      assertEquals(volume.getSize().intValue(), 1);
   }


   public void testCreateVolumeFromSnapshot() {
      HttpRequest createVolumeRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "CreateVolume")
                      .addFormParam("AvailabilityZone", availabilityZone)
                      .addFormParam("SnapshotId", snapshotId)
                      .build());

      HttpResponse createVolumeResponse =
              HttpResponse.builder()
                      .statusCode(200)
                      .payload(payloadFromResource("/created_volume_from_snapshot.xml")).build();

      HttpResponse describeVolumesNewResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_volumes_extension_new_snapshot.xml", MediaType.APPLICATION_XML)).build();


      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesNodeRequest, describeVolumesNewResponse);
      requestResponseMap.put(describeSnapshotsSingleRequest, describeSnapshotsSingleResponse);
      requestResponseMap.put(createVolumeRequest, createVolumeResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      EC2VolumeOptions options = EC2VolumeOptions.Builder.snapshotId(new RegionAndName(region, snapshotId).slashEncode())
              .availabilityZone(availabilityZone);

      Volume volume = extension.createVolume("some-volume", options);

      assertEquals(volume.getLocationId(), region);
      assertEquals(volume.getSize().intValue(), 800);
   }

   public void testCreateSnapshot() {
      HttpRequest createSnapshotRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "CreateSnapshot")
                      .addFormParam("VolumeId", volumeId)
                      .build());

      HttpResponse createSnapshotResponse =
              HttpResponse.builder()
                      .statusCode(200)
                      .payload(payloadFromResource("/created_snapshot_extension.xml")).build();

      HttpResponse describeSnapshotsCreateResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_snapshots_extension_created.xml", MediaType.APPLICATION_XML)).build();

      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesNodeRequest, describeVolumesNodeResponse);
      requestResponseMap.put(describeSnapshotsSingleRequest, describeSnapshotsCreateResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);
      requestResponseMap.put(createSnapshotRequest, createSnapshotResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      Snapshot snapshot = extension.createSnapshot(region + "/" + volumeId, new CreateSnapshotOptions());

      assertEquals(snapshot.getLocationId(), region);
      assertEquals(snapshot.getSize().intValue(), 800);
      assertEquals(snapshot.getVolumeId(), region + "/" + volumeId);
   }

   public void testAttachVolume() {
      HttpRequest attachVolumeRequest  =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "AttachVolume")
                      .addFormParam("VolumeId", volumeId)
                      .addFormParam("InstanceId", nodeId)
                      .addFormParam("Device", device)
                      .build());

      HttpResponse attachVolumeResponse =
              HttpResponse.builder()
                      .statusCode(200)
                      .payload(payloadFromResource("/attach_volume_extension.xml")).build();

      ImmutableMap.Builder<HttpRequest, HttpResponse> requestResponseMap = ImmutableMap.<HttpRequest, HttpResponse> builder();
      requestResponseMap.put(describeRegionsRequest, describeRegionsResponse);
      requestResponseMap.put(describeAvailabilityZonesRequest, describeAvailabilityZonesResponse);
      requestResponseMap.put(describeVolumesNodeRequest, describeVolumesNodeResponse);
      requestResponseMap.put(describeInstanceRequest, describeInstanceResponse);
      requestResponseMap.put(attachVolumeRequest, attachVolumeResponse);

      VolumeExtension extension = requestsSendResponses(requestResponseMap.build()).getVolumeExtension().get();

      assertTrue(extension.attachVolume(region + "/" + volumeId, region + "/" + nodeId, AttachVolumeOptions.Builder.device(device)));
   }

   public void testDetachVolume() {
      HttpRequest detachVolumeRequest  =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DetachVolume")
                      .addFormParam("VolumeId", volumeId)
                      .addFormParam("Device", device)
                      .addFormParam("Force", "false")
                      .addFormParam("InstanceId", nodeId)
                      .build());

      HttpResponse detachVolumeResponse =
              HttpResponse.builder()
                      .statusCode(200)
                      .payload(payloadFromResource("/detach_volume.xml")).build();

      HttpResponse describeVolumesDetachedResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_volumes_extension_detached.xml", MediaType.APPLICATION_XML)).build();

      ImmutableList<HttpRequest> requests = ImmutableList.of(describeRegionsRequest,
              describeVolumesNodeRequest,
              detachVolumeRequest,
              describeVolumesNodeRequest);

      ImmutableList<HttpResponse> responses = ImmutableList.of(describeRegionsResponse,
              describeVolumesNodeResponse,
              detachVolumeResponse,
              describeVolumesDetachedResponse);

      VolumeExtension extension = orderedRequestsSendResponses(requests, responses).getVolumeExtension().get();

      EC2DetachVolumeOptions options = EC2DetachVolumeOptions.Builder.device(device).instanceId(region + "/" + nodeId);

      assertTrue(extension.detachVolume(region + "/" + volumeId, options));
   }

   public void testRemoveVolume() {
      HttpRequest removeVolumeRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DeleteVolume")
                      .addFormParam("VolumeId", volumeId)
                      .build());

      HttpResponse removeVolumeResponse =
              HttpResponse.builder()
                      .statusCode(200)
                      .payload(payloadFromResource("/delete_volume.xml")).build();

      HttpResponse describeVolumesDeletedResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_volumes_extension_deleted.xml", MediaType.APPLICATION_XML)).build();

      ImmutableList<HttpRequest> requests = ImmutableList.of(describeRegionsRequest,
              describeVolumesNodeRequest,
              removeVolumeRequest,
              describeVolumesNodeRequest);

      ImmutableList<HttpResponse> responses = ImmutableList.of(describeRegionsResponse,
              describeVolumesNodeResponse,
              removeVolumeResponse,
              describeVolumesDeletedResponse);

      VolumeExtension extension = orderedRequestsSendResponses(requests, responses).getVolumeExtension().get();

      assertTrue(extension.removeVolume(region + "/" + volumeId));
   }

   public void testDeleteSnapshot() {
      HttpRequest removeSnapshotRequest =
              formSigner.filter(HttpRequest.builder()
                      .method("POST")
                      .endpoint("https://ec2." + region + ".amazonaws.com/")
                      .addHeader("Host", "ec2." + region + ".amazonaws.com")
                      .addFormParam("Action", "DeleteSnapshot")
                      .addFormParam("SnapshotId", snapshotId)
                      .build());

      HttpResponse removeSnapshotResponse =
              HttpResponse.builder()
                      .statusCode(200)
                      .payload(payloadFromResource("/delete_snapshot.xml")).build();

      HttpResponse describeSnapshotsDeletedResponse =
              HttpResponse.builder().statusCode(200)
                      .payload(payloadFromResourceWithContentType(
                              "/describe_snapshots_extension_deleted.xml", MediaType.APPLICATION_XML)).build();

      ImmutableList<HttpRequest> requests = ImmutableList.of(describeRegionsRequest,
              describeSnapshotsSingleRequest,
              removeSnapshotRequest,
              describeSnapshotsSingleRequest);

      ImmutableList<HttpResponse> responses = ImmutableList.of(describeRegionsResponse,
              describeSnapshotsSingleResponse,
              removeSnapshotResponse,
              describeSnapshotsDeletedResponse);

      VolumeExtension extension = orderedRequestsSendResponses(requests, responses).getVolumeExtension().get();

      assertTrue(extension.deleteSnapshot(region + "/" + snapshotId));

   }
}
