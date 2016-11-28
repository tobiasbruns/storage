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

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * created: 06.12.2016
 *
 * @author Tobias Bruns
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateContentTest {

	@InjectMocks
	private StorageService service;
	@Mock
	private ContentService contentService;
	@Mock
	private MetaDataService metaDataService;

	@Before
	public void initTest() {
		when(contentService.createBinaryContent(anyString(), anyString(), any(InputStream.class)))
				.thenReturn("new/Path/content.json");
	}

	@Test
	public void createBinaryContent() {
		Content<InputStream> content = new Content<>();
		content.setContent(mock(InputStream.class));
		content.getHeader().setContentType("images/jpeg");
		content.getHeader().setName("image.jpeg");

		String newPath = service.createContent("/path/to/content", content);

		verify(contentService).createBinaryContent(eq("/path/to/content"), eq("image.jpeg"), any(InputStream.class));
		verify(metaDataService).writeMetaData(eq("new/Path/content.json"), any(MetaData.class));
		assertThat(newPath).isNotNull().isEqualTo("new/Path/content.json");

	}
}
