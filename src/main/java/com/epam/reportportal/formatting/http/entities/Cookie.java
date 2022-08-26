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

import io.reactivex.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Date;

public class Cookie implements Cloneable {

	private final String name;
	private String value;
	private String comment;
	private Date expiryDate;
	private String domain;
	private String path;
	private Boolean secured;
	private Boolean httpOnly;
	private Integer version;
	private Long maxAge;
	private String sameSite;

	public Cookie(@Nonnull String cookieName) {
		name = cookieName;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Nullable
	public String getValue() {
		return value;
	}

	public void setValue(@Nullable String value) {
		this.value = value;
	}

	@Nullable
	public String getComment() {
		return comment;
	}

	public void setComment(@Nullable String comment) {
		this.comment = comment;
	}

	@Nullable
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(@Nullable Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	@Nullable
	public String getDomain() {
		return domain;
	}

	public void setDomain(@Nullable String domain) {
		this.domain = domain;
	}

	@Nullable
	public String getPath() {
		return path;
	}

	public void setPath(@Nullable String path) {
		this.path = path;
	}

	@Nullable
	public Boolean getSecured() {
		return secured;
	}

	public void setSecured(@Nullable Boolean secured) {
		this.secured = secured;
	}

	@Nullable
	public Boolean getHttpOnly() {
		return httpOnly;
	}

	public void setHttpOnly(@Nullable Boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	@Nullable
	public Integer getVersion() {
		return version;
	}

	public void setVersion(@Nullable Integer version) {
		this.version = version;
	}

	@Nullable
	public Long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(@Nullable Long maxAge) {
		this.maxAge = maxAge;
	}

	@Nullable
	public String getSameSite() {
		return sameSite;
	}

	public void setSameSite(@Nullable String sameSite) {
		this.sameSite = sameSite;
	}

	@Nonnull
	@Override
	public Cookie clone() {
		Cookie clone = new Cookie(name);
		clone.value = value;
		clone.comment = comment;
		clone.expiryDate = expiryDate;
		clone.domain = domain;
		clone.path = path;
		clone.secured = secured;
		clone.httpOnly = httpOnly;
		clone.version = version;
		clone.maxAge = maxAge;
		clone.sameSite = sameSite;
		return clone;
	}
}
