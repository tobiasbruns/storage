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

import static de.tobiasbruns.content.storage.ContentControllerITCase.buildMetaDatumJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tobiasbruns.content.storage.ContentHeader.ContentItemType;

/**
 * created: 25.10.2016
 *
 * @author Tobias Bruns
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentControllerTest {

	@InjectMocks
	private ContentController controller;
	@Mock
	private StorageService service;
	@Mock
	private HttpServletResponse response;
	@Mock
	private ObjectMapper mapper;

	@Captor
	private ArgumentCaptor<Content<?>> contentCaptor;

	private MockMvc mockMvc;

	private static final String PATH_TO_CONTENT = "/path/to/content.file";
	private static final String REQUEST = "http://localhost" + PATH_TO_CONTENT;

	@Before
	public void initTest() throws IOException {
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

		when(service.readContent(anyString())).thenReturn(buildTestContent());
		when(service.createContent(anyString(), any(Content.class))).thenReturn("/path/to/new/content");

		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
	}

	private Content buildTestContent() {
		Content content = new Content<>();
		content.getHeader().setContentType("application/json");
		return content;
	}

	@Test
	public void loadContent() throws Exception {
		mockMvc.perform(get(REQUEST))//
				.andExpect(status().isOk())//
				.andExpect(content().contentType("application/hal+json"));

		verify(service).readContent(eq(PATH_TO_CONTENT));
	}

	@Test
	public void loadMetaData() throws Exception {
		when(service.loadMetaData(anyString())).thenReturn(new MetaData());

		mockMvc.perform(get(REQUEST).param("projection", "metadata")).andExpect(status().isOk());

		verify(service).loadMetaData(eq(PATH_TO_CONTENT));
	}

	@Test
	public void loadContentData() throws Exception {
		when(service.loadContentData(anyString())).thenReturn(new ByteArrayInputStream("test data".getBytes()));
		when(service.loadContentHeader(anyString())).thenReturn(buildLeafHeader("images/jpeg"));

		mockMvc.perform(get(REQUEST).param("projection", "content")).andExpect(status().isOk()) //
				.andExpect(content().bytes("test data".getBytes())) //
				.andExpect(content().contentType("images/jpeg"));

		verify(service).loadContentData(eq(PATH_TO_CONTENT));
		verify(service).loadContentHeader(anyString());
	}

	@Test
	public void loadContentDataOfDirectory() throws Exception {
		when(service.loadContentHeader(anyString())).thenReturn(buildNodeHeader());

		mockMvc.perform(get(REQUEST).param("projection", "content")).andExpect(status().isUnprocessableEntity());
	}

	@Test
	public void missingContentType() throws Exception {
		when(service.loadContentData(anyString())).thenReturn(new ByteArrayInputStream("test data".getBytes()));
		when(service.loadContentHeader(anyString())).thenReturn(buildLeafHeader(null));

		mockMvc.perform(get(REQUEST).param("projection", "content")).andExpect(status().isOk()) //
				.andExpect(content().contentType("application/octet-stream"));
	}

	@Test
	public void createFolder() throws Exception {
		mockMvc.perform(post(REQUEST).contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(String.format(TestUtils.loadTextFile("requests/createFolder.json"), "new_folder")))
				.andExpect(status().isCreated())
				.andExpect(header().stringValues("Location", "http://localhost/path/to/new/content"));

		verify(service).createContent(eq(PATH_TO_CONTENT), contentCaptor.capture());
		assertThat(contentCaptor.getValue().getHeader().getName()).isEqualTo("new_folder");
	}

	@Test
	public void addJsonContent() throws Exception {
		String content = String.format(TestUtils.loadTextFile("requests/jsonContent.json"), "test1", "",
				"{\"headline\":\"Best\"}");

		mockMvc.perform(post(REQUEST).contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
				.andExpect(status().isCreated())
				.andExpect(header().stringValues("Location", "http://localhost/path/to/new/content"));

		verify(service).createContent(eq("/path/to/content.file"), any(Content.class));
	}

	@Test
	public void addBinaryContent() throws Exception {
		InputStream testStream = TestUtils.loadFile("requests/testimg.jpeg");

		mockMvc.perform(
				fileUpload(REQUEST).file(new MockMultipartFile("file", "testimg.jpeg", "images/jpeg", testStream)))//
				.andExpect(status().isCreated())
				.andExpect(header().stringValues("Location", "http://localhost/path/to/new/content"));

		verify(service).createContent(eq("/path/to/content.file"), contentCaptor.capture());
		ContentHeader header = contentCaptor.getValue().getHeader();
		assertThat(header.getContentType().isPresent()).isTrue();
		assertThat(header.getContentType().get()).isEqualTo("images/jpeg");
		assertThat(header.getName()).isEqualTo("testimg.jpeg");
	}

	@Test
	public void changeMetaData() throws Exception {
		mockMvc.perform(put(REQUEST).param("projection", "metadata")//
				.contentType(MediaType.APPLICATION_JSON_UTF8)//
				.content("[" + buildMetaDatumJson("toast", "salami") + "," + buildMetaDatumJson("pizza", "hawaii")
						+ "]"))
				.andExpect(status().isNoContent());

		verify(service).writeMetaData(eq(PATH_TO_CONTENT), anyCollectionOf(MetaDatum.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void changeJsonContent() throws Exception {
		String content = String.format(TestUtils.loadTextFile("requests/jsonContent.json"), "test1.json", "",
				"{\"headline\":\"Much better\"}");

		mockMvc.perform(put(REQUEST)//
				.contentType(MediaType.APPLICATION_JSON_UTF8).content(content))//
				.andExpect(jsonPath("$.header.name", is("test1.json"))).andExpect(status().isOk());

		verify(service).writeContent(eq(PATH_TO_CONTENT), contentCaptor.capture());
		assertThat(contentCaptor.getValue().getContent()).isInstanceOf(Map.class);
		assertThat((Map<String, Object>) contentCaptor.getValue().getContent()).containsEntry("headline",
				"Much better");
	}

	private ContentHeader buildLeafHeader(String contentType) {
		ContentHeader header = new ContentHeader();
		if (contentType != null) {
			header.setContentType(contentType);
		}
		header.setType(ContentItemType.LEAF);
		return header;
	}

	private ContentHeader buildNodeHeader() {
		ContentHeader header = new ContentHeader();
		header.setType(ContentItemType.NODE);
		return header;
	}
}
