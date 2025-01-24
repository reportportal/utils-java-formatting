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

package com.epam.reportportal.formatting.http.prettiers;

import com.epam.reportportal.formatting.http.prettifiers.XmlPrettifier;

import javax.xml.transform.OutputKeys;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated Use {@link XmlPrettifier} instead.
 */
public class XmlPrettier extends XmlPrettifier {
	private static final int DEFAULT_INDENT = 2;
	private static final Map<String, String> DEFAULT_OUTPUT_PROPERTIES = new HashMap<String, String>() {{
		put(OutputKeys.ENCODING, "UTF-8");
		put(OutputKeys.OMIT_XML_DECLARATION, "yes");
		put(OutputKeys.INDENT, "yes");
	}};

	public XmlPrettier(int indent, Map<String, String> outputSettings) {
		super(indent, outputSettings);
	}

	private XmlPrettier() {
		this(DEFAULT_INDENT, DEFAULT_OUTPUT_PROPERTIES);
	}
}
