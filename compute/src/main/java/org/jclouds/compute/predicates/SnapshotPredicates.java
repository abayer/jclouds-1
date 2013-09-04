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
package org.jclouds.compute.predicates;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.jclouds.compute.domain.Snapshot;
import org.jclouds.compute.domain.Volume;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

/**
 * Container for snapshot predicates
 *
 *  This class has static methods that create customized predicates to use with
 * {@link org.jclouds.compute.ComputeService}.
 *
 * @author Andrew Bayer
 */
public class SnapshotPredicates {

   /**
    * return everything.
    */
   public static Predicate<Snapshot> any() {
      return Predicates.<Snapshot> alwaysTrue();
   }

   /**
    * evaluates true if the Snapshot id is in the supplied set
    *
    * @param ids
    *           ids of the Snapshot
    * @return predicate
    */
   public static Predicate<Snapshot> idIn(Iterable<String> ids) {
      checkNotNull(ids, "ids must be defined");
      final Set<String> search = Sets.newHashSet(ids);
      return new Predicate<Snapshot>() {
         @Override
         public boolean apply(Snapshot snapshot) {
            return search.contains(snapshot.getId());
         }

         @Override
         public String toString() {
            return "idIn(" + search + ")";
         }
      };
   }

   /**
    * evaluates true if the snapshot id equals the given id
    *
    * @param id
    *
    * @return predicate
    */
   public static Predicate<Snapshot> idEquals(final String id) {
      checkNotNull(id, "id must be defined");
      return new Predicate<Snapshot>() {
         @Override
         public boolean apply(Snapshot snapshot) {
            return id.equals(snapshot.getId());
         }

         @Override
         public String toString() {
            return "idEquals(" + id + ")";
         }
      };
   }

   /**
    * evaluates true if the volume of the snapshot is in the supplied set
    *
    * @param volumes
    *           volumes
    * @return predicate
    */
   public static Predicate<Snapshot> volumeIn(Iterable<Volume> volumes) {
      checkNotNull(volumes, "volumes must be defined");
      final Set<Volume> search = Sets.newHashSet(volumes);
      return new Predicate<Snapshot>() {
         @Override
         public boolean apply(Snapshot snapshot) {
            return search.contains(snapshot.getVolumeId());
         }

         @Override
         public String toString() {
            return "volumeIn(" + search + ")";
         }
      };
   }

   /**
    * evaluates true if the snapshot's volume equals the given volume
    *
    * @param volume
    *
    * @return predicate
    */
   public static Predicate<Snapshot> volumeEquals(final Volume volume) {
      checkNotNull(volume, "volume must be defined");
      return new Predicate<Snapshot>() {
         @Override
         public boolean apply(Snapshot snapshot) {
            return volume.equals(snapshot.getVolumeId());
         }

         @Override
         public String toString() {
            return "volumeEquals(" + volume + ")";
         }
      };
   }
}
