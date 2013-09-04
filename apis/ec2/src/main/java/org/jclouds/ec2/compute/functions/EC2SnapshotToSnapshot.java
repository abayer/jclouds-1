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

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.compute.domain.Snapshot;
import org.jclouds.compute.domain.SnapshotBuilder;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.ec2.compute.domain.RegionAndName;
import org.jclouds.logging.Logger;

import com.google.common.base.Function;

/**
 * A function for transforming an EC2-specific Snapshot into a generic Snapshot object.
 *
 * @author Andrew Bayer
 */
@Singleton
public class EC2SnapshotToSnapshot implements Function<org.jclouds.ec2.domain.Snapshot, Snapshot> {
   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   @Override
   public Snapshot apply(org.jclouds.ec2.domain.Snapshot snapshot) {
      SnapshotBuilder builder = new SnapshotBuilder();
      builder.locationId(snapshot.getRegion());
      builder.id(new RegionAndName(snapshot.getRegion(), snapshot.getId()).slashEncode());
      builder.size(new Float(snapshot.getVolumeSize()));
      builder.created(snapshot.getStartTime());
      builder.volumeId(new RegionAndName(snapshot.getRegion(), snapshot.getVolumeId()).slashEncode());

      return builder.build();
   }

}
