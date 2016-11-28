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

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import de.tobiasbruns.content.storage.exception.ResourceNotFoundException;
import de.tobiasbruns.content.storage.exception.UnprocessableEntityException;

/**
 * created: 26.10.2016
 *
 * @author Tobias Bruns
 */
@RunWith(MockitoJUnitRunner.class)
public class FileSystemServiceTest {

   @InjectMocks
   private FileSystemService service;

   private String testRootDir = "testRootDir";
   private File testRoot = new File(testRootDir);

   @Before
   public void initTest() {
      testRoot.delete();
      testRoot.mkdir();
      setRootDir(testRootDir);
   }

   @After
   public void cleanup() throws IOException {
      FileUtils.deleteDirectory(testRoot);
   }

   @Test
   public void createRootDir() {
      String rootDir = testRootDir + "/test/dir";
      setRootDir(rootDir);

      assertThat(new File(rootDir).exists()).isTrue();
   }

   @Test
   public void getFile() throws IOException {
      String fileName = "testfile";
      File testFile = new File(testRoot, fileName);
      testFile.createNewFile();

      File loadedFile = service.getFile(fileName);

      assertThat(loadedFile.exists()).isTrue();
      assertThat(loadedFile).isEqualTo(testFile);
   }

   @Test(expected = ResourceNotFoundException.class)
   public void getFileNotExists() {
      service.getFile("not_exists.txt");
   }

   @Test
   public void createMissingFile() {
      File file = service.getOrCreateFile("not_exists.txt");

      assertThat(file).isNotNull().exists();
   }

   @Test
   public void getDirContent() throws IOException {
      String dirName = "testDir";
      File testDir = new File(testRoot, dirName);
      testDir.mkdir();
      String fileName = "testFile";
      File testFile = new File(testDir, fileName);
      testFile.createNewFile();

      File[] dirContent = service.getDirContent(dirName);

      assertThat(dirContent).hasSize(1).contains(testFile);
   }

   @Test(expected = UnprocessableEntityException.class)
   public void getDirContentNotADir() throws IOException {
      String fileName = "testfile";
      File testFile = new File(testRoot, fileName);
      testFile.createNewFile();

      service.getDirContent(fileName);
   }

   @Test
   public void renameFile() {
      service.getOrCreateFile("/renameTestFile");

      String newPath = service.renameFile("/renameTestFile", "newName");

      assertThat(newPath).isEqualTo("/newName");
   }

   private void setRootDir(String rootDir) {
      ReflectionTestUtils.setField(service, "storageRootDirectory", rootDir);
      service.initBean();
   }
}
