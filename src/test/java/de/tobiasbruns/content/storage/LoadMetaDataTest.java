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
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * created: 26.10.2016
 *
 * @author Tobias Bruns
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadMetaDataTest {

   @Spy
   @InjectMocks
   private MetaDataService metaDataService;
   @Mock
   private FileSystemService fsService;

   private static final String TEST_FILE_NAME = "testfilename";

   @Before
   public void initTest() {
      File mockFile = mockFile();
      when(fsService.getFile(TEST_FILE_NAME + ".metadata.json")).thenReturn(mockFile);
      when(fsService.getFile(TEST_FILE_NAME + "/.metadata.json")).thenReturn(mockFile);

      doReturn(new MetaData()).when(metaDataService).parseFile(notNull(File.class));

   }

   @Test
   public void fromFile() {
      File mockFile = mockFile();
      when(fsService.getFile(TEST_FILE_NAME)).thenReturn(mockFile);

      metaDataService.loadMetaData(TEST_FILE_NAME);

      verify(fsService).getFile(eq(TEST_FILE_NAME + ".metadata.json"));
      verify(metaDataService).parseFile(notNull(File.class));
   }

   @Test
   public void fromDirectory() {
      File mockDirectory = mockDirectory();
      when(fsService.getFile(TEST_FILE_NAME)).thenReturn(mockDirectory);

      metaDataService.loadMetaData(TEST_FILE_NAME);

      verify(fsService).getFile(eq(TEST_FILE_NAME + "/.metadata.json"));
      verify(metaDataService).parseFile(notNull(File.class));
   }

   @Test
   public void buildMetaDataFileNameForFile() {
      File mockFile = mockFile();
      when(fsService.getFile(TEST_FILE_NAME)).thenReturn(mockFile);

      String fileName = metaDataService.buildMetaDataFileName(TEST_FILE_NAME);

      assertThat(fileName).isEqualTo(TEST_FILE_NAME + ".metadata.json");
   }

   @Test
   public void buildMetaDataFileNameForDir() {
      File mockDirectory = mockDirectory();
      when(fsService.getFile(TEST_FILE_NAME)).thenReturn(mockDirectory);

      String fileName = metaDataService.buildMetaDataFileName(TEST_FILE_NAME);

      assertThat(fileName).isEqualTo(TEST_FILE_NAME + "/.metadata.json");
   }

   private File mockDirectory() {
      File fileMock = mock(File.class);
      when(fileMock.isDirectory()).thenReturn(true);
      return fileMock;
   }

   private File mockFile() {
      File fileMock = mock(File.class);
      when(fileMock.isDirectory()).thenReturn(false);
      return fileMock;
   }
}
