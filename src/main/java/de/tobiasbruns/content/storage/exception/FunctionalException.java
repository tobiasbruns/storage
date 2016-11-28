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
public class FunctionalException extends RuntimeException {

   private static final long serialVersionUID = 1L;
   private final String messageCode;
   private final HttpStatus httpStatus;

   FunctionalException(String message, String messageCode, HttpStatus httpStatus) {
      super(message);
      this.messageCode = messageCode;
      this.httpStatus = httpStatus;
   }

   FunctionalException(String message, String messageCode, HttpStatus httpStatus, Throwable cause) {
      super(message, cause);
      this.messageCode = messageCode;
      this.httpStatus = httpStatus;
   }

   public String getMessageCode() {
      return messageCode;
   }

   public HttpStatus getHttpStatus() {
      return httpStatus;
   }

}
