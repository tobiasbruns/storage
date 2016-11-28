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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * created: 01.11.2016
 *
 * @author Tobias Bruns
 */
@RunWith(MockitoJUnitRunner.class)
public class MetaDataServiceTest {

   @Spy
   @InjectMocks
   private MetaDataService service;
   @Mock
   private File fileMock;
   @Mock
   private FileSystemService fsService;
   @Mock
   private ObjectMapper objectMapper;

   private Collection<MetaDatum> metaData = new ArrayList<>();

   @Before
   public void initTest() throws JsonParseException, JsonMappingException, IOException {
      when(fileMock.isDirectory()).thenReturn(true);
      when(fsService.getFile(anyString())).thenReturn(fileMock);
      when(fsService.getOrCreateFile(anyString())).thenReturn(fileMock);

      MetaData md = new MetaData();
      md.setData(metaData);
      when(objectMapper.readValue(notNull(File.class), eq(MetaData.class))).thenReturn(md);
   }

   @Test
   public void loadInheritedMetaData() {
      service.loadInheritedData("/path/to/content.json");

      verify(service).loadFolderMetaData("/");
      verify(service).loadFolderMetaData("/path");
      verify(service).loadFolderMetaData("/path/to");
      verify(service, never()).loadFolderMetaData("/path/to/content.json");
   }

   @Test
   public void mergeData() {
      List<MetaDatum> metaData =
            Arrays.asList(buildMetaDatum("md1", "val1", true), buildMetaDatum("md2", "val2", true));
      when(service.loadInheritedData(anyString())).thenReturn(new HashSet<>(metaData));

      metaData = Arrays.asList(buildMetaDatum("md1", "val1.1", false), buildMetaDatum("md3", "val3", false));
      MetaData md = new MetaData();
      md.setData(metaData);
      when(service.readMetaData(anyString())).thenReturn(md);

      MetaData loadedMetaData = service.loadMetaData("/content");

      assertThat(loadedMetaData.getData()).hasSize(3);
      List<MetaDatum> data = new ArrayList<>(loadedMetaData.getData());
      Collections.sort(data, Comparator.comparing(MetaDatum::getKey));
      assertThat(data.get(2)).hasFieldOrPropertyWithValue("key", "md3").hasFieldOrPropertyWithValue("value", "val3");
      assertThat(data.get(0)).hasFieldOrPropertyWithValue("key", "md1").hasFieldOrPropertyWithValue("value", "val1.1");

   }

   @Test
   public void loadFolderMetaData() {
      MetaDatum datum = new MetaDatum();
      datum.setKey("test");
      datum.setValue("value");
      metaData.add(datum);

      Set<MetaDatum> result = service.loadFolderMetaData("/path");

      assertThat(result).hasSize(1).first().hasFieldOrPropertyWithValue("inherited", true);
   }

   private MetaDatum buildMetaDatum(String key, String value, boolean inherited) {
      MetaDatum datum = new MetaDatum();
      datum.setKey(key);
      datum.setValue(value);
      datum.setInherited(inherited);
      return datum;
   }

}
