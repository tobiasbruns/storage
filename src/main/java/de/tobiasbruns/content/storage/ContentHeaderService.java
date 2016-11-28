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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.tobiasbruns.content.storage.ContentHeader.ContentItemType;
import de.tobiasbruns.content.storage.exception.UnprocessableEntityException;
import de.tobiasbruns.content.storage.exception.UnprocessableEntityException.MessageCode;

/**
 * created: 26.10.2016
 *
 * @author Tobias Bruns
 */
@Service
public class ContentHeaderService {

   @Autowired
   private FileSystemService fsService;
   @Autowired
   private MetaDataService metaDataService;

   public ContentHeader getContentHeader(String path) {
      File file = fsService.getFile(path);

      ContentHeader header = getContentHeader(file);
      if (file.isFile()) {
         header.setContentType(getContentType(path));
      }
      return header;
   }

   public ContentHeader getContentHeader(File file) {
      ContentHeader header = new ContentHeader();
      header.setType(file.isDirectory() ? ContentItemType.NODE : ContentItemType.LEAF);
      header.setName(file.getName());

      if (file.isFile()) {
         header.setSize(file.length());
      }
      return header;
   }

   private String getContentType(String path) {
      return metaDataService.loadMetaData(path).getContentType()
            .orElseThrow(() -> new UnprocessableEntityException(MessageCode.MISSING_CONTENT_TYPE));
   }
}
