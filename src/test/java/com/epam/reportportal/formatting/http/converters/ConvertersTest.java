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

package com.epam.reportportal.formatting.http.converters;

import com.epam.reportportal.formatting.http.Constants;
import com.epam.reportportal.formatting.http.entities.Cookie;
import com.epam.reportportal.formatting.http.entities.Header;
import com.epam.reportportal.formatting.http.prettifiers.HtmlPrettifier;
import com.epam.reportportal.formatting.http.prettifiers.JsonPrettifier;
import com.epam.reportportal.formatting.http.prettifiers.Prettifier;
import com.epam.reportportal.formatting.http.prettifiers.XmlPrettifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ConvertersTest {

	public static Iterable<Object[]> cookieCases() {
		Cookie cookie1 = new Cookie("session_id");
		cookie1.setValue("my_test_session_id");
		cookie1.setComment("test comment");
		cookie1.setDomain("example.com");
		cookie1.setHttpOnly(true);
		cookie1.setPath("/");

		Cookie cookie2 = new Cookie("test_cookie");
		cookie2.setValue("my_test_session_id");
		cookie2.setPath("/");

		return Arrays.asList(
				new Object[] { cookie1,
						"session_id: " + Constants.REMOVED_TAG + "; Comment=test comment; Path=/; Domain=example.com; HttpOnly=true" },
				new Object[] { null, null },
				new Object[] { cookie2, "test_cookie: my_test_session_id; Path=/" }
		);
	}

	@ParameterizedTest
	@MethodSource("cookieCases")
	public void testSessionIdHeaderRemove(Cookie input, String expected) {
		assertThat(SanitizingCookieConverter.INSTANCE.apply(input), equalTo(expected));
	}

	public static Iterable<Object[]> headerCases() {
		return Arrays.asList(
				new Object[] { new Header("Authorization", "Bearer test_token"), "Authorization: " + Constants.REMOVED_TAG },
				new Object[] { null, null },
				new Object[] { new Header("Accept", "*/*"), "Accept: \\*/\\*" }
		);
	}

	@ParameterizedTest
	@MethodSource("headerCases")
	public void testAuthorizationHeaderRemove(Header input, String expected) {
		assertThat(SanitizingHttpHeaderConverter.INSTANCE.apply(input), equalTo(expected));
	}

	public static Iterable<Object[]> uriCases() {
		return Arrays.asList(
				new Object[] { "://my-invalid-uri", "://my-invalid-uri" },
				new Object[] { "https://test:password@example.com/my/api",
						"https://test:" + Constants.REMOVED_TAG + "@example.com/my/api" },
				new Object[] { null, null },
				new Object[] { "https://test@example.com/my/api", "https://test@example.com/my/api" }
		);
	}

	@ParameterizedTest
	@MethodSource("uriCases")
	public void testUriSanitizingConverter(String input, String expected) {
		assertThat(SanitizingUriConverter.INSTANCE.apply(input), equalTo(expected));
	}

	public static Iterable<Object[]> prettierData() {
		return Arrays.asList(
				new Object[] { JsonPrettifier.INSTANCE, "{\"object\": {\"key\": \"value\"}}",
						"{\n  \"object\" : {\n    \"key\" : \"value\"\n  }\n}" },
				new Object[] { XmlPrettifier.INSTANCE, "<test><key><value>value</value></key></test>",
						"<test>\n  <key>\n    <value>value</value>\n  </key>\n</test>" },
				new Object[] { HtmlPrettifier.INSTANCE, "<html><body><h1>hello world</h1></body></html>",
						"<html>\n  <head></head>\n  <body>\n    <h1>hello world</h1>\n  </body>\n</html>" },
				new Object[] { JsonPrettifier.INSTANCE, "^$V\\B#$^", "^$V\\B#$^" },
				new Object[] { XmlPrettifier.INSTANCE, "^$V\\B#$^", "^$V\\B#$^" },
				new Object[] { HtmlPrettifier.INSTANCE, "^$V\\B#$\"^",
						"<html>\n  <head></head>\n  <body>\n    ^$V\\B#$\"^\n  </body>\n</html>" },
				new Object[] { HtmlPrettifier.INSTANCE, "</", "<html>\n  <head></head>\n  <body>\n    &lt;/\n  </body>\n</html>" },
				new Object[] { HtmlPrettifier.INSTANCE, "", "<html>\n  <head></head>\n  <body></body>\n</html>" },
				new Object[] { JsonPrettifier.INSTANCE, null, null },
				new Object[] { HtmlPrettifier.INSTANCE, null, null },
				new Object[] { HtmlPrettifier.INSTANCE, null, null }
		);
	}

	@ParameterizedTest
	@MethodSource("prettierData")
	public void test_prettifier(Prettifier prettier, String input, String expected) {
		assertThat(prettier.apply(input), equalTo(expected));
	}
}
