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

/**
 * created: 25.10.2016
 *
 * @author Tobias Bruns
 */
public class Node {

   private Collection<ContentHeader> children;

   public Collection<ContentHeader> getChildren() {
      return children;
   }

   public void setChildren(Collection<ContentHeader> children) {
      this.children = children;
   }

}
