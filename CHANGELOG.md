# Changelog

## [Unreleased]

## [5.3.0]
### Changed
- Client version updated on [5.3.14](https://github.com/reportportal/client-java/releases/tag/5.3.14), by @HardNorth

## [5.2.7]
### Changed
- More `Prettiers` to `Prettifiers` renaming and deprecation of `Prettiers`, by @HardNorth

## [5.2.6]
### Added
- `AbstractHttpFormatter.setContentPrettifiers` and `AbstractHttpFormatter.getContentPrettifiers` methods, by @HardNorth
### Changed
- `AbstractHttpFormatter.setContentPrettiers` method deprecated, since it was misspelled, by @HardNorth
- `AbstractHttpFormatter.contentPrettiers` field deprecated, since it was misspelled, by @HardNorth
- `AbstractHttpFormatter.bodyTypeMap` field deprecated in favor of `AbstractHttpFormatter.getBodyTypeMap` method, by @HardNorth

## [5.2.5]
### Changed
- Client version updated on [5.2.26](https://github.com/reportportal/client-java/releases/tag/5.2.26), by @HardNorth

## [5.2.4]
### Added
- New Json and Text types to `Constants` class, by @HardNorth
### Changed
- Client version updated on [5.2.23](https://github.com/reportportal/client-java/releases/tag/5.2.23), by @HardNorth

## [5.2.3]
### Changed
- Client version updated on [5.2.13](https://github.com/reportportal/client-java/releases/tag/5.2.13), by @HardNorth

## [5.2.2]
### Changed
- Client version updated on [5.2.8](https://github.com/reportportal/client-java/releases/tag/5.2.8), by @HardNorth
- Client and JSR 305 dependencies marked as `compileOnly`, by @HardNorth

## [5.2.1]
### Changed
- Client version updated on [5.2.4](https://github.com/reportportal/client-java/releases/tag/5.2.4), by @HardNorth
- All dependencies are marked as `implementation`, by @HardNorth
### Removed
- `commons-model` dependency to rely on `clinet-java` exclusions in security fixes, by @HardNorth

## [5.2.0]
### Changed
- Client version updated on [5.2.0](https://github.com/reportportal/client-java/releases/tag/5.2.0), by @HardNorth
### Removed
- HttpCore dependency was removed to avoid conflicts, by @HardNorth

## [5.1.6]
### Changed
- Client version updated on [5.1.22](https://github.com/reportportal/client-java/releases/tag/5.1.22), by @HardNorth

## [5.1.5]
### Changed
- Client version updated on [5.1.16](https://github.com/reportportal/client-java/releases/tag/5.1.16), by @HardNorth

## [5.1.4]
### Changed
- Client version updated on [5.1.15](https://github.com/reportportal/client-java/releases/tag/5.1.15), by @HardNorth

## [5.1.3]
### Added
- Deprecation warning on invalid constructor in `HttpPartFormatter` class, by @HardNorth
- Cookie utility methods in `HttpFormatUtils` class, by @HardNorth
- Header string parsing method  in `HttpFormatUtils` class, by @HardNorth
- Charset parsing from content type header for Form request, by @HardNorth
### Changed
- `jsoup` version update to fix vulnerabilities, by @HardNorth

## [5.1.2]
### Added
- Additional multipart types, by @HardNorth
- `HttpFormatter.Builder.addCookie(Cookie)` methods, by @HardNorth
- `DefaultCookieConverter.DEFAULT_COOKIE_DATE_FORMAT` and `DefaultCookieConverter.DEFAULT_COOKIE_TIME_ZONE` constants, which are used to format Cookie Expiry Date, by @HardNorth
- Date Format and Time Zone bypass in Cookie converter constructors, by @HardNorth
### Fixed
- Response title format for empty phrase, by @HardNorth
- Spacing for binary multipart headers, by @HardNorth

## [5.1.1]
### Fixed
- Form data type formatting, by @HardNorth

## [5.1.0]
### Added
- `AbstractHttpFormatter` class and `HttpFormatter` interface, by @HardNorth

## [5.0.3]
### Added
- `HttpRequestFormatter.Builder.bodyParams(List<Param>)` method, by @HardNorth

## [5.0.2]
### Added
- `HttpFormatUtils.getBodyType` method, by @HardNorth

## [5.0.1]
### Added
- `application/x-www-form-urlencoded` body type handling, by @HardNorth

## [5.0.0]
### Added
- Initial release of HTTP logging utils, by @HardNorth
