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
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.AfterClass;
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
public class StorageServiceITCase {

   @Autowired
   private StorageService service;

   @AfterClass
   public static void cleanup() throws IOException {
      FileUtils.cleanDirectory(new File("testroot"));
   }

   @Test
   public void writeAndLoadContent() {
      service.createContent("/", createTestJsonContent());

      Content<?> content = service.readContent("/testcontent.json");

      assertThat(content).isNotNull();
      assertThat(content.getHeader().isJsonContent()).isTrue();
   }

   @Test
   public void loadDirectory() {
      Content<?> content = service.readContent("/");

      assertThat(content).isNotNull();
      assertThat(content.getHeader().isJsonContent()).isTrue();
   }

   private Content<Map<String, Object>> createTestJsonContent() {
      Content<Map<String, Object>> content = new Content<>();
      content.setMetaData(MetaDataServiceITCase.createTestData());
      content.setHeader(createJsonHeader());
      content.getHeader().setName("testcontent.json");

      Map<String, Object> data = new HashMap<>();
      data.put("val1", "Lorem Ipsum");
      data.put("val int", 42);
      data.put("bool", true);
      content.setContent(data);

      return content;
   }

   private ContentHeader createJsonHeader() {
      ContentHeader header = new ContentHeader();
      header.setContentType("application/json");
      return header;
   }

}
