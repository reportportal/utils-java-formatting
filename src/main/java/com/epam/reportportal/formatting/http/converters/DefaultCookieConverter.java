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

import com.epam.reportportal.formatting.http.entities.Cookie;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class DefaultCookieConverter implements Function<Cookie, String> {
	public static final String DEFAULT_COOKIE_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

	public static final TimeZone DEFAULT_COOKIE_TIME_ZONE = TimeZone.getTimeZone(ZoneId.of("UTC"));

	private static final int UNDEFINED = -1;
	private static final String COMMENT = "Comment";
	private static final String PATH = "Path";
	private static final String DOMAIN = "Domain";
	private static final String MAX_AGE = "Max-Age";
	private static final String SECURE = "Secure";
	private static final String HTTP_ONLY = "HttpOnly";
	private static final String EXPIRES = "Expires";
	private static final String VERSION = "Version";
	private static final String SAME_SITE = "SameSite";

	private static final String ATTRIBUTE_SEPARATOR = "; ";
	private static final String ATTRIBUTE_VALUE = "=";
	private static final String NAME_DELIMITER = ": ";

	public static final Function<Cookie, String> INSTANCE = new DefaultCookieConverter();

	private final String dateFormat;
	private final TimeZone timeZone;

	public DefaultCookieConverter(@Nonnull String datesFormat, @Nonnull TimeZone datesTimeZone) {
		dateFormat = datesFormat;
		timeZone = datesTimeZone;
	}

	public DefaultCookieConverter() {
		this(DEFAULT_COOKIE_DATE_FORMAT, DEFAULT_COOKIE_TIME_ZONE);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public @Nullable String apply(@Nullable Cookie cookie) {
		return ofNullable(cookie).map(c -> {
			List<String> cookieValues = new ArrayList<>();
			ofNullable(c.getValue()).ifPresent(cookieValues::add);
			ofNullable(c.getComment()).ifPresent(comment -> cookieValues.add(COMMENT + ATTRIBUTE_VALUE + comment));
			ofNullable(c.getPath()).ifPresent(path -> cookieValues.add(PATH + ATTRIBUTE_VALUE + path));
			ofNullable(c.getDomain()).ifPresent(domain -> cookieValues.add(DOMAIN + ATTRIBUTE_VALUE + domain));
			ofNullable(c.getMaxAge()).filter(m -> m != UNDEFINED).ifPresent(maxAge -> cookieValues.add(MAX_AGE + ATTRIBUTE_VALUE + maxAge));
			ofNullable(c.getSecured()).filter(s -> s).ifPresent(secured -> cookieValues.add(SECURE + ATTRIBUTE_VALUE + secured));
			ofNullable(c.getHttpOnly()).filter(h -> h).ifPresent(httpOnly -> cookieValues.add(HTTP_ONLY + ATTRIBUTE_VALUE + httpOnly));
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			sdf.setTimeZone(timeZone);
			ofNullable(c.getExpiryDate()).ifPresent(expireDate -> cookieValues.add(EXPIRES + ATTRIBUTE_VALUE + sdf.format(expireDate)));
			ofNullable(c.getVersion()).ifPresent(version -> cookieValues.add(VERSION + ATTRIBUTE_VALUE + version));
			ofNullable(c.getSameSite()).ifPresent(sameSite -> cookieValues.add(SAME_SITE + ATTRIBUTE_VALUE + sameSite));
			return cookieValues.isEmpty() ? c.getName() : c.getName() + NAME_DELIMITER + String.join(ATTRIBUTE_SEPARATOR, cookieValues);
		}).orElse(null);
	}
}
