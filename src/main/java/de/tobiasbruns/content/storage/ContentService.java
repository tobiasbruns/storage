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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * created: 26.10.2016
 *
 * @author Tobias Bruns
 */
@Service
public class ContentService {

	@Autowired
	private FileSystemService fsService;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ContentHeaderService headerService;

	public void writeJsonData(String path, Map<String, Object> data) {
		try {
			objectMapper.writeValue(fsService.getOrCreateFile(path), data);
		} catch (IOException e) {
			throw new RuntimeException("Error writing json data", e);
		}
	}

	public Map<String, Object> readJsonData(String path) {
		try {
			File file = fsService.getFile(path);

			if (file.isFile()) {
				return objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {
				});
			}
			return readFolder(file);
		} catch (IOException e) {
			throw new RuntimeException("Error when reading json data", e);
		}
	}

	public InputStream readBinaryData(String path) {
		File file = fsService.getFile(path);
		if (file.isFile()) return buildFileInputStream(file);
		throw new RuntimeException("Wrong File-Type found: " + file);
	}

	private InputStream buildFileInputStream(File file) {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error reading Binary Content", e);
		}
	}

	private Map<String, Object> readFolder(File folder) {
		Map<String, Object> result = new HashMap<>();
		List<ContentHeader> headers = new ArrayList<>();
		result.put("sub", headers);

		File[] childFiles = folder.listFiles();
		for (File file : childFiles) {
			headers.add(headerService.getContentHeader(file));
		}

		return result;
	}

	public String createFolder(String path, String name) {
		fsService.createDirectory(path, name);
		return path + "/" + name;
	}

	public String createJsonContent(String path, String name, Map<String, Object> data) {
		String newPath = path + "/" + name;
		writeJsonData(newPath, data);
		return newPath;
	}

	public String createBinaryContent(String path, String name, InputStream data) {
		String newPath = path + "/" + name;
		writeBinaryData(newPath, data);
		return newPath;
	}

	public void writeBinaryData(String newPath, InputStream data) {
		File file = fsService.getOrCreateFile(newPath);
		try (OutputStream out = new FileOutputStream(file)) {
			IOUtils.copy(data, out);
		} catch (IOException e) {
			throw new RuntimeException("Error writing Binary Data.", e);
		} finally {
			IOUtils.closeQuietly(data);
		}
	}

}
