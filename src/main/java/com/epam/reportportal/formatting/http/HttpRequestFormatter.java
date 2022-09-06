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
import com.epam.reportportal.formatting.http.converters.DefaultUriConverter;
import com.epam.reportportal.formatting.http.entities.BodyType;
import com.epam.reportportal.formatting.http.entities.Cookie;
import com.epam.reportportal.formatting.http.entities.Header;
import com.epam.reportportal.formatting.http.entities.Param;
import org.apache.http.entity.ContentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static com.epam.reportportal.formatting.http.Constants.*;
import static com.epam.reportportal.formatting.http.HttpFormatUtils.joinParts;
import static java.util.Optional.ofNullable;

public class HttpRequestFormatter implements HttpFormatter {
	private final String method;
	private final String uri;

	private Function<String, String> uriConverter;
	private Function<Header, String> headerConverter;
	private Function<Cookie, String> cookieConverter;
	private Function<Param, String> paramConverter;
	private Map<String, Function<String, String>> prettiers;

	private List<Header> headers;
	private List<Cookie> cookies;

	private String mimeType;
	private BodyType type = BodyType.NONE;

	private Object body;

	public HttpRequestFormatter(@Nonnull String requestMethod, @Nonnull String requestUri) {
		method = requestMethod;
		uri = requestUri;
	}

	@Override
	@Nonnull
	public String formatTitle() {
		return REQUEST_TAG + LINE_DELIMITER + String.format("%s to %s", method, uriConverter.apply(uri));
	}

	@Nonnull
	public String formatHeaders() {
		return HttpFormatUtils.formatHeaders(headers, headerConverter);
	}

	@Nonnull
	public String formatCookies() {
		return HttpFormatUtils.formatCookies(cookies, cookieConverter);
	}

	@Override
	@Nonnull
	public String formatHead() {
		return joinParts(LINE_DELIMITER + LINE_DELIMITER, formatTitle(), formatHeaders(), formatCookies());
	}

	@Override
	@Nonnull
	public String formatAsText() {
		if (BodyType.FORM == type) {
			return HttpFormatUtils.formatText(formatHead(), getFormBody(), BODY_FORM_TAG, paramConverter);
		}
		return HttpFormatUtils.formatText(formatHead(), getTextBody(), BODY_TAG, prettiers, mimeType);
	}

	public void setUriConverter(@Nonnull Function<String, String> uriConverter) {
		this.uriConverter = uriConverter;
	}

	public void setHeaderConverter(@Nonnull Function<Header, String> headerConverter) {
		this.headerConverter = headerConverter;
	}

	public void setCookieConverter(@Nonnull Function<Cookie, String> cookieConverter) {
		this.cookieConverter = cookieConverter;
	}

	public void setParamConverter(Function<Param, String> paramConverter) {
		this.paramConverter = paramConverter;
	}

	public void setHeaders(@Nonnull List<Header> requestHeaders) {
		headers = requestHeaders;
	}

	public void setCookies(@Nonnull List<Cookie> cookies) {
		this.cookies = cookies;
	}

	@Override
	@Nullable
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(@Nullable String mimeType) {
		this.mimeType = mimeType;
	}

	public void setType(@Nonnull BodyType type) {
		this.type = type;
	}

	@Override
	@Nonnull
	public BodyType getType() {
		return type;
	}

	public <T> void setBody(@Nullable T body) {
		if (body == null) {
			setType(BodyType.NONE);
		}
		this.body = body;
	}

	public String getTextBody() {
		if (BodyType.TEXT == type) {
			return (String) body;
		}
		throw new ClassCastException("Cannot return text body for body type: " + type.name());

	}

	@SuppressWarnings("unchecked")
	public List<Param> getFormBody() {
		if (BodyType.FORM == type) {
			return (List<Param>) body;
		}
		throw new ClassCastException("Cannot return form body for body type: " + type.name());

	}

	@Override
	@Nonnull
	public byte[] getBinaryBody() {
		if (BodyType.BINARY == type) {
			return (byte[]) body;
		}
		throw new ClassCastException("Cannot return binary body for body type: " + type.name());
	}

	@SuppressWarnings("unchecked")
	public List<HttpPartFormatter> getMultipartBody() {
		Objects.requireNonNull(body);
		if (BodyType.MULTIPART == type) {
			return (List<HttpPartFormatter>) body;
		}
		throw new ClassCastException("Cannot return multipart body for body type: " + type.name());
	}

	public void setPrettiers(Map<String, Function<String, String>> prettiers) {
		this.prettiers = prettiers;
	}

	public static class Builder {
		private final String method;
		private final String uri;
		private final List<Header> headers = new ArrayList<>();
		private final List<Cookie> cookies = new ArrayList<>();

		private Function<String, String> uriConverter;
		private Function<Header, String> headerConverter;
		private Function<Cookie, String> cookieConverter;
		private Function<Param, String> paramConverter;

		private BodyType type;
		private String mimeType;

		private Object body;

		private Map<String, Function<String, String>> prettiers;

		public Builder(@Nonnull String requestMethod, @Nonnull String requestUri) {
			method = requestMethod;
			uri = requestUri;
		}

		public Builder uriConverter(Function<String, String> uriConverter) {
			this.uriConverter = uriConverter;
			return this;
		}

		public Builder headerConverter(Function<Header, String> headerConverter) {
			this.headerConverter = headerConverter;
			return this;
		}

		public Builder cookieConverter(Function<Cookie, String> cookieConverter) {
			this.cookieConverter = cookieConverter;
			return this;
		}

		public Builder paramConverter(Function<Param, String> paramConverter) {
			this.paramConverter = paramConverter;
			return this;
		}

		public Builder addHeader(String name, String value) {
			headers.add(HttpFormatUtils.toHeader(name, value));
			return this;
		}

		public Builder addCookie(Cookie cookie) {
			cookies.add(cookie);
			return this;
		}

		public Builder addCookie(String name, String value, String comment, String path, String domain, Long maxAge,
				Boolean secured, Boolean httpOnly, Date expiryDate, Integer version, String sameSite) {
			return addCookie(HttpFormatUtils.toCookie(name,
					value,
					comment,
					path,
					domain,
					maxAge,
					secured,
					httpOnly,
					expiryDate,
					version,
					sameSite
			));
		}

		public Builder addCookie(String name, String value) {
			return addCookie(name, value, null, null, null, null, null, null, null, null, null);
		}

		public Builder addCookie(String name) {
			return addCookie(name, null);
		}

		public Builder bodyText(String mimeType, String payload) {
			type = BodyType.TEXT;
			this.mimeType = mimeType;
			body = payload;
			return this;
		}

		public Builder bodyBytes(String mimeType, byte[] payload) {
			type = BodyType.BINARY;
			this.mimeType = mimeType;
			body = payload;
			return this;
		}

		public Builder bodyParams(List<Param> formParameters) {
			type = BodyType.FORM;
			this.mimeType = ContentType.APPLICATION_FORM_URLENCODED.getMimeType();
			body = formParameters;
			return this;
		}

		public Builder bodyParams(Map<String, String> formParameters) {
			type = BodyType.FORM;
			this.mimeType = ContentType.APPLICATION_FORM_URLENCODED.getMimeType();
			body = HttpFormatUtils.toForm(formParameters);
			return this;
		}

		public Builder bodyParams(String formParameters) {
			type = BodyType.FORM;
			this.mimeType = ContentType.APPLICATION_FORM_URLENCODED.getMimeType();
			body = HttpFormatUtils.toForm(formParameters);
			return this;
		}

		@SuppressWarnings("unchecked")
		public Builder addBodyPart(HttpPartFormatter part) {
			if (body != null && type == BodyType.MULTIPART) {
				((List<HttpPartFormatter>) body).add(part);
			} else {
				type = BodyType.MULTIPART;
				body = new ArrayList<>(Collections.singleton(part));
			}
			return this;
		}

		public Builder prettiers(Map<String, Function<String, String>> formatPrettiers) {
			this.prettiers = formatPrettiers;
			return this;
		}

		public HttpRequestFormatter build() {
			HttpRequestFormatter result = new HttpRequestFormatter(method, uri);
			result.setUriConverter(ofNullable(uriConverter).orElse(DefaultUriConverter.INSTANCE));
			result.setHeaderConverter(ofNullable(headerConverter).orElse(DefaultHttpHeaderConverter.INSTANCE));
			result.setCookieConverter(ofNullable(cookieConverter).orElse(DefaultCookieConverter.INSTANCE));
			result.setParamConverter(ofNullable(paramConverter).orElse(DefaultFormParamConverter.INSTANCE));
			result.setPrettiers(ofNullable(prettiers).orElse(Constants.DEFAULT_PRETTIERS));
			result.setHeaders(headers);
			result.setCookies(cookies);
			if (body != null) {
				result.setType(type);
				result.setMimeType(mimeType);
				result.setBody(body);
			} else {
				result.setType(BodyType.NONE);
			}
			return result;
		}
	}
}
