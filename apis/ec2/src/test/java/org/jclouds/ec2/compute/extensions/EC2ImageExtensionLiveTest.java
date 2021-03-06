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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Properties;

import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilderSpec;
import org.jclouds.compute.extensions.ImageExtension;
import org.jclouds.compute.extensions.internal.BaseImageExtensionLiveTest;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.testng.annotations.Test;

import com.google.inject.Module;

/**
 * Live test for ec2 {@link ImageExtension} implementation
 * 
 * @author David Alves
 * 
 */
@Test(groups = "live", singleThreaded = true, testName = "EC2ImageExtensionLiveTest")
public class EC2ImageExtensionLiveTest extends BaseImageExtensionLiveTest {
   protected TemplateBuilderSpec ebsTemplate;

   public EC2ImageExtensionLiveTest() {
      provider = "ec2";
   }

   @Override
   protected Properties setupProperties() {
      Properties overrides = super.setupProperties();
      String ebsSpec = checkNotNull(setIfTestSystemPropertyPresent(overrides, provider + ".ebs-template"), provider
              + ".ebs-template");
      ebsTemplate = TemplateBuilderSpec.parse(ebsSpec);
      return overrides;
   }

   @Override
   public Template getNodeTemplate() {
      return view.getComputeService().templateBuilder().from(ebsTemplate).build();
   }

   @Override
   protected Module getSshModule() {
      return new SshjSshClientModule();
   }

}
