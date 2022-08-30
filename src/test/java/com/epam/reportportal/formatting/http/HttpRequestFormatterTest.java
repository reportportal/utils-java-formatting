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

import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.epam.reportportal.formatting.http.Constants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HttpRequestFormatterTest {

	public static final String REQUEST_METHOD = "POST";
	public static final String REQUEST_URL = "https://example.com";
	public static final String TEST_TEXT = "this is a test";

	public static Iterable<Object[]> textBodies() {
		return Arrays.asList(new Object[] { ContentType.APPLICATION_JSON, "{\"object\": {\"key\": \"value\"}}",
						"{\n" + "  \"object\" : {\n" + "    \"key\" : \"value\"\n" + "  }\n" + "}" },
				new Object[] { ContentType.APPLICATION_XML, "<test><key><value>value</value></key></test>",
						"<test>\n" + "  <key>\n" + "    <value>value</value>\n" + "  </key>\n" + "</test>" },
				new Object[] { ContentType.TEXT_HTML, "<html><body><h1>hello world</h1></body></html>",
						"<html>\n  <head></head>\n  <body>\n    <h1>hello world</h1>\n  </body>\n</html>" },
				new Object[] { ContentType.TEXT_PLAIN, "this is a test", "this is a test" }
		);
	}

	private static String getRequestString() {
		return REQUEST_TAG + LINE_DELIMITER + REQUEST_METHOD + " to " + REQUEST_URL + LINE_DELIMITER + LINE_DELIMITER;
	}

	private static String getTextBody(ContentType contentType, String bodyTag, String payload) {
		return getRequestString() + HEADERS_TAG + LINE_DELIMITER + HttpHeaders.CONTENT_TYPE + ": " + contentType
				+ LINE_DELIMITER + LINE_DELIMITER + bodyTag + LINE_DELIMITER + BODY_HIGHLIGHT + LINE_DELIMITER + payload
				+ LINE_DELIMITER + BODY_HIGHLIGHT;
	}

	@ParameterizedTest
	@MethodSource("textBodies")
	public void verify_request_text_body_format(ContentType contentType, String body, String expected) {
		HttpRequestFormatter formatter = new HttpRequestFormatter.Builder(REQUEST_METHOD, REQUEST_URL).addHeader(HttpHeaders.CONTENT_TYPE,
				contentType.toString()
		).bodyText(contentType.getMimeType(), body).build();
		assertThat(formatter.formatAsText(), equalTo(getTextBody(contentType, BODY_TAG, expected)));
	}

	@Test
	public void verify_request_form_body_format() {
		String body = "test1=test1&test2=wefwfqwef%20df%20qwef%20%23%24%25&test3=&F%23%24%25DFFG=dclk%20345%25%2056%20";
		String expectedBody = "test1: test1\ntest2: wefwfqwef df qwef #$%\ntest3: \nF#$%DFFG: dclk 345% 56 ";
		ContentType contentType = ContentType.APPLICATION_FORM_URLENCODED;
		HttpRequestFormatter formatter = new HttpRequestFormatter.Builder(REQUEST_METHOD, REQUEST_URL).addHeader(HttpHeaders.CONTENT_TYPE,
				contentType.toString()
		).bodyParams(body).build();
		assertThat(formatter.formatAsText(), equalTo(getTextBody(contentType, BODY_FORM_TAG, expectedBody)));
	}

	@Test
	public void verify_request_no_header_format() {
		HttpRequestFormatter formatter = new HttpRequestFormatter.Builder(REQUEST_METHOD, REQUEST_URL).bodyText(
				ContentType.TEXT_PLAIN.getMimeType(),
				TEST_TEXT
		).build();
		assertThat(
				formatter.formatAsText(),
				equalTo(getRequestString() + BODY_TAG + LINE_DELIMITER + BODY_HIGHLIGHT + LINE_DELIMITER + TEST_TEXT
						+ LINE_DELIMITER + BODY_HIGHLIGHT)
		);
	}
}
