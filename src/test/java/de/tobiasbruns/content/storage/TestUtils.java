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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

/**
 * created: 28.10.2016
 *
 * @author Tobias Bruns
 */
public class TestUtils {

	public static InputStream loadFile(String filename) {
		InputStream dataStream = TestUtils.class.getClassLoader().getResourceAsStream(filename);
		if (dataStream == null) {
			throw new RuntimeException("File " + filename + " not found.");
		}
		return dataStream;
	}

	public static String loadTextFile(String filename) {
		try {
			return IOUtils.toString(loadFile(filename), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static RestDocumentationResultHandler writeDoc(String useCase) {
		return document(useCase, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
	}
}
