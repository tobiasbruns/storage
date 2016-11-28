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
package de.tobiasbruns.content.storage.exception;

import org.springframework.http.HttpStatus;

/**
 * created: 26.10.2016
 *
 * @author Tobias Bruns
 */
public class ResourceNotFoundException extends FunctionalException {

   private static final long serialVersionUID = 1L;

   public enum MessageCode {
      FILE_NOT_FOUND("File not found", "storage.file.not_found");

      private final String defaultMessage;
      private final String messageCode;

      private MessageCode(String defaultMessage, String messageCode) {
         this.defaultMessage = defaultMessage;
         this.messageCode = messageCode;
      }

      public String getDefaultMessage() {
         return defaultMessage;
      }

      public String getMessageCode() {
         return messageCode;
      }
   }

   public ResourceNotFoundException(MessageCode code) {
      super(code.getDefaultMessage(), code.getMessageCode(), HttpStatus.NOT_FOUND);
   }

   public ResourceNotFoundException(MessageCode code, Throwable cause) {
      super(code.getDefaultMessage(), code.getMessageCode(), HttpStatus.NOT_FOUND, cause);
   }

}
