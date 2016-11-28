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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * created: 27.10.2016
 *
 * @author Tobias Bruns
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs("target/generated-snippets")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ContentControllerITCase {

	@Autowired
	private MockMvc mockMvc;

	private final static String BASE = "http://localhost";

	@BeforeClass
	public static void init() throws IOException {
		cleanup();
	}

	@AfterClass
	public static void cleanup() throws IOException {
		File rootDir = new File("testroot");
		if (rootDir.exists()) {
			FileUtils.cleanDirectory(rootDir);
		}
	}

	@Test
	public void test01_createFolder() throws Exception {
		mockMvc.perform(post(BASE + "/").contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("{\"header\":{\"name\":\"newfolder\",\"type\":\"NODE\"}}")) //
				.andExpect(status().isCreated()) //
				.andExpect(header().string("Location", "http://localhost:8080/newfolder"))
				.andDo(TestUtils.writeDoc("createFolder"));
	}

	@Test
	public void test02_addMetadata() throws Exception {
		mockMvc.perform(put(BASE + "/newfolder").param("projection", "metadata")
				.contentType(MediaType.APPLICATION_JSON_UTF8).content("[" + buildMetaDatumJson("toast", "salami") + ","
						+ buildMetaDatumJson("pizza", "hawaii") + "]"))
				.andExpect(status().isNoContent()).andDo(TestUtils.writeDoc("addMetaData"));
	}

	@Test
	public void test03_readMetaData() throws Exception {
		mockMvc.perform(get(BASE + "/newfolder").param("projection", "metadata"))//
				.andExpect(status().isOk())//
				.andExpect(jsonPath("$.content[0].key", is("toast")))//
				.andExpect(jsonPath("$.content[0].value", is("salami")))//
				.andExpect(jsonPath("$.content", hasSize(2)))//
				.andExpect(jsonPath("$._links.self.href", is("http://localhost:8080/newfolder?projection=metadata")))
				.andDo(TestUtils.writeDoc("readMetaData"));
	}

	@Test
	public void test04_addJsonContent() throws Exception {
		String content = String.format(TestUtils.loadTextFile("requests/jsonContent.json"), "test1.json", "",
				"{\"headline\":\"suppi\"}");

		mockMvc.perform(post(BASE + "/newfolder").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost:8080/newfolder/test1.json"))
				.andDo(TestUtils.writeDoc("addJsonContent"));
	}

	@Test
	public void test05_readJsonContent() throws Exception {
		mockMvc.perform(get(BASE + "/newfolder/test1.json")).andExpect(status().isOk())
				.andExpect(jsonPath("$._links.self.href", is("http://localhost:8080/newfolder/test1.json")))
				.andDo(TestUtils.writeDoc("readJsonContent"));
	}

	@Test
	public void test06_readInheritMetaData() throws Exception {
		mockMvc.perform(get(BASE + "/newfolder/test1.json").param("projection", "metadata"))//
				.andExpect(status().isOk())//
				.andExpect(jsonPath("$.content[0].key", is("toast")))//
				.andExpect(jsonPath("$.content[0].value", is("salami")))//
				.andExpect(jsonPath("$.content[0].inherited", is(true)))//
				.andExpect(jsonPath("$.content[1].key", is("pizza")))//
				.andExpect(jsonPath("$.content[1].value", is("hawaii")))//
				.andExpect(jsonPath("$.content[1].inherited", is(true)));//
	}

	@Test
	public void test07_editJsonContent() throws Exception {
		String content = String.format(TestUtils.loadTextFile("requests/jsonContent.json"), "test1.json",
				buildMetaDatumJson("toast", "schinken"), "{\"headline\":\"Viel besser\"}");

		mockMvc.perform(
				put(BASE + "/newfolder/test1.json").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
				.andExpect(jsonPath("$._links.self.href", is("http://localhost:8080/newfolder/test1.json")))
				.andExpect(status().isOk());

		mockMvc.perform(get(BASE + "/newfolder/test1.json")).andExpect(status().isOk())
				.andExpect(jsonPath("$.metaData.data[0].key", is("toast")))//
				.andExpect(jsonPath("$.metaData.data[0].value", is("schinken")))//
				.andExpect(jsonPath("$.metaData.data[0].inherited", is(false)));//
	}

	@Test
	public void test08_renameFolder() throws Exception {
		mockMvc.perform(put(BASE + "/newfolder").contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("{\"header\":{\"name\":\"folder\",\"type\":\"NODE\"}}")).andExpect(status().isOk()) //
				.andExpect(jsonPath("$._links.self.href", is("http://localhost:8080/folder")));
	}

	@Test
	public void test09_renameFile() throws Exception {
		String content = String.format(TestUtils.loadTextFile("requests/jsonContent.json"), "test2.json", "",
				"{\"headline\":\"Viel besser\"}");

		mockMvc.perform(put(BASE + "/folder/test1.json").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$._links.self.href", is("http://localhost:8080/folder/test2.json")))
				.andDo(TestUtils.writeDoc("renameFile"));

		mockMvc.perform(get(BASE + "/folder/test2.json")).andExpect(status().isOk());
		mockMvc.perform(get(BASE + "/folder/test2.json").param("projection", "metadata")).andExpect(status().isOk());
	}

	@Test
	public void test10_readFolder() throws Exception {
		mockMvc.perform(get(BASE + "/")).andExpect(status().isOk()).andDo(TestUtils.writeDoc("readFolder"));
	}

	@Test
	public void test11_uploadBinaryFile() throws Exception {
		InputStream testStream = TestUtils.loadFile("requests/testimg.jpeg");

		mockMvc.perform(fileUpload(BASE + "/folder")
				.file(new MockMultipartFile("file", "testimage.jpeg", "image/jpeg", testStream)))
				.andExpect(status().isCreated())//
				.andExpect(header().string("Location", "http://localhost:8080/folder/testimage.jpeg"))
				.andDo(TestUtils.writeDoc("addBinaryContent"));
	}

	@Test
	public void test12_loadBinaryFile() throws Exception {
		mockMvc.perform(get(BASE + "/folder/testimage.jpeg")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.IMAGE_JPEG))//
				.andDo(TestUtils.writeDoc("readBinaryContent"));
	}

	@Test
	public void test12_uploadNewBinaryFile() throws Exception {
		InputStream testStream = TestUtils.loadFile("requests/testimg.jpeg");
		MockMultipartFile mockFile = new MockMultipartFile("file", "testimage.jpeg", "image/jpeg", testStream);

		MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
				.fileUpload(BASE + "/folder/testimage.jpeg");
		builder.with(new RequestPostProcessor() {
			@Override
			public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
				request.setMethod("PUT");
				return request;
			}
		});

		mockMvc.perform(builder.file(mockFile)).andExpect(status().is2xxSuccessful());
	}

	static String buildMetaDatumJson(String key, String value) {
		String metaDatumTempl = TestUtils.loadTextFile("requests/metaDatum.json");
		return String.format(metaDatumTempl, key, value);
	}
}
