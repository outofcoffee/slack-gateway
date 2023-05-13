# Change Log

All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [0.4.8] - 2023-05-13
### Changed
- ci: adds Java 11 build.

## [0.4.7] - 2023-05-13
### Changed
- refactor: makes compatible with Vert.x 4.
- build(deps): bump io.vertx:vertx-web from 3.4.2 to 4.4.0
- build(deps): bump org.amshove.kluent:kluent from 1.64 to 1.73
  build(deps): bump org.glassfish.tyrus.bundles:tyrus-standalone-client from 1.8.3 to 2.1.3 #20
- build(deps): bump version_spek from 1.1.5 to 1.2.1 #13
- build(deps): bump version_spek from 1.1.5 to 1.2.1
- build(deps): bump mockito-kotlin-kt1.1 from 1.5.0 to 1.6.0 #10

## [0.4.6] - 2022-10-27
### Changed
- Bump version_kotlin from 1.7.10 to 1.7.20
- Bump version_jackson from 2.9.0 to 2.13.4

## [0.4.5] - 2022-10-27
### Changed
- build: bumps JRE to 8u345.
- build: bumps Kluent to 1.64.
- build: bumps Gradle to 6.9.2. build: bumps Kotlin to 1.7.10.

## [0.4.4] - 2022-10-27
### Changed
- fix: release trigger on version match.

## [0.4.3] - 2022-10-27
### Changed
- Bump version_junit_platform_gradle to 1.2.0
- Bump version_log4j to 2.19.0

## [0.4.2] - 2020-02-25
### Changed
- Defaults missing array elements in Slack API responses to empty.

## [0.4.1] - 2019-09-12
### Changed
- Now accepts both a bot token and user token to support a wider range of operations.

## [0.4.0] - 2019-08-22
### Changed
- Package renamed to align with domain.

## [0.3.0] - 2019-08-22
### Added
- Adds support for creating and posting to public channels.
- Adds support for specifying the default channel type to create.

### Changed
- Private, then public channels are searched before attempting to create a channel.

## [0.2.0] - 2019-08-08
### Added
- Adds paging support for listing users and private channels.

### Fixed
- Fixes incorrect attempt to create an existing private channel.

## [0.1.0] - 2018-08-07
### Added
- Initial release.
