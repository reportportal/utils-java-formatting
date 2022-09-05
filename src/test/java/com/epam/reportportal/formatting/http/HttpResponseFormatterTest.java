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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.epam.reportportal.formatting.http.Constants.LINE_DELIMITER;
import static com.epam.reportportal.formatting.http.Constants.RESPONSE_TAG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HttpResponseFormatterTest {

	public static final String STATUS_LINE = "HTTP/1.1 201";

	public static Iterable<Object[]> responseStatuses() {
		return Arrays.asList(
				new Object[] { 200, null, RESPONSE_TAG + LINE_DELIMITER + 200 },
				new Object[] { 200, STATUS_LINE, RESPONSE_TAG + LINE_DELIMITER + STATUS_LINE },
				new Object[] { 200, "", RESPONSE_TAG + LINE_DELIMITER + 200 }
		);
	}

	@ParameterizedTest
	@MethodSource("responseStatuses")
	public void test_response_title_format(int statusCode, String phrase, String expected) {
		assertThat(new HttpResponseFormatter(statusCode, phrase).formatTitle(), equalTo(expected));
	}

}
