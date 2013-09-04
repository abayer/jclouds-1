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

import org.jclouds.compute.domain.Volume;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

/**
 * Container for volume predicates
 *
 *  This class has static methods that create customized predicates to use with
 * {@link org.jclouds.compute.ComputeService}.
 *
 * @author Andrew Bayer
 */
public class VolumePredicates {

   private static final class TypeEqualsPredicate implements Predicate<Volume> {
      private Volume.Type type;

      public TypeEqualsPredicate(Volume.Type type) {
         this.type = type;
      }

      @Override
      public boolean apply(Volume volume) {
         return type.equals(volume.getType());
      }

      @Override
      public String toString() {
         return "typeEquals(" + type + ")";
      }
   }

   /**
    * return everything.
    */
   public static Predicate<Volume> any() {
      return Predicates.<Volume> alwaysTrue();
   }

   /**
    * evaluates true if the Volume id is in the supplied set
    *
    * @param ids
    *           ids of the volumes
    * @return predicate
    */
   public static Predicate<Volume> idIn(Iterable<String> ids) {
      checkNotNull(ids, "ids must be defined");
      final Set<String> search = Sets.newHashSet(ids);
      return new Predicate<Volume>() {
         @Override
         public boolean apply(Volume volume) {
            return search.contains(volume.getId());
         }

         @Override
         public String toString() {
            return "idIn(" + search + ")";
         }
      };
   }

   /**
    * evaluates true if the volume id equals the given id
    *
    * @param id
    *
    * @return predicate
    */
   public static Predicate<Volume> idEquals(final String id) {
      checkNotNull(id, "id must be defined");
      return new Predicate<Volume>() {
         @Override
         public boolean apply(Volume volume) {
            return id.equals(volume.getId());
         }

         @Override
         public String toString() {
            return "idEquals(" + id + ")";
         }
      };
   }

   /**
    * evaluates true if the volume is durable.
    *
    * @return predicate
    */
   public static Predicate<Volume> isDurable() {
      return new Predicate<Volume>() {
         @Override
         public boolean apply(Volume volume) {
            return volume.isDurable();
         }
      };
   }

   /**
    * evaluates true if the volume is the boot device.
    *
    * @return predicate
    */
   public static Predicate<Volume> isBootDevice() {
      return new Predicate<Volume>() {
         @Override
         public boolean apply(Volume volume) {
            return volume.isBootDevice();
         }
      };
   }

   /**
    * evaluates true if the volume's device equals the given device
    *
    * @param device
    *
    * @return predicate
    */
   public static Predicate<Volume> deviceEquals(final String device) {
      checkNotNull(device, "device must be defined");
      return new Predicate<Volume>() {
         @Override
         public boolean apply(Volume volume) {
            return device.equals(volume.getId());
         }

         @Override
         public String toString() {
            return "deviceEquals(" + device + ")";
         }
      };
   }

   /**
    * evaluates true if the volume's type equals the given type
    *
    * @param type
    *
    * @return predicate
    */
   public static Predicate<Volume> typeEquals(Volume.Type type) {
      return new TypeEqualsPredicate(type);
   }

   /**
    * evaluates true if the volume's type is Local.
    *
    * @return predicate
    */
   public static Predicate<Volume> isLocal() {
      return new TypeEqualsPredicate(Volume.Type.LOCAL);
   }

}
