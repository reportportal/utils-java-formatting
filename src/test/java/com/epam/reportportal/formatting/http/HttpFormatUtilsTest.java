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
import com.epam.reportportal.formatting.http.entities.Cookie;
import com.epam.reportportal.formatting.http.entities.Header;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HttpFormatUtilsTest {

	public static Iterable<Object[]> contentTypes() {
		return Arrays.asList(
				new Object[] { "text/html; charset=utf-16", "text/html" },
				new Object[] { "application/json", "application/json" },
				new Object[] { null, "application/octet-stream" },
				new Object[] { "application/x-www-form-urlencoded; charset=ISO-8859-1", "application/x-www-form-urlencoded" }
		);
	}

	@ParameterizedTest
	@MethodSource("contentTypes")
	public void testGetMimeType(String contentType, String result) {
		assertThat(HttpFormatUtils.getMimeType(contentType), equalTo(result));
	}

	public static final String DELIMITER = "; ";

	public static Iterable<Object[]> joinParts() {
		return Arrays.asList(
				new Object[] { null, "" },
				new Object[] { new String[] { "1", "2", "3" }, "1" + DELIMITER + "2" + DELIMITER + "3" },
				new Object[] { new String[] { "1", null, "3" }, "1" + DELIMITER + "3" },
				new Object[] { new String[] { "1", "", "3" }, "1" + DELIMITER + "3" },
				new Object[] { new String[] { "1", "2", "" }, "1" + DELIMITER + "2" }
		);
	}

	@ParameterizedTest
	@MethodSource("joinParts")
	public void testJoinParts(String[] parts, String result) {
		assertThat(HttpFormatUtils.joinParts(DELIMITER, parts), equalTo(result));
	}

	public static Iterable<Object[]> bodyTypes() {
		return Arrays.asList(
				new Object[] { "text/html; charset=utf-16", BodyType.TEXT },
				new Object[] { "application/json", BodyType.TEXT },
				new Object[] { null, BodyType.NONE },
				new Object[] { "application/x-www-form-urlencoded; charset=ISO-8859-1", BodyType.FORM },
				new Object[] { "image/jpeg", BodyType.BINARY },
				new Object[] { "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW", BodyType.MULTIPART },
				new Object[] { "", BodyType.NONE }
		);
	}

	@ParameterizedTest
	@MethodSource("bodyTypes")
	public void testGetBodyType(String contentType, BodyType expected) {
		assertThat(HttpFormatUtils.getBodyType(contentType), equalTo(expected));
	}

	public static Iterable<Object[]> headerValues() {
		return Arrays.asList(
				new Object[] { "Content-Type: multipart/form-data; boundary=LjalBsLkAYGgsOfzTPStiqo8-Ur9wnV9", "Content-Type",
						"multipart/form-data; boundary=LjalBsLkAYGgsOfzTPStiqo8-Ur9wnV9" },
				new Object[] { "Host: docker.local:8080", "Host", "docker.local:8080" },
				new Object[] { "X-Custom-Header: a: b", "X-Custom-Header", "a: b" },
				new Object[] { "X-Custom-Header: ", "X-Custom-Header", "" },
				new Object[] { "X-Custom-Header", "X-Custom-Header", "" },
				new Object[] { "", "", "" }
		);
	}

	@ParameterizedTest
	@MethodSource("headerValues")
	public void testToHeader(String headerLine, String expectedKey, String expectedValue) {
		Header header = HttpFormatUtils.toHeader(headerLine);
		assertThat(header.getName(), equalTo(expectedKey));
		assertThat(header.getValue(), equalTo(expectedValue));
	}

	public static final String DATE_STR = "Tue, 06 Sep 2022 09:32:51 UTC";
	public static final Calendar DATE_CAL = new GregorianCalendar(2022, Calendar.SEPTEMBER, 6, 9, 32, 51);

	static {
		DATE_CAL.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static Iterable<Object[]> cookieValues() {
		return Arrays.asList(
				new Object[] { "test=value", "test", "value", null, null, false, false },
				new Object[] { "test=value; expires=" + DATE_STR + "; path=/; secure; httponly", "test", "value", DATE_CAL.getTime(), "/",
						true, true }
		);
	}

	@ParameterizedTest
	@MethodSource("cookieValues")
	public void testToCookie(String headerLine, String name, String value, Date date, String path, boolean secure, boolean http) {
		Cookie cookie = HttpFormatUtils.toCookie(headerLine);
		assertThat(cookie.getName(), equalTo(name));
		assertThat(cookie.getValue(), equalTo(value));
		assertThat(cookie.getValue(), equalTo(value));
		assertThat(cookie.getExpiryDate(), equalTo(date));
		assertThat(cookie.getPath(), equalTo(path));
		assertThat(cookie.getSecured(), equalTo(secure));
		assertThat(cookie.getHttpOnly(), equalTo(http));
	}

	public static Iterable<Object[]> cookieHeaders() {
		return Arrays.asList(new Object[] { "cookie", true }, new Object[] { "Cookie", true }, new Object[] { "Cook", false });
	}

	@ParameterizedTest
	@MethodSource("cookieHeaders")
	public void testIsCookie(String header, boolean expectedResult) {
		assertThat(HttpFormatUtils.isCookie(header), equalTo(expectedResult));
	}

	public static Iterable<Object[]> setCookieHeaders() {
		return Arrays.asList(new Object[] { "set-cookie", true }, new Object[] { "Set-Cookie", true }, new Object[] { "setcookie", false });
	}

	@ParameterizedTest
	@MethodSource("setCookieHeaders")
	public void testIsSetCookie(String header, boolean expectedResult) {
		assertThat(HttpFormatUtils.isSetCookie(header), equalTo(expectedResult));
	}
}
