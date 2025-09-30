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

package com.epam.reportportal.formatting;

import com.epam.reportportal.formatting.http.HttpFormatter;
import com.epam.reportportal.formatting.http.HttpPartFormatter;
import com.epam.reportportal.formatting.http.HttpRequestFormatter;
import com.epam.reportportal.formatting.http.entities.BodyType;
import com.epam.reportportal.formatting.http.entities.Cookie;
import com.epam.reportportal.formatting.http.entities.Header;
import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.message.ReportPortalMessage;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.step.StepReporter;
import com.epam.reportportal.utils.files.ByteSource;
import com.epam.reportportal.utils.http.ContentType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static com.epam.reportportal.formatting.http.Constants.BODY_TYPE_MAP;
import static com.epam.reportportal.formatting.http.Constants.DEFAULT_PRETTIFIERS;
import static java.util.Optional.ofNullable;

/**
 * Common class for HTTP formatters.
 *
 * @param <SELF> the type of the formatter
 */
public abstract class AbstractHttpFormatter<SELF extends AbstractHttpFormatter<SELF>> {

	protected final String logLevel;

	protected final Function<Header, String> headerConverter;
	protected final Function<Header, String> partHeaderConverter;
	protected final Function<Cookie, String> cookieConverter;
	protected final Function<String, String> uriConverter;

	private Map<String, Function<String, String>> contentPrettifiers = DEFAULT_PRETTIFIERS;

	/**
	 * @deprecated Use {@link #getContentPrettifiers()} instead
	 */
	@Deprecated
	protected Map<String, Function<String, String>> contentPrettiers = contentPrettifiers;

	/**
	 * @deprecated Use {@link #getBodyTypeMap()} instead
	 */
	@Deprecated
	protected Map<String, BodyType> bodyTypeMap = BODY_TYPE_MAP;

	/**
	 * Create a formatter with the specific log level and converters.
	 *
	 * @param defaultLogLevel           log level on which OKHTTP3 requests/responses will appear on Report Portal
	 * @param headerConvertFunction     if you want to preprocess your HTTP Headers before they appear on Report Portal
	 *                                  provide this custom function for the class, default function formats it like
	 *                                  that: <code>header.getName() + ": " + header.getValue()</code>
	 * @param partHeaderConvertFunction the same as for HTTP Headers, but for parts in Multipart request
	 * @param cookieConvertFunction     the same as 'headerConvertFunction' param but for Cookies, default function
	 *                                  formats Cookies with <code>toString</code> method
	 * @param uriConverterFunction      the same as 'headerConvertFunction' param but for URI, default function returns
	 *                                  URI "as is"
	 */
	protected AbstractHttpFormatter(@Nonnull LogLevel defaultLogLevel, @Nullable Function<Header, String> headerConvertFunction,
			@Nullable Function<Header, String> partHeaderConvertFunction, @Nullable Function<Cookie, String> cookieConvertFunction,
			@Nullable Function<String, String> uriConverterFunction) {
		logLevel = defaultLogLevel.name();
		headerConverter = headerConvertFunction;
		partHeaderConverter = partHeaderConvertFunction;
		cookieConverter = cookieConvertFunction;
		uriConverter = uriConverterFunction;
	}

	protected void attachAsBinary(@Nullable String message, @Nullable byte[] attachment, @Nonnull String contentType) {
		if (attachment == null) {
			ReportPortal.emitLog(message, logLevel, java.time.Instant.now());
		} else {
			ReportPortal.emitLog(
					new ReportPortalMessage(ByteSource.wrap(attachment), contentType, message),
					logLevel,
					java.time.Instant.now()
			);
		}
	}

	protected void logMultiPartRequest(@Nonnull HttpRequestFormatter formatter) {
		java.time.Instant currentDate = java.time.Instant.now();
		String headers = formatter.formatHeaders() + formatter.formatCookies();
		if (!headers.isEmpty()) {
			ReportPortal.emitLog(headers, logLevel, currentDate);
		}

		java.time.Instant myDate = currentDate;
		for (HttpPartFormatter part : formatter.getMultipartBody()) {
			myDate = myDate.plusMillis(1);
			HttpPartFormatter.PartType partType = part.getType();
			switch (partType) {
				case TEXT:
					ReportPortal.emitLog(part.formatAsText(), logLevel, myDate);
					break;
				case BINARY:
					attachAsBinary(part.formatForBinaryDataPrefix(), part.getBinaryPayload(), part.getMimeType());
			}
		}
	}

	protected void emitLog(HttpFormatter formatter) {
		BodyType type = formatter.getType();
		switch (type) {
			case NONE:
				ReportPortal.emitLog(formatter.formatHead(), logLevel, java.time.Instant.now());
				break;
			case TEXT:
			case FORM:
				ReportPortal.emitLog(formatter.formatAsText(), logLevel, java.time.Instant.now());
				break;
			case BINARY:
				attachAsBinary(
						formatter.formatHead(),
						formatter.getBinaryBody(),
						ofNullable(formatter.getMimeType()).orElse(ContentType.APPLICATION_OCTET_STREAM)
				);
				break;
			case MULTIPART:
				Optional<StepReporter> sr = ofNullable(Launch.currentLaunch()).map(Launch::getStepReporter);
				//noinspection ReactiveStreamsUnusedPublisher
				sr.ifPresent(r -> r.sendStep(ItemStatus.INFO, formatter.formatTitle()));
				logMultiPartRequest((HttpRequestFormatter) formatter); // No multipart type for responses
				sr.ifPresent(StepReporter::finishPreviousStep);
				break;
			default:
				ReportPortal.emitLog("Unknown entity type: " + type.name(), LogLevel.ERROR.name(), java.time.Instant.now());
		}
	}

	/**
	 * Set the body type map for the formatter.
	 * <p>
	 * The map should contain the mapping between the content type and the body type (an instance of {@link BodyType} enum). The body type
	 * is used to determine how to log the body of the request/response.
	 *
	 * @param typeMap a map with the content type as a key and the body type as a value
	 * @return the formatter instance
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public SELF setBodyTypeMap(@Nonnull Map<String, BodyType> typeMap) {
		this.bodyTypeMap = Collections.unmodifiableMap(new HashMap<>(typeMap));
		return (SELF) this;
	}

	/**
	 * Get the body type map for the formatter.
	 * <p>
	 * The map contains the mapping between the content type and the body type (an instance of {@link BodyType} enum). The body type is
	 * used to determine how to log the body of the request/response.
	 *
	 * @return a map with the content type as a key and the body type as a value
	 */
	@Nonnull
	public Map<String, BodyType> getBodyTypeMap() {
		return bodyTypeMap;
	}

	/***
	 * Set the content prettifiers for the formatter.
	 * <p>
	 * Content prettifiers are used to format the content of the request/response before logging it. The prettifiers are applied to the
	 * content based on the content type.
	 *
	 * @param contentPrettifiers a map with the content type as a key and the prettifier function as a value
	 * @return the formatter instance
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public SELF setContentPrettifiers(@Nonnull Map<String, Function<String, String>> contentPrettifiers) {
		this.contentPrettifiers = Collections.unmodifiableMap(new HashMap<>(contentPrettifiers));
		this.contentPrettiers = this.contentPrettifiers;
		return (SELF) this;
	}

	/***
	 * Set the content prettifiers for the formatter.
	 * <p>
	 * Content prettifiers are used to format the content of the request/response before logging it. The prettifiers are applied to the
	 * content based on the content type.
	 *
	 * @param contentPrettifiers a map with the content type as a key and the prettifier function as a value
	 * @return the formatter instance
	 * @deprecated Use {@link #setContentPrettifiers(Map)} instead
	 */
	@Deprecated
	@Nonnull
	public SELF setContentPrettiers(@Nonnull Map<String, Function<String, String>> contentPrettifiers) {
		return setContentPrettifiers(contentPrettifiers);
	}

	/**
	 * Get the content prettifiers for the formatter.
	 * <p>
	 * Content prettifiers are used to format the content of the request/response before logging it. The prettifiers are applied to the
	 * content based on the content type.
	 *
	 * @return a map with the content type as a key and the prettifier function as a value
	 */
	@Nonnull
	public Map<String, Function<String, String>> getContentPrettifiers() {
		return contentPrettifiers;
	}
}
