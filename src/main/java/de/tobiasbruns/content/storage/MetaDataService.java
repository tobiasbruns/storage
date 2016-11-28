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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tobiasbruns.content.storage.exception.ResourceNotFoundException;

/**
 * created: 25.10.2016
 *
 * @author Tobias Bruns
 */
@Service
public class MetaDataService {

   @Autowired
   private FileSystemService fsService;
   @Autowired
   private ObjectMapper objectMapper;

   private static final String METADATA_SUFFIX = ".metadata.json";

   public MetaData loadMetaData(String path) {
      Set<MetaDatum> inheritedData = loadInheritedData(path);
      MetaData metaData = readMetaData(path);
      Set<MetaDatum> fileData = new HashSet<>(metaData.getData());
      fileData.addAll(inheritedData);
      metaData.setData(fileData);

      return metaData;
   }

   Set<MetaDatum> loadInheritedData(String basePath) {
      String[] pathElements = basePath.split("/");
      Set<MetaDatum> data = new HashSet<>();
      StringBuilder sb = new StringBuilder("/");

      for (int i = 0; i < pathElements.length - 1; i++) {
         sb.append(pathElements[i]);
         data.addAll(loadFolderMetaData(sb.toString()));
         if (i > 0) {
            sb.append("/");
         }
      }
      return data;
   }

   Set<MetaDatum> loadFolderMetaData(String path) {
      Collection<MetaDatum> metaData = readMetaData(path).getData();
      metaData.forEach(datum -> datum.setInherited(true));
      return new HashSet<>(metaData);
   }

   MetaData readMetaData(String path) {
      try {
         return parseFile(fsService.getFile(buildMetaDataFileName(path)));
      } catch (ResourceNotFoundException e) {
         MetaData data = new MetaData();
         writeMetaData(path, data);
         return data;
      }
   }

   MetaData parseFile(File metaDataFile) {
      Objects.requireNonNull(metaDataFile, "MetaDataFile must not be null");
      try {
         return objectMapper.readValue(metaDataFile, MetaData.class);
      } catch (IOException e) {
         throw new RuntimeException("Error reading Meta-Data File: " + metaDataFile, e);
      }
   }

   public void writeMetaData(String path, MetaData metaData) {
      Objects.requireNonNull(metaData, "MetaData must not be null");

      File metaDataFile = fsService.getOrCreateFile(buildMetaDataFileName(path));
      writeToFile(metaDataFile, metaData);
   }

   public void renameMetaDataFile(String oldPath, String newName) {
      String oldMetaDataPath = buildMetaDataFileNameForFile(oldPath);
      String newMetaDataName = buildMetaDataFileNameForFile(newName);
      fsService.renameFile(oldMetaDataPath, newMetaDataName);
   }

   void writeToFile(File metaDataFile, MetaData metaData) {
      try {
         objectMapper.writeValue(metaDataFile, metaData);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   String buildMetaDataFileName(String origFilePath) {
      if (isDirectory(origFilePath)) {
         return origFilePath + "/" + METADATA_SUFFIX;
      } else {
         return buildMetaDataFileNameForFile(origFilePath);
      }
   }

   String buildMetaDataFileNameForFile(String origFilePath) {
      return origFilePath + METADATA_SUFFIX;
   }

   private boolean isDirectory(String path) {
      return fsService.getFile(path).isDirectory();
   }
}
