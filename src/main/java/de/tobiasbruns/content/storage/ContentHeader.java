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

import java.util.Optional;

import org.springframework.util.MimeType;

import de.tobiasbruns.content.storage.exception.UnprocessableEntityException;
import de.tobiasbruns.content.storage.exception.UnprocessableEntityException.MessageCode;

/**
 * created: 25.10.2016
 *
 * @author Tobias Bruns
 */
public class ContentHeader {

	enum ContentItemType {
		LEAF, NODE;
	}

	private String name;
	private Optional<String> contentType = Optional.empty();
	private Optional<Long> size = Optional.empty();
	private ContentItemType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Optional<String> getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = Optional.of(contentType);
	}

	public ContentItemType getType() {
		return type;
	}

	public void setType(ContentItemType type) {
		this.type = type;
	}

	public Optional<Long> getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = Optional.of(size);
	}

	boolean isJsonContent() {
		if (type == ContentItemType.NODE) {
			return true;
		}

		MimeType mimeType = MimeType.valueOf(
				contentType.orElseThrow(() -> new UnprocessableEntityException(MessageCode.MISSING_CONTENT_TYPE)));
		if (mimeType.isCompatibleWith(MimeType.valueOf("application/json"))) {
			return true;
		}
		return false;
	}

	boolean isBinaryContent() {
		return !isJsonContent();
	}
}
