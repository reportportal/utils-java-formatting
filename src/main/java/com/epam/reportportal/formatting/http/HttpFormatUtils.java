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

import com.epam.reportportal.formatting.http.converters.DefaultCookieConverter;
import com.epam.reportportal.formatting.http.converters.DefaultFormParamConverter;
import com.epam.reportportal.formatting.http.converters.DefaultHttpHeaderConverter;
import com.epam.reportportal.formatting.http.entities.BodyType;
import com.epam.reportportal.formatting.http.entities.Cookie;
import com.epam.reportportal.formatting.http.entities.Header;
import com.epam.reportportal.formatting.http.entities.Param;
import org.apache.http.entity.ContentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.reportportal.formatting.http.Constants.*;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class HttpFormatUtils {

	private HttpFormatUtils() {
		throw new IllegalStateException("Static only class");
	}

	public static String getMimeType(@Nullable String contentType) {
		return ofNullable(contentType).filter(ct -> !ct.isEmpty())
				.map(ct -> ContentType.parse(contentType).getMimeType())
				.orElse(ContentType.APPLICATION_OCTET_STREAM.getMimeType());
	}

	public static String joinParts(String delimiter, String... parts) {
		if (parts == null) {
			return "";
		}
		return Arrays.stream(parts)
				.filter(p -> Objects.nonNull(p) && !p.isEmpty())
				.collect(Collectors.joining(delimiter));
	}

	@Nonnull
	public static <T> String format(@Nullable List<T> entities, @Nonnull Function<T, String> converter,
			@Nullable String tag) {
		String prefix = tag == null ? "" : tag + LINE_DELIMITER;
		if (entities == null || entities.isEmpty()) {
			return "";
		}
		return of(entities.stream()
				.map(converter)
				.filter(e -> e != null && !e.isEmpty())
				.collect(Collectors.joining(LINE_DELIMITER, prefix, ""))).orElse("");
	}

	@Nonnull
	public static String formatHeaders(@Nullable List<Header> headers,
			@Nullable Function<Header, String> headerConverter) {
		return format(headers,
				headerConverter == null ? DefaultHttpHeaderConverter.INSTANCE : headerConverter,
				HEADERS_TAG
		);
	}

	@Nonnull
	public static String formatCookies(@Nullable List<Cookie> cookies,
			@Nullable Function<Cookie, String> cookieConverter) {
		return format(cookies,
				cookieConverter == null ? DefaultCookieConverter.INSTANCE : cookieConverter,
				COOKIES_TAG
		);
	}

	@Nonnull
	public static String formatText(@Nullable String header, @Nullable List<Param> params, @Nullable String tag,
			@Nullable Function<Param, String> paramConverter) {
		if (params == null || params.isEmpty()) {
			return header == null ? "" : header;
		}
		String prefix = tag == null ? "" : tag + LINE_DELIMITER;
		String body = format(params,
				paramConverter == null ? DefaultFormParamConverter.INSTANCE : paramConverter,
				null
		);
		return (header == null || header.isEmpty() ? "" : header + LINE_DELIMITER + LINE_DELIMITER) + (body.isEmpty() ?
				body :
				prefix + BODY_HIGHLIGHT + LINE_DELIMITER + body + LINE_DELIMITER + BODY_HIGHLIGHT);
	}

	@Nonnull
	public static String formatText(@Nullable String header, @Nullable String body, @Nullable String tag,
			@Nullable Map<String, Function<String, String>> contentPrettiers, String contentType) {
		Map<String, Function<String, String>> prettiers = contentPrettiers;
		if (contentPrettiers == null) {
			prettiers = Collections.emptyMap();
		}
		if (body == null || body.isEmpty()) {
			return header == null ? "" : header;
		}
		return (header == null || header.isEmpty() ? "" : header + LINE_DELIMITER + LINE_DELIMITER) + (tag == null || tag.isEmpty() ?
				"" :
				tag + LINE_DELIMITER) + BODY_HIGHLIGHT + LINE_DELIMITER + (prettiers.containsKey(contentType) ?
				prettiers.get(contentType).apply(body) :
				body) + LINE_DELIMITER + BODY_HIGHLIGHT;
	}

	@Nonnull
	public static Header toHeader(@Nonnull String name, @Nonnull String value) {
		return new Header(name, value);
	}

	@Nonnull
	public static Cookie toCookie(@Nonnull String name, @Nullable String value, @Nullable String comment,
			@Nullable String path, @Nullable String domain, @Nullable Long maxAge, @Nullable Boolean secured,
			@Nullable Boolean httpOnly, @Nullable Date expiryDate, @Nullable Integer version,
			@Nullable String sameSite) {
		Cookie cookie = new Cookie(name);
		cookie.setValue(value);
		cookie.setComment(comment);
		cookie.setPath(path);
		cookie.setDomain(domain);
		cookie.setMaxAge(maxAge);
		cookie.setSecured(secured);
		cookie.setHttpOnly(httpOnly);
		cookie.setExpiryDate(expiryDate);
		cookie.setVersion(version);
		cookie.setSameSite(sameSite);
		return cookie;
	}

	@Nonnull
	public static List<Param> toForm(@Nullable String formParameters) {
		return ofNullable(formParameters).map(params -> Arrays.stream(formParameters.split("&"))
				.map(param -> param.split("=", 2))
				.map(Arrays::stream)
				.map(param -> param.map(p -> {
					try {
						return URLDecoder.decode(p, StandardCharsets.UTF_8.name());
					} catch (UnsupportedEncodingException e) {
						throw new IllegalStateException("Missed standard charset", e);
					}
				}).collect(Collectors.toList()))
				.map(param -> {
					if (param.isEmpty()) {
						return new Param("", "");
					} else if (param.size() < 2) {
						return new Param(param.get(0), "");
					} else {
						return new Param(param.get(0), param.get(1));
					}
				})
				.collect(Collectors.toList())).orElse(Collections.emptyList());
	}

	@Nonnull
	public static List<Param> toForm(@Nullable Map<String, String> formParameters) {
		return ofNullable(formParameters).map(params -> params.entrySet()
				.stream()
				.map(e -> new Param(e.getKey(), e.getValue()))
				.collect(Collectors.toList())).orElse(Collections.emptyList());
	}

	public static BodyType getBodyType(@Nullable String contentType, @Nullable Map<String, BodyType> typeMap) {
		if (contentType == null || contentType.isEmpty()) {
			return BodyType.NONE;
		}
		String mimeType = ContentType.parse(contentType).getMimeType();
		return ofNullable(typeMap).map(m -> m.getOrDefault(mimeType, BodyType.BINARY)).orElse(BodyType.BINARY);
	}

	public static BodyType getBodyType(@Nullable String contentType) {
		return getBodyType(contentType, BODY_TYPE_MAP);
	}
}
