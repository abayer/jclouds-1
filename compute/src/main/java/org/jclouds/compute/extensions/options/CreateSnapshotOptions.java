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
package org.jclouds.compute.extensions.options;


import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

/**
 * Options supported by {@code VolumeExtension#createSnapshot}.
 *
 * APIs/providers implementing @{link VolumeExtension} will generally wish to
 * extend CreateSnapshotOptions, as is done with @{link TemplateOptions}
 */
public class CreateSnapshotOptions implements Cloneable {

   public static class ImmutableCreateSnapshotOptions extends CreateSnapshotOptions {
      private final CreateSnapshotOptions delegate;

      @Override
      public CreateSnapshotOptions clone() {
         return delegate.clone();
      }

      @Override
      public String getName() {
         return delegate.getName();
      }

      @Override
      public void copyTo(CreateSnapshotOptions to) {
         delegate.copyTo(to);
      }

      @Override
      public String toString() {
         return delegate.toString();
      }

      public ImmutableCreateSnapshotOptions(CreateSnapshotOptions delegate) {
         this.delegate = delegate;
      }
   }

   protected String name;

   /**
    * @return The name for this snapshot.
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name for this snapshot.
    *
    * @param name
    *
    * @return this object with the name set.
    */
   public CreateSnapshotOptions name(String name) {
      this.name = name;
      return this;
   }

   public static class Builder {

      public static CreateSnapshotOptions name(String name) {
         CreateSnapshotOptions options = new CreateSnapshotOptions();
         return options.name(name);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      CreateSnapshotOptions that = CreateSnapshotOptions.class.cast(o);
      return equal(this.name, that.name);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(name);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   protected Objects.ToStringHelper string() {
      Objects.ToStringHelper toString = Objects.toStringHelper("").omitNullValues();
      toString.add("name", name);

      return toString;
   }

   @Override
   public CreateSnapshotOptions clone() {
      CreateSnapshotOptions options = new CreateSnapshotOptions();
      copyTo(options);
      return options;
   }

   public void copyTo(CreateSnapshotOptions to) {
      if (this.getName() != null) {
         to.name(this.getName());
      }
   }
}