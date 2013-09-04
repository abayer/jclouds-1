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

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.jclouds.compute.domain.Snapshot;
import org.testng.annotations.Test;

/**
 * @author Andrew Bayer
 */
@Test(groups = "unit", testName = "EC2SnapshotToSnapshotTest")
public class EC2SnapshotToSnapshotTest {

   @Test
   public void testApply() {
      EC2SnapshotToSnapshot parser = new EC2SnapshotToSnapshot();

      org.jclouds.ec2.domain.Snapshot origSnapshot = new org.jclouds.ec2.domain.Snapshot(
              "us-east-1", "foo", "vol-abcd", 800, org.jclouds.ec2.domain.Snapshot.Status.COMPLETED,
              new Date(), 100, "efgh", null, null
      );

      Snapshot snapshot = parser.apply(origSnapshot);

      assertEquals(snapshot.getLocationId(), origSnapshot.getRegion());
      assertEquals(snapshot.getSize().intValue(), origSnapshot.getVolumeSize());
      assertEquals(snapshot.getCreated(), origSnapshot.getStartTime());
      assertEquals(snapshot.getId(), "us-east-1/foo");
      assertEquals(snapshot.getVolumeId(), "us-east-1/vol-abcd");

   }
}
