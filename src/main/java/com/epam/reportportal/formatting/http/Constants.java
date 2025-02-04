/*
 * Copyright 2022 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.formatting.http;

import com.epam.reportportal.formatting.http.entities.BodyType;
import com.epam.reportportal.formatting.http.prettifiers.HtmlPrettifier;
import com.epam.reportportal.formatting.http.prettifiers.JsonPrettifier;
import com.epam.reportportal.formatting.http.prettifiers.XmlPrettifier;
import com.epam.reportportal.utils.http.ContentType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Constants {
	public static final String REMOVED_TAG = "&lt;removed&gt;";
	public static final String LINE_DELIMITER = "\n";
	public static final String HEADERS_TAG = "**Headers**";
	public static final String REQUEST_TAG = "**>>> REQUEST**";
	public static final String RESPONSE_TAG = "**<<< RESPONSE**";
	public static final String BODY_TAG = "**Body**";
	public static final String BODY_FORM_TAG = "**Body form**";
	public static final String COOKIES_TAG = "**Cookies**";
	public static final String BODY_PART_TAG = "**Body part**";
	public static final String BODY_HIGHLIGHT = "```";

	public static final Set<String> MULTIPART_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			ContentType.MULTIPART_FORM_DATA,
			ContentType.MULTIPART_MIXED,
			ContentType.MULTIPART_ALTERNATIVE,
			ContentType.MULTIPART_DIGEST,
			ContentType.MULTIPART_PARALLEL
	)));

	public static final Set<String> TEXT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			ContentType.APPLICATION_JSON,
			ContentType.TEXT_PLAIN,
			ContentType.TEXT_HTML,
			ContentType.TEXT_XML,
			ContentType.APPLICATION_XML,
			ContentType.APPLICATION_SOAP_XML,
			ContentType.APPLICATION_ATOM_XML,
			// Can't use ContentType.TEXT_JSON, etc. because client-java dependency marked as compileOnly
			"text/json",
			"application/x.reportportal.launch.v2+json",
			"application/x.reportportal.test.v2+json"
	)));

	public static final Set<String> FORM_TYPES = Collections.singleton(ContentType.APPLICATION_FORM_URLENCODED);

	public static final Map<String, BodyType> BODY_TYPE_MAP = Collections.unmodifiableMap(Stream.of(
			TEXT_TYPES.stream().collect(Collectors.toMap(k -> k, v -> BodyType.TEXT)),
			FORM_TYPES.stream().collect(Collectors.toMap(k -> k, v -> BodyType.FORM)),
			MULTIPART_TYPES.stream().collect(Collectors.toMap(k -> k, v -> BodyType.MULTIPART))
	).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

	public static final Map<String, Function<String, String>> DEFAULT_PRETTIFIERS = Collections.unmodifiableMap(new HashMap<String, Function<String, String>>() {{
		put(ContentType.APPLICATION_XML, XmlPrettifier.INSTANCE);
		put(ContentType.APPLICATION_SOAP_XML, XmlPrettifier.INSTANCE);
		put(ContentType.APPLICATION_ATOM_XML, XmlPrettifier.INSTANCE);
		put(ContentType.APPLICATION_SVG_XML, XmlPrettifier.INSTANCE);
		put(ContentType.APPLICATION_XHTML_XML, XmlPrettifier.INSTANCE);
		put(ContentType.TEXT_XML, XmlPrettifier.INSTANCE);
		put(ContentType.APPLICATION_JSON, JsonPrettifier.INSTANCE);
		// Can't use ContentType.TEXT_JSON, etc. because client-java dependency marked as compileOnly
		put("text/json", JsonPrettifier.INSTANCE);
		put("application/x.reportportal.launch.v2+json", JsonPrettifier.INSTANCE);
		put("application/x.reportportal.test.v2+json", JsonPrettifier.INSTANCE);
		put(ContentType.TEXT_HTML, HtmlPrettifier.INSTANCE);
	}});

	/**
	 * @deprecated Use {@link #DEFAULT_PRETTIFIERS} instead
	 */
	@Deprecated
	public static final Map<String, Function<String, String>> DEFAULT_PRETTIERS = DEFAULT_PRETTIFIERS;

	private Constants() {
		throw new RuntimeException("No instances should exist for the class!");
	}
}
