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

import java.util.function.Predicate;

import de.tobiasbruns.content.storage.ContentHeader.ContentItemType;

/**
 * created: 25.10.2016
 *
 * @author Tobias Bruns
 */
public class Content<T> {

   static final Predicate<Content<?>> IS_FOLDER = c -> c.getHeader().getType() == ContentItemType.NODE;
   static final Predicate<Content<?>> IS_JSON_CONTENT = IS_FOLDER.negate().and(c -> c.getHeader().isJsonContent());

   private ContentHeader header = new ContentHeader();
   private MetaData metaData = new MetaData();
   private T content;

   public ContentHeader getHeader() {
      return header;
   }

   public void setHeader(ContentHeader header) {
      this.header = header;
   }

   public MetaData getMetaData() {
      return metaData;
   }

   public void setMetaData(MetaData metaData) {
      this.metaData = metaData;
   }

   public T getContent() {
      return content;
   }

   public void setContent(T content) {
      this.content = content;
   }

}
