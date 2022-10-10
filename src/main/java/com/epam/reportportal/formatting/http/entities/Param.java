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

package com.epam.reportportal.formatting.http.entities;

import javax.annotation.Nonnull;

public class Param implements Cloneable {

	private final String name;
	private String value;

	public Param(@Nonnull String paramName, @Nonnull String paramValue) {
		name = paramName;
		value = paramValue;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Nonnull
	public String getValue() {
		return value;
	}

	public void setValue(@Nonnull String value) {
		this.value = value;
	}

	@Nonnull
	@Override
	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public Param clone() {
		return new Param(name, value);
	}
}
