# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

Back to [Readme](README.md).

## [0.1.7] - 2018-03-02

### Changed

- Updated to Gherkin 5.0.0

### Added

- All Gherkin languages are now supported

## [0.1.6] - 2018-02-09

### Added

- Ability to use example placeholders in scenario outline names (contributed by [@daczczcz1](https://github.com/daczczcz1))

## [0.1.5] - 2018-02-07

### Added

- Docstring support in steps
- Possibility to specify multiple line numbers to process specific scenarios (like in Cucumber), e.g. ```myFeature.feature:12:42:111```. 

## [0.1.4] - 2018-02-05

### Fixed

- Scenario and feature descriptions were not included in generated feature files

## [0.1.3] - 2017-12-20

### Fixed
- Fixed handling of data tables that include example table values

## [0.1.2] - 2017-12-05

### Fixed
- Tag filtering now also considers feature tags

## [0.1.1] - 2017-11-28

### Fixed
- Removed debug logs

## [0.1.0] - 2017-11-28

### Added
- Ability to include and exclude scenarios based on certain tags
- More unit tests

## [0.0.9] - 2017-10-27

### Changed
- Logging is now more compact
- Changed license blocks from trivago GmbH to trivago N.V.

### Added
- It is now possible to process a specific scenario inside a feature file by specifying a line number prefix (like in Cucumber), e.g. ```myFeature.feature:12```.
- More unit tests added.

## [0.0.8] - 2017-09-24

### Changed
- Fixed parse error on empty cells in data tables
- Fixed file name generation bug with special chars in source feature file names

### Added
- Unit tests for data table handling

## [0.0.7] - 2017-09-20

### Changed
- Complete rewrite of the internal logic of the plugin to support all edge cases like
  - empty scenarios
  - background steps (also with data tables)
  - scenario steps with data tables
  - complex scenario outlines

### Added
- Better logging (including plugin version)
- Clearer error messages

## [0.0.6] - 2017-08-31

### Added
- Ability to run single features multiple times in parallel
- Properties are now logged on plugin start
- More unit tests

### Changed
- Complete project now uses dependency injection via Google [Guice](https://github.com/google/guice)
- POM parameter _featureFileDirectory_ was renamed to _sourceFeatures_ since it now supports specifying either a directory or a single feature file

## [0.0.5] - 2017-08-18

### Added
- Cucumber 'Background' is now supported in feature files

### Changed
- More logging added.

## [0.0.4] - 2017-06-01

Initial project version on GitHub and Maven Central.

[0.1.7]: https://github.com/trivago/cucable-plugin/compare/0.1.5...0.1.7
[0.1.6]: https://github.com/trivago/cucable-plugin/compare/0.1.5...0.1.6
[0.1.5]: https://github.com/trivago/cucable-plugin/compare/0.1.4...0.1.5
[0.1.4]: https://github.com/trivago/cucable-plugin/compare/0.1.3...0.1.4
[0.1.3]: https://github.com/trivago/cucable-plugin/compare/0.1.2...0.1.3
[0.1.2]: https://github.com/trivago/cucable-plugin/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/trivago/cucable-plugin/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/trivago/cucable-plugin/compare/0.0.9...0.1.0
[0.0.9]: https://github.com/trivago/cucable-plugin/compare/0.0.8...0.0.9
[0.0.8]: https://github.com/trivago/cucable-plugin/compare/0.0.7...0.0.8
[0.0.7]: https://github.com/trivago/cucable-plugin/compare/0.0.6...0.0.7
[0.0.6]: https://github.com/trivago/cucable-plugin/compare/0.0.5...0.0.6
[0.0.5]: https://github.com/trivago/cucable-plugin/compare/v0.0.4...0.0.5
[0.0.4]: https://github.com/trivago/cucable-plugin/tree/v0.0.4