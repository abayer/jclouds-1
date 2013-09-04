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
package org.jclouds.ec2.predicates;

import javax.annotation.Resource;
import javax.inject.Singleton;

import org.jclouds.ec2.domain.Volume;
import org.jclouds.ec2.features.ElasticBlockStoreApi;
import org.jclouds.logging.Logger;
import org.jclouds.rest.ResourceNotFoundException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * 
 * Tests to see if a volume is deleted.
 * 
 * @author Adrian Cole
 * @author Andrew Bayer
 */
@Singleton
public class VolumeDeleted implements Predicate<Volume> {

   private final ElasticBlockStoreApi client;
   @Resource
   protected Logger logger = Logger.NULL;

   @Inject
   public VolumeDeleted(ElasticBlockStoreApi client) {
      this.client = client;
   }

   public boolean apply(Volume volume) {
      logger.trace("looking for status on volume %s", volume.getId());
      try {
         volume = Iterables.getOnlyElement(client.describeVolumesInRegion(volume.getRegion(), volume
                 .getId()));
         logger.trace("%s: looking for status %s: currently: %s", volume, Volume.Status.DELETED,
               volume.getStatus());
         return volume.getStatus() == Volume.Status.DELETED;
      } catch (ResourceNotFoundException e) {
         // This means the volume's already thoroughly deleted, so we return true anyway.
         return true;
      }
   }

}
