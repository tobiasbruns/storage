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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tobiasbruns.content.storage.ContentHeader.ContentItemType;

/**
 * created: 25.10.2016
 * 
 * @author Tobias Bruns
 */
@RestController
@RequestMapping(path = "/**")
public class ContentController {

	@Autowired
	private StorageService service;
	@Autowired
	private ObjectMapper mapper;

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Resource<Content<?>> loadContent(HttpServletRequest req, UriComponentsBuilder uriBuilder) {
		Content<?> content = service.readContent(getPath(req));

		return new Resource<>(content, currentContentSelfLink(uriBuilder, req));
	}

	@RequestMapping(method = RequestMethod.GET)
	public void readBinaryContent(HttpServletRequest req, UriComponentsBuilder uriBuilder,
			HttpServletResponse response) {
		Content<?> content = service.readContent(getPath(req));
		if (content.getHeader().isJsonContent()) {
			writeJsonContent((Content<Map<String, Object>>) content, uriBuilder, req, response);
		} else
			writeBinaryContent((Content<InputStream>) content, response);
	}

	private void writeBinaryContent(Content<InputStream> content, HttpServletResponse response) {
		content.getHeader().getContentType().ifPresent(response::setContentType);
		content.getHeader().getSize().ifPresent(response::setContentLengthLong);
		try {
			IOUtils.copy(content.getContent(), response.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeJsonContent(Content<Map<String, Object>> content, UriComponentsBuilder uriBuilder,
			HttpServletRequest req, HttpServletResponse response) {
		response.setContentType("application/hal+json");
		Resource<Content<Map<String, Object>>> result = new Resource<>(content,
				currentContentSelfLink(uriBuilder, req));
		try {
			mapper.writeValue(response.getOutputStream(), result);
		} catch (IOException e) {
			throw new RuntimeException("Error writing json content", e);
		}
	}

	@RequestMapping(method = RequestMethod.GET, params = "projection=metadata", produces = "application/json")
	public @ResponseBody Resource<MetaDatum[]> loadMetadata(HttpServletRequest req, UriComponentsBuilder uriBuilder) {

		Link selfLink = new Link(uriBuilder.path(getPath(req)).queryParam("projection", "metadata").toUriString());

		Collection<MetaDatum> metaData = service.loadMetaData(getPath(req)).getData();
		return new Resource<>(metaData.toArray(new MetaDatum[metaData.size()]), selfLink);
	}

	@RequestMapping(method = RequestMethod.GET, params = "projection=content")
	public void loadContentData(HttpServletRequest req, HttpServletResponse response) throws IOException {
		ContentHeader header = service.loadContentHeader(getPath(req));

		if (header.getType() == ContentItemType.NODE) {
			response.sendError(HttpStatus.UNPROCESSABLE_ENTITY.value());
			return;
		}

		response.setContentType(header.getContentType().orElse("application/octet-stream"));
		IOUtils.copy(service.loadContentData(getPath(req)), response.getOutputStream());
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(code = HttpStatus.CREATED)
	public HttpEntity<?> createJsonContent(HttpServletRequest req, @RequestBody Content<Map<String, Object>> content,
			UriComponentsBuilder uriBuilder) {
		setContentTypeIfEmpty(content, "application/json");
		String newPath = service.createContent(getPath(req), content);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", uriBuilder.path(newPath).toUriString());
		return new HttpEntity<>(headers);
	}

	private void setContentTypeIfEmpty(Content<?> content, String contentType) {
		if (!content.getHeader().getContentType().isPresent()) {
			content.getHeader().setContentType(contentType);
		}
	}

	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
	@ResponseStatus(code = HttpStatus.CREATED)
	public HttpEntity<?> createBinaryContent(HttpServletRequest req, @RequestParam("file") MultipartFile file,
			UriComponentsBuilder uriBuilder) {
		String path = service.createContent(getPath(req), buildContent(file));

		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", uriBuilder.path(path).toUriString());
		return new HttpEntity<>(headers);
	}

	private Content<InputStream> buildContent(MultipartFile file) {
		try {
			Content<InputStream> content = new Content<>();
			content.setContent(file.getInputStream());
			content.getHeader().setContentType(file.getContentType());
			content.getHeader().setName(file.getOriginalFilename());
			return content;
		} catch (IOException e) {
			throw new RuntimeException("Error reading incoming file", e);
		}
	}

	@RequestMapping(method = RequestMethod.PUT)
	public Resource<Content<?>> changeContent(HttpServletRequest req, @RequestBody Content<Map<String, Object>> content,
			UriComponentsBuilder uriBuilder) {
		String path = service.writeContent(getPath(req), content);
		return new Resource<>(content, currentContentSelfLink(uriBuilder, path));
	}

	@RequestMapping(method = RequestMethod.PUT, consumes = "multipart/form-data")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void changeBinaryContent(HttpServletRequest req, @RequestParam("file") MultipartFile file,
			UriComponentsBuilder uriBuilder) {
		service.writeContent(getPath(req), buildContent(file));
	}

	@RequestMapping(method = RequestMethod.PUT, params = "projection=metadata")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void changeMetaData(HttpServletRequest req, @RequestBody Collection<MetaDatum> metaData) {
		service.writeMetaData(getPath(req), metaData);
	}

	private String getPath(HttpServletRequest req) {
		return req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
	}

	private Link currentContentSelfLink(UriComponentsBuilder uriBuilder, HttpServletRequest req) {
		return currentContentSelfLink(uriBuilder, getPath(req));
	}

	private Link currentContentSelfLink(UriComponentsBuilder uriBuilder, String path) {
		return new Link(uriBuilder.path(path).toUriString()).withSelfRel();
	}
}
