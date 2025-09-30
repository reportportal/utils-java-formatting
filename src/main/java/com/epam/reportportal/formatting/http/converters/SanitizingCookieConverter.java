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

import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static com.epam.reportportal.formatting.http.converters.DefaultCookieConverter.DEFAULT_COOKIE_DATE_FORMAT;
import static com.epam.reportportal.formatting.http.converters.DefaultCookieConverter.DEFAULT_COOKIE_TIME_ZONE;
import static java.util.Optional.ofNullable;

public class SanitizingCookieConverter implements Function<Cookie, String> {

	public static final Set<String> SESSION_COOKIES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"sid",
			"session",
			"session_id",
			"sessionId",
			"sessionid"
	)));

	public static final Function<Cookie, String> INSTANCE = new SanitizingCookieConverter();

	private final Set<String> sanitizeSet;
	private final Function<Cookie, String> defaultConverter;

	public SanitizingCookieConverter(Set<String> sanitizeCookies, String datesFormat, TimeZone datesTimeZone) {
		sanitizeSet = sanitizeCookies;
		defaultConverter = new DefaultCookieConverter(datesFormat, datesTimeZone);
	}

	public SanitizingCookieConverter(Set<String> sanitizeCookies) {
		this(sanitizeCookies, DEFAULT_COOKIE_DATE_FORMAT, DEFAULT_COOKIE_TIME_ZONE);
	}

	private SanitizingCookieConverter() {
		this(SESSION_COOKIES);
	}

	@Override
	public @Nullable String apply(@Nullable Cookie cookie) {
		return defaultConverter.apply(ofNullable(cookie).filter(c -> sanitizeSet.contains(cookie.getName())).map(c -> {
			Cookie newCookie = c.clone();
			newCookie.setValue(Constants.REMOVED_TAG);
			return newCookie;
		}).orElse(cookie));
	}
}
