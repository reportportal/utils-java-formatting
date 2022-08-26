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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HttpFormatUtilsTest {

	public static Iterable<Object[]> contentTypes() {
		return Arrays.asList(
				new Object[] { "text/html; charset=utf-16", "text/html" },
				new Object[] { "application/json", "application/json" },
				new Object[] { null, "application/octet-stream" }
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
}
