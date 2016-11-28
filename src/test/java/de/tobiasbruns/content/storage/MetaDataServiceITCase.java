/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tobiasbruns.content.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * created: 26.10.2016
 *
 * @author Tobias Bruns
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MetaDataServiceITCase {

   @Autowired
   private MetaDataService service;

   @After
   public void cleanup() throws IOException {
      FileUtils.cleanDirectory(new File("testroot"));
   }

   @Test
   public void loadNotExistingData() {
      MetaData metaData = service.loadMetaData("/");

      assertThat(metaData).isNotNull();
   }

   @Test
   public void writeAndLoadData() {
      service.writeMetaData("/", createTestData());

      MetaData loaded = service.loadMetaData("/");
      assertThat(loaded.getData()).hasSize(2).hasOnlyElementsOfType(MetaDatum.class);
   }

   static MetaData createTestData() {
      MetaDatum md1 = new MetaDatum();
      md1.setKey("name");
      md1.setValue("Salami");
      md1.setInherited(false);

      MetaDatum md2 = new MetaDatum();
      md2.setKey("unterlage");
      md2.setValue("Brot");
      md2.setInherited(false);

      MetaData metaData = new MetaData();
      metaData.setData(Arrays.asList(md1, md2));
      metaData.setContentType("application/json");
      return metaData;
   }

}
