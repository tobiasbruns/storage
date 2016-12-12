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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * created: 26.10.2016
 *
 * @author Tobias Bruns
 */
public class MetaData {

   private Optional<String> contentType = Optional.empty();
   private Collection<MetaDatum> data = Collections.emptyList();

   public Optional<String> getContentType() {
      return contentType;
   }

   public void setContentType(String contentType) {
      this.contentType = Optional.ofNullable(contentType);
   }

   public Collection<MetaDatum> getData() {
      return Collections.unmodifiableCollection(data);
   }

   public void setData(Collection<MetaDatum> data) {
      this.data = data;
   }

}
