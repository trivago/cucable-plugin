# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

Back to [Readme](README.md).

## [1.10.0] - UNRELEASED

### Added

* Ability added to specify a folder containing rerun text files [183]
* Additional `generated-features.properties` file in the generated feature directory that stores all generated feature names and their reference to the respective source feature [184]
* Feature source options can now be freely combined
* Cucable version is now mentioned in the generated runners and features

### Changed

* Various dependency updates 

## [1.9.0] - 2020-11-26

### Added
* Better error messages when features cannot be parsed (#173)
* `Background` steps are now preserved as `Background` in generated scenarios (#160)

## [1.8.0] - 2020-08-15

### Fixed

* Correct handling of feature tags in combination with multiple tagged example tables (#168)
* Invalid runner class name for features beginning with numbers (#156)

## [1.7.2] - 2020-04-03

### Fixed

* Cucable does not fail on empty Cucumber feature list file.

## [1.7.1] - 2020-04-03

### Changed

* Runner and feature file names generated from Cucumber feature text files are now postfixed with `_rerun_IT`

## [1.7.0] - 2020-03-30

### Added

* Support for Cucumber feature text files (#154)

### Changed

* Various dependency and documentation updates

## [1.6.0] - 2019-12-10

### Changed

* Cucable is now more resilient when trying to deal with unparsable features - these are skipped instead of stopping the overall execution.

### Fixed

* Utf-8 encoding error on linux (#150)
* Scenarios had too many tags when there were also feature tags (#145)

## [1.5.3] - 2019-09-09

### Added

* Scenario name support (#127, contributed by @josepj)

## [1.5.2] - 2019-07-11

### Changed

* Updated dependencies
* Replaced `cobertura` with `jacoco`

## [1.5.1] - 2019-03-14

### Fixed

* Potentially wrong handling of scenarios without tags when a tag expression is provided in `<includeScenarioTags>` (#107)

## [1.5.0] - 2019-03-11

### Fixed

* Untrue error on missing example table columns (#100)
* Errors before scenario parsing are not reported as parse errors anymore

### Added

* [Cucumber tag expression](https://docs.cucumber.io/cucumber/api/#tag-expressions) support (#104)

### Removed

* Removed parameters `excludeScenarioTags`, `includeScenarioTagsConnector` and `excludeScenarioTagsConnector` in favor of [Cucumber tag expressions](https://docs.cucumber.io/cucumber/api/#tag-expressions) in `includeScenarioTags`

### Changed

* `includeScenarioTags` now expects a [Cucumber tag expression](https://docs.cucumber.io/cucumber/api/#tag-expressions)

## [1.4.0] - 2019-02-13

### Added

- Support for `and` and `or` mode for `includeScenarioTags` and `excludeScenarioTags` via `includeScenarioTagsConnector` and `excludeScenarioTagsConnector` parameters (default: 'or') (#88):

    ```
    <includeScenarioTagsConnector>and</includeScenarioTagsConnector>
    <excludeScenarioTagsConnector>or</excludeScenarioTagsConnector>
    ```

- Support to specify multiple `sourceFeatures` (#55), e.g.:
    ```
    <sourceFeatures>
        src/test/resources/features/sometests,
        src/test/resources/features/MyFeature.feature:8:15
    </sourceFeatures>
    ```
    
- Ability to generate runners with a specific number of features by specifying `desiredNumberOfFeaturesPerRunner` (#70)

### Changed

- `includeScenarioTags` and `excludeScenarioTags` are now specified as a list of strings:
  
    ```
    <includeScenarioTags>@tag1,@tag2</includeScenarioTags>
    <excludeScenarioTags>@tag3</excludeScenarioTags>
    ```
- `includeScenarioTags` and `excludeScenarioTags` can now be used without the preceding `@`:

     `<includeScenarioTags>tag1,tag2</includeScenarioTags>`
    
- Completely rewritten feature handling to support more options 

## [1.3.2] - 2019-02-01

### Fixed

- Feature parsing on windows (#73)
- Arbitrary table cell content handling (#94)

## [1.3.1] - 2019-01-21

### Fixed

- Fixed handling of data and example tables containing line breaks (`\n`)

## [1.3.0] - 2018-11-30

### Added

- Support for scenarios with multiple example tables

### Changed

- `parallelizationMode=features` now uses exact copies of the source features

## [1.2.0] - 2018-11-12

### Added

- New property `parallelizationMode` so that Cucable can optionally parallelize complete features instead of individual scenarios

### Changed

- Logging tweaks and removed code duplication

## [1.1.0] - 2018-06-25

### Added

- Support for custom template placeholders via `[CUCABLE:CUSTOM:xxx]`

## [1.0.0] - 2018-05-18

### Added

- Support a fixed number of runners running multiple scenarios in sequence
- New `[CUCABLE:RUNNER]` template placeholder that is substituted with the current runner name
- New `[CUCABLE:FEATURE]` template placeholder that is substituted with the one or multiple features in the generated runner

### Changed

- Feature and runner generation is now separated in order to support more features in the future.

### Fixed

- Better logging for missing example table placeholders in scenario outlines  (contributed by [@daczczcz1](https://github.com/daczczcz1))

### Removed

- Template placeholder `[FEATURE_FILE_NAME]` is not supported anymore, please use `[CUCABLE:FEATURE]` and `[CUCABLE:RUNNER]` instead

## [0.1.11] - 2018-05-08

- Fix for wrong unicode detection of source feature path in runner comments

## [0.1.10] - 2018-05-08

- Runner comments containing "\u" were wrongly considered unicode

## [0.1.9] - 2018-04-20

### Added

- It is now possible to use an existing Java runner class as a template for generated runners
- Source runner template is now included as a comment in generated runners

## [0.1.8] - 2018-03-30

### Added

- Source feature path is now included as a comment in generated features and runners
- Configurable logging with 4 levels: _default_, _compact_, _minimal_, _off_
- The number of generated scenarios per feature is shown in the default logs
- The total number of generated features is shown in a summary line in all log levels except _off_

### Changed

- All dependencies are updated to their latest version

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

[1.10.0]: https://github.com/trivago/cucable-plugin/compare/1.9.0...1.10.0
[1.9.0]: https://github.com/trivago/cucable-plugin/compare/1.8.0...1.9.0
[1.8.0]: https://github.com/trivago/cucable-plugin/compare/1.7.2...1.8.0
[1.7.2]: https://github.com/trivago/cucable-plugin/compare/1.7.1...1.7.2
[1.7.1]: https://github.com/trivago/cucable-plugin/compare/1.7.0...1.7.1
[1.7.0]: https://github.com/trivago/cucable-plugin/compare/1.6.0...1.7.0
[1.6.0]: https://github.com/trivago/cucable-plugin/compare/1.5.3...1.6.0
[1.5.3]: https://github.com/trivago/cucable-plugin/compare/1.5.2...1.5.3
[1.5.2]: https://github.com/trivago/cucable-plugin/compare/1.5.1...1.5.2
[1.5.1]: https://github.com/trivago/cucable-plugin/compare/1.5.0...1.5.1
[1.5.0]: https://github.com/trivago/cucable-plugin/compare/1.4.0...1.5.0
[1.4.0]: https://github.com/trivago/cucable-plugin/compare/1.3.2...1.4.0
[1.3.2]: https://github.com/trivago/cucable-plugin/compare/1.3.1...1.3.2
[1.3.1]: https://github.com/trivago/cucable-plugin/compare/1.3.0...1.3.1
[1.3.0]: https://github.com/trivago/cucable-plugin/compare/1.2.0...1.3.0
[1.2.0]: https://github.com/trivago/cucable-plugin/compare/1.1.0...1.2.0
[1.1.0]: https://github.com/trivago/cucable-plugin/compare/1.0.0...1.1.0
[1.0.0]: https://github.com/trivago/cucable-plugin/compare/0.1.11...1.0.0
[0.1.11]: https://github.com/trivago/cucable-plugin/compare/0.1.10...0.1.11
[0.1.10]: https://github.com/trivago/cucable-plugin/compare/0.1.9...0.1.10
[0.1.9]: https://github.com/trivago/cucable-plugin/compare/0.1.8...0.1.9
[0.1.8]: https://github.com/trivago/cucable-plugin/compare/0.1.7...0.1.8
[0.1.7]: https://github.com/trivago/cucable-plugin/compare/0.1.6...0.1.7
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