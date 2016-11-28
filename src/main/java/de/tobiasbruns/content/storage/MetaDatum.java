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

import javax.validation.constraints.NotNull;

/**
 * created: 25.10.2016
 *
 * @author Tobias Bruns
 */
public class MetaDatum {

   @NotNull
   private String key;
   @NotNull
   private String value;
   private boolean inherited;

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public boolean isInherited() {
      return inherited;
   }

   public void setInherited(boolean inherited) {
      this.inherited = inherited;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      MetaDatum other = (MetaDatum) obj;
      if (key == null) {
         if (other.key != null) {
            return false;
         }
      } else if (!key.equals(other.key)) {
         return false;
      }
      return true;
   }

}
