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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.tobiasbruns.content.storage.ContentHeader.ContentItemType;

/**
 * created: 25.10.2016
 *
 * @author Tobias Bruns
 */
@Service
public class StorageService {

	@Autowired
	private ContentService contentService;
	@Autowired
	private MetaDataService metaDataService;
	@Autowired
	private ContentHeaderService contentHeaderService;
	@Autowired
	private FileSystemService fsService;

	public Content<?> readContent(String path) {
		Content<Object> readedContent = new Content<>();
		ContentHeader header = loadContentHeader(path);
		readedContent.setHeader(header);
		readedContent.setContent(readContent(path, header));
		readedContent.setMetaData(metaDataService.loadMetaData(path));
		return readedContent;
	}

	private Object readContent(String path, ContentHeader header) {
		if (header.isJsonContent()) {
			return contentService.readJsonData(path);
		} else {
			return contentService.readBinaryData(path);
		}
	}

	@SuppressWarnings("unchecked")
	public String writeContent(String path, Content<?> content) {
		path = renameIfNameChanged(path, content);
		if (Content.IS_FOLDER.test(content)) {

		} else if (content.getHeader().isJsonContent()) {
			content.getMetaData().setContentType("application/json");
			contentService.writeJsonData(path, ((Content<Map<String, Object>>) content).getContent());
		} else {
			contentService.writeBinaryData(path, (InputStream) content.getContent());
		}
		metaDataService.writeMetaData(path, content.getMetaData());
		return path;
	}

	private String renameIfNameChanged(String path, Content<?> content) {
		File origFile = fsService.getFile(path);
		if (!StringUtils.equals(origFile.getName(), content.getHeader().getName())) {
			if (content.getHeader().getType() == ContentItemType.LEAF) {
				metaDataService.renameMetaDataFile(path, content.getHeader().getName());
			}
			return fsService.renameFile(path, content.getHeader().getName());
		}
		return path;
	}

	@SuppressWarnings("unchecked")
	public String createContent(String path, Content<?> content) {
		String newPath;
		if (Content.IS_FOLDER.test(content)) {
			newPath = contentService.createFolder(path, content.getHeader().getName());
		} else if (Content.IS_JSON_CONTENT.test(content)) {
			content.getMetaData().setContentType("application/json");
			newPath = contentService.createJsonContent(path, content.getHeader().getName(),
					(Map<String, Object>) content.getContent());
		} else {
			content.getMetaData().setContentType(content.getHeader().getContentType().get());
			newPath = contentService.createBinaryContent(path, content.getHeader().getName(),
					(InputStream) content.getContent());

		}
		metaDataService.writeMetaData(newPath, content.getMetaData());
		return newPath;
	}

	public ContentHeader loadContentHeader(String path) {
		return contentHeaderService.getContentHeader(path);
	}

	public MetaData loadMetaData(String path) {
		return metaDataService.loadMetaData(path);
	}

	public void writeMetaData(String path, Collection<MetaDatum> metaData) {
		MetaData loaded = loadMetaData(path);
		loaded.setData(metaData);
		metaDataService.writeMetaData(path, loaded);
	}

	public InputStream loadContentData(String path) {
		try {
			return new FileInputStream(fsService.getFile(path));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error reading file content", e);
		}
	}
}
