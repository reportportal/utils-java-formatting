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
import com.epam.reportportal.utils.http.ContentType;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.reportportal.formatting.http.Constants.*;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class HttpFormatUtils {

	private HttpFormatUtils() {
		throw new IllegalStateException("Static only class");
	}

	@Nonnull
	public static String getMimeType(@Nullable String contentType) {
		return ofNullable(contentType).filter(ct -> !ct.isEmpty())
				.map(ct -> ContentType.parse(contentType))
				.orElse(ContentType.APPLICATION_OCTET_STREAM);
	}

	@Nonnull
	public static String joinParts(@Nonnull String delimiter, @Nullable String... parts) {
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
	public static Header toHeader(@Nonnull String nameValuePair) {
		String[] nameValue = nameValuePair.split(": ", 2);
		if (nameValue.length == 0) {
			// invalid header?
			return new Header("", "");
		} else if (nameValue.length == 1) {
			return new Header(nameValue[0], "");
		}
		return new Header(nameValue[0], nameValue[1]);
	}

	@Nonnull
	public static Stream<Pair<String, String>> toKeyValue(@Nonnull String headerValue) {
		return Arrays.stream(headerValue.split(";\\s*")).map(c -> c.split("=", 2)).map(kv -> {
			if (kv.length > 1) {
				try {
					return Pair.of(kv[0], URLDecoder.decode(kv[1], Charset.defaultCharset().name()));
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				}
			}
			return Pair.of(kv[0], "");
		});
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
	public static Cookie toCookie(@Nonnull String headerValue) {
		List<Pair<String, String>> cookieValues = toKeyValue(headerValue).collect(Collectors.toList());
		Pair<String, String> nameValue = cookieValues.get(0);
		Map<String, String> cookieMetadata = cookieValues.subList(1, cookieValues.size())
				.stream()
				.collect(Collectors.toMap(kv -> kv.getKey().toLowerCase(Locale.US), Pair<String, String>::getValue));
		String comment = cookieMetadata.get("comment");
		String path = cookieMetadata.get("path");
		String domain = cookieMetadata.get("domain");
		Long maxAge = cookieMetadata.get("maxage") == null ? null : Long.valueOf(cookieMetadata.get("maxage"));
		Boolean secure = cookieMetadata.containsKey("secure");
		Boolean httpOnly = cookieMetadata.containsKey("httponly");
		// Examples: Tue, 06 Sep 2022 09:32:51 GMT
		//           Wed, 06-Sep-2023 11:22:09 GMT
		Date expiryDate = ofNullable(cookieMetadata.get("expires")).map(d -> {
			try {
				return new SimpleDateFormat(DefaultCookieConverter.DEFAULT_COOKIE_DATE_FORMAT).parse(d.replace('-',
						' '
				));
			} catch (ParseException e) {
				return null;
			}
		}).orElse(null);
		Integer version = cookieMetadata.get("version") == null ? null : Integer.valueOf(cookieMetadata.get("version"));
		String sameSite = cookieMetadata.get("samesite");

		return toCookie(nameValue.getKey(),
				nameValue.getValue(),
				comment,
				path,
				domain,
				maxAge,
				secure,
				httpOnly,
				expiryDate,
				version,
				sameSite
		);
	}

	public static boolean isCookie(@Nullable String headerName) {
		return "cookie".equalsIgnoreCase(headerName);
	}

	public static boolean isSetCookie(@Nullable String headerName) {
		return "set-cookie".equalsIgnoreCase(headerName);
	}

	@Nonnull
	private static Charset getCharset(@Nullable String contentType) {
		return ofNullable(contentType).flatMap(h -> toKeyValue(h).filter(p -> "charset".equalsIgnoreCase(p.getKey()))
				.findAny()).map(Pair::getValue).map(Charset::forName).orElse(StandardCharsets.UTF_8);
	}

	@Nonnull
	public static List<Param> toForm(@Nullable String formParameters, @Nullable String contentType) {
		Charset charset = getCharset(contentType);
		return ofNullable(formParameters).map(params -> Arrays.stream(formParameters.split("&"))
				.map(param -> param.split("=", 2))
				.map(Arrays::stream)
				.map(param -> param.map(p -> {
					try {
						return URLDecoder.decode(p, charset.name());
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
	public static List<Param> toForm(@Nullable String formParameters) {
		return toForm(formParameters, null);
	}

	@Nonnull
	public static List<Param> toForm(@Nullable Map<String, String> formParameters) {
		return ofNullable(formParameters).map(params -> params.entrySet()
				.stream()
				.map(e -> new Param(e.getKey(), e.getValue()))
				.collect(Collectors.toList())).orElse(Collections.emptyList());
	}

	@Nonnull
	public static BodyType getBodyType(@Nullable String contentType, @Nullable Map<String, BodyType> typeMap) {
		if (contentType == null || contentType.isEmpty()) {
			return BodyType.NONE;
		}
		String mimeType = ContentType.parse(contentType);
		return ofNullable(typeMap).map(m -> m.getOrDefault(mimeType, BodyType.BINARY)).orElse(BodyType.BINARY);
	}

	@Nonnull
	public static BodyType getBodyType(@Nullable String contentType) {
		return getBodyType(contentType, BODY_TYPE_MAP);
	}
}
