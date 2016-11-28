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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.tobiasbruns.content.storage.ContentHeader.ContentItemType;

/**
 * created: 01.11.2016
 *
 * @author Tobias Bruns
 */
@RunWith(MockitoJUnitRunner.class)
public class WriteContentTest {

	@InjectMocks
	private StorageService service;
	@Mock
	private FileSystemService fsService;
	@Mock
	private MetaDataService metaDataService;
	@Mock
	private ContentService contentService;
	@Mock
	private ContentHeaderService contentHeaderService;

	@Mock
	private File mockFile;
	@Mock
	private ContentHeader header;

	@Before
	public void initTest() {
		when(fsService.getFile(anyString())).thenReturn(mockFile);
		when(contentHeaderService.getContentHeader(anyString())).thenReturn(header);
	}

	@Test
	public void changeFolderName() {
		when(fsService.renameFile(anyString(), anyString())).thenReturn("test/new_name");
		Content<?> folder = buildFolder();
		folder.getHeader().setName("new_name");

		String newPath = service.writeContent("test/path", folder);

		verify(fsService).renameFile("test/path", "new_name");
		verify(metaDataService).writeMetaData(eq("test/new_name"), notNull(MetaData.class));
		verify(metaDataService, never()).renameMetaDataFile(anyString(), anyString());

		assertThat(newPath).isEqualTo("test/new_name");
	}

	@Test
	public void readBinaryContent() {
		when(header.isJsonContent()).thenReturn(false);

		Content<?> content = service.readContent("path/to/content.jpeg");

		verify(contentService).readBinaryData(eq("path/to/content.jpeg"));
		verify(metaDataService).loadMetaData(eq("path/to/content.jpeg"));
		assertThat(content.getHeader()).as("Binary Content Header").isNotNull().isSameAs(header);
	}

	private Content<?> buildFolder() {
		Content<?> content = new Content<>();
		content.getHeader().setType(ContentItemType.NODE);
		return content;
	}
}
