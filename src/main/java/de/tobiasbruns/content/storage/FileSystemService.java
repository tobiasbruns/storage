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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.tobiasbruns.content.storage.exception.ResourceNotFoundException;
import de.tobiasbruns.content.storage.exception.ResourceNotFoundException.MessageCode;
import de.tobiasbruns.content.storage.exception.UnprocessableEntityException;

/**
 * created: 26.10.2016
 *
 * @author Tobias Bruns
 */
@Service
public class FileSystemService {

   @Value("${storage.root}")
   private String storageRootDirectory;
   @Value("${storage.delete_on_shutdown}")
   private boolean deleteOnShutdown;

   private File root;

   @PostConstruct
   public void initBean() {
      root = new File(storageRootDirectory);
      root.mkdirs();
      if (deleteOnShutdown) {
         root.deleteOnExit();
      }
   }

   public File getFile(String relativePath) {
      File file = new File(root, relativePath);
      if (file.exists()) {
         return file;
      }
      throw new ResourceNotFoundException(MessageCode.FILE_NOT_FOUND);
   }

   public File getOrCreateFile(String relativePath) {
      try {
         return getFile(relativePath);
      } catch (ResourceNotFoundException e) {
         return createFile("/", relativePath);
      }
   }

   public File[] getDirContent(String relativePath) {
      File dir = getFile(relativePath);
      if (dir.isDirectory()) {
         return dir.listFiles();
      }
      throw new UnprocessableEntityException(UnprocessableEntityException.MessageCode.PATH_NOT_DIR);
   }

   public File createFile(String basePath, String name) {
      File base = getFile(basePath);
      File newFile = new File(base, name);
      try {
         newFile.createNewFile();
      } catch (IOException e) {
         throw new RuntimeException("Error creating File" + newFile.getAbsolutePath(), e);
      }
      return newFile;
   }

   public String renameFile(String oldPath, String newName) {
      File old = getFile(oldPath);
      File newFile = new File(old.getParentFile(), newName);
      //TODO check if newFile exists - error?
      old.renameTo(newFile);
      return newFile.getAbsolutePath().substring(root.getAbsolutePath().length()).replace("\\", "/");
   }

   public File createDirectory(String basePath, String name) {
      File base = getFile(basePath);
      File newDir = new File(base, name);
      newDir.mkdirs();
      return newDir;
   }
}
