![cucable logo](documentation/img/cucable.png)

### Run Cucumber Scenarios in Parallel with Maven

[![Apache V2 License](http://img.shields.io/badge/license-Apache%20V2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.trivago.rta/cucable-plugin.svg)](http://repo1.maven.org/maven2/com/trivago/rta/cucable-plugin/)
[![Build Status](https://travis-ci.org/trivago/cucable-plugin.svg?branch=master)](https://travis-ci.org/trivago/cucable-plugin)
[![codecov](https://codecov.io/gh/trivago/cucable-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/trivago/cucable-plugin)
[![Twitter URL](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/bischoffdev)

![Cucumber compatible](documentation/img/cucumber-compatible-black-64.png)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Cucable Maven Plugin](#cucable-maven-plugin)
  - [Repository Structure](#repository-structure)
  - [Changelog](#changelog)
  - [Maven dependency](#maven-dependency)
- [Typical workflow](#typical-workflow)
  - [1. Generation of runners and features](#1-generation-of-runners-and-features)
    - [Required Parameters](#required-parameters)
      - [sourceRunnerTemplateFile](#sourcerunnertemplatefile)
      - [sourceFeatures](#sourcefeatures)
      - [generatedFeatureDirectory](#generatedfeaturedirectory)
      - [generatedRunnerDirectory](#generatedrunnerdirectory)
    - [Optional Parameters](#optional-parameters)
      - [numberOfTestRuns](#numberoftestruns)
      - [includeScenarioTags](#includescenariotags)
      - [excludeScenarioTags](#excludescenariotags)
    - [Generating runners and features inside target directory](#generating-runners-and-features-inside-target-directory)
    - [Complete Example](#complete-example)
      - [Source feature file](#source-feature-file)
      - [Runner template file](#runner-template-file)
      - [Generated Scenarios](#generated-scenarios)
      - [Generated runners](#generated-runners)
  - [2. Running the generated tests with Maven failsafe](#2-running-the-generated-tests-with-maven-failsafe)
  - [3. Aggregation of a single test report after all test runs](#3-aggregation-of-a-single-test-report-after-all-test-runs)
- [Example project](#example-project)
- [Additional Information](#additional-information)
  - [Building](#building)
  - [License](#license)
  - [Credits](#credits)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Cucable Maven Plugin

Cucable is a Maven plugin for [Cucumber](https://cucumber.io) scenarios that simplifies fine-grained and efficient parallel test runs.

This plugin does the following:

- Generate single Cucumber features containing one single scenario each
- Convert scenario outlines into separate scenarios
- Generating Cucumber runners for every generated "single scenario" feature file

Those generated runners and features can then be used with [Maven Failsafe](http://maven.apache.org/surefire/maven-failsafe-plugin/) in order to parallelize test runs.

**Note:** From version 0.1.7 on this also works for non-english feature files!

## Repository Structure

* [plugin-code](plugin-code) contains the full plugin source code.
* [example-project](example-project) contains an example Maven project to see the plugin in action.

## Changelog

All changes are documented in the [full changelog](CHANGELOG.md).

## Maven dependency

```xml
<dependency>
    <groupId>com.trivago.rta</groupId>
    <artifactId>cucable-plugin</artifactId>
    <version>(check version on top of the page)</version>
</dependency>
```

# Typical workflow

1. Generation of runners and features
2. Running the generated tests with Maven failsafe
3. Aggregation of a single test report after all test runs

The following sections break down the above steps.

## 1. Generation of runners and features

```xml
<plugin>
    <groupId>com.trivago.rta</groupId>
    <artifactId>cucable-plugin</artifactId>
    <version>${cucable-plugin.version}</version>
    <executions>
        <execution>
            <id>generate-test-resources</id>
            <phase>generate-test-resources</phase>
            <goals>
                <goal>parallel</goal>
            </goals>
            <configuration>
                <!-- Required properties -->
                <sourceRunnerTemplateFile>src/test/resources/parallel/cucable.template</sourceRunnerTemplateFile>
                <sourceFeatures>src/test/resources/features</sourceFeatures>
                <generatedFeatureDirectory>src/test/resources/parallel/features</generatedFeatureDirectory>
                <generatedRunnerDirectory>src/test/java/parallel/runners</generatedRunnerDirectory>
                
                <!-- Optional properties -->
                <numberOfTestRuns>1</numberOfTestRuns>
                <includeScenarioTags>
                    <param>@includeMe</param>
                    <param>@includeMeAsWell</param>
                </includeScenarioTags>                                
                <excludeScenarioTags>
                    <param>@skip</param>
                </excludeScenarioTags>                                
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Required Parameters

#### sourceRunnerTemplateFile

The path to a text file (e.g. _src/test/resources/parallel/cucable.template_) with **[FEATURE_FILE_NAME]** placeholders for the generated feature file name.
This file will be used to generate runners for every generated feature file.

Example:

<pre>
package com.example;

import cucumber.api.junit.Cucumber;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = {"classpath:parallel/features/<b>[FEATURE_FILE_NAME]</b>.feature"},
    plugin = {"json:target/cucumber-report/<b>[FEATURE_FILE_NAME]</b>.json"},
    glue = {"com.example.glue"}
)
public class <b>[FEATURE_FILE_NAME]</b> {
}
</pre>

The placeholder **[FEATURE_FILE_NAME]** will be replaced with generated feature names by Cucable.

#### sourceFeatures

This can specify
* the root path of your __existing__ Cucumber _.feature_ files (e.g. ```src/test/resources/features```)
* the path to a specific __existing__ Cucumber _.feature_ file (e.g. ```src/test/resources/features/MyFeature.feature```)
* the path to a specific __existing__ Cucumber _.feature_ file including line numbers of specific scenarios/scenario outlines inside this file (e.g. ```src/test/resources/features/MyFeature.feature:12:19``` would only convert the scenarios starting at line _12_ and _19_ inside _MyFeature.feature_)

#### generatedFeatureDirectory

The path where the __generated__ Cucumber .feature files should be located (e.g. _src/test/resources/parallel_).

**Note:** This directory should be located under a valid resource folder to be included as a test source by Maven.
If you want to use a directory inside Maven's target folder, [check this example](#generating-runners-and-features-inside-target-directory).

**Caution:** This directory will be wiped prior to the feature file generation!

#### generatedRunnerDirectory

The path where the __generated__ runner classes should be located (e.g. _src/test/java/parallel/runners_).

**Note:** This directory should be located under a valid source folder to be included as a test source by Maven.
If you want to use a directory inside Maven's target folder, [check this example](#generating-runners-and-features-inside-target-directory).

**Caution:** This directory will be wiped prior to the runner file generation!

### Optional Parameters

#### numberOfTestRuns

Optional number of test runs. If it is not set, its default value is __1__.
For each test run, the whole set of features and runners is generated like this:

- MyFeature_scenario001_run001_IT.feature
- MyFeature_scenario001_run002_IT.feature
- MyFeature_scenario001_run003_IT.feature
- etc.

**Note:** Characters other than letters from A to Z, numbers and underscores will be stripped out of the feature file name.

#### includeScenarioTags

Optional scenario tags that __should be included__ in the feature and runner generation.
To include multiple tags, just add each one into as its own ```<param>```:

```
<includeScenarioTags>
    <param>@scenario1Tag1</param>
    <param>@scenario1Tag2</param>
</includeScenarioTags>
```

__Note:__ When using _includeScenarioTags_ and _excludeScenarioTags_ together, the _excludeScenarioTags_ will override the _includeScenarioTags_.
This means that a scenario containing an included tag __and__ an excluded tag will be __excluded__!

#### excludeScenarioTags

Optional scenario tags that __should not be included__ in the feature and runner generation.
To include multiple tags, just add each one into as its own ```<param>```:

```
<excludeScenarioTags>
    <param>@tag1</param>
    <param>@tag2</param>
</excludeScenarioTags>
```

__Note:__ When using _includeScenarioTags_ and _excludeScenarioTags_ together, the _excludeScenarioTags_ will override the _includeScenarioTags_.
This means that a scenario containing an included tag __and__ an excluded tag will be __excluded__!

### Generating runners and features inside target directory

It may be desirable for you to generate the Cucable features and runners in Maven's `target` directory.
The advantage of this is that this directory is wiped by the `mvn clean` command and older generated files do not reside in your `src` directory.

In order to achieve this, you can specify subdirectories under target (`${project.build.directory}`) for Cucable, e.g. `${project.build.directory}/parallel/runners` and `${project.build.directory}/parallel/features`

After this step, use the *build-helper-maven-plugin* in your POM file in order to consider the generated runner classes test sources:

```xml
<plugin>
    <groupId>com.trivago.rta</groupId>
    <artifactId>cucable-plugin</artifactId>
    <version>${cucable.plugin.version}</version>
    <executions>
        <execution>
            <id>generate-test-resources</id>
            <phase>generate-test-resources</phase>
            <goals>
                <goal>parallel</goal>
            </goals>
            <configuration>
                <sourceRunnerTemplateFile>path_to_template_file</sourceRunnerTemplateFile>
                <sourceFeatures>path_to_features</sourceFeatures>
                <generatedFeatureDirectory>${project.build.directory}/parallel/features</generatedFeatureDirectory>
                <generatedRunnerDirectory>${project.build.directory}/parallel/runners</generatedRunnerDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>${build.helper.plugin.version}</version>
    <executions>
        <execution>
            <id>add-test-source</id>
            <phase>generate-test-sources</phase>
            <goals>
                <goal>add-test-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>${project.build.directory}/parallel/runners</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Complete Example

Below, you can see a full example of what Cucable does.

#### Source feature file

This is our source feature file. It contains a scenario and a scenario outline with two examples.

*MyFeature.feature*
```
Feature: This is the feature name

    Scenario: First scenario
        Given I am on the start page
        And I click the login button
        Then I see an error message

    Scenario Outline: Second scenario with an amount of <amount>
        Given I am on the start page
        And I add <amount> items
        And I navigate to the shopping basket
        Then I see <amount> items
        Examples:
            | amount |
            | 12     |
            | 85     |
```

#### Runner template file

This is the runner template file that is used to generate single scenario runners.
The placeholder **[FEATURE_FILE_NAME]** will be replaced with generated feature names by Cucable.

<pre>
package parallel.runners;

import cucumber.api.junit.Cucumber;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = {"classpath:parallel/features/<b>[FEATURE_FILE_NAME]</b>.feature"},
    plugin = {"json:target/cucumber-report/<b>[FEATURE_FILE_NAME]</b>.json"},
    glue = {"com.trivago.glue"}
)
public class <b>[FEATURE_FILE_NAME]</b> {
}
</pre>

**Note:** The specified _plugin_ generates Cucumber JSON files which are needed for custom aggregated test reports.

#### Generated Scenarios

For each scenario, a single feature file is created: 

*MyFeature_scenario001_run001_IT.feature*

```
Feature: This is the feature name

Scenario: First scenario
Given I am on the start page
And I click the login button
Then I see an error message
```

Note that for the scenario outlines, each example is converted to its own scenario and feature file:

*MyFeature_scenario002_run001_IT.feature*

<pre>
Feature: This is the feature name

Scenario: Second scenario with an amount of <b>12</b>
Given I am on the start page
And I add <b>12</b> items
And I navigate to the shopping basket
Then I see <b>12</b> items
</pre>

*MyFeature_scenario003_run001_IT.feature*

<pre>
Feature: This is the feature name

Scenario: Second scenario with an amount of <b>85</b>
Given I am on the start page
And I add <b>85</b> items
And I navigate to the shopping basket
Then I see <b>85</b> items
</pre>

#### Generated runners

The generated runners point to each one of the generated feature files.

This is an example for one of the generated runners - note how the placeholders are now replaced with the name of the feature to run:

*MyFeature_scenario001_run001_IT.java*

<pre>
package parallel.runners;

import cucumber.api.junit.Cucumber;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    monochrome = false,
    features = {"classpath:parallel/features/<b>MyFeature_scenario001_run001_IT</b>.feature"},
    plugin = {"json:target/cucumber-report/<b>MyFeature_scenario001_run001_IT</b>.json"},
    glue = {"com.trivago.glue"}
)
public class <b>MyFeature_scenario001_run001_IT</b> {
}
</pre>

## 2. Running the generated tests with Maven failsafe

This will skip the unit tests (if any) and run the generated runner classes with Failsafe.
Since all generated runner classes from the step before end with ___IT__, they are automatically considered integration tests and run with failsafe.

**Note:** If all tests should be run regardless of their result, it is important to set ```<testFailureIgnore>true</testFailureIgnore>``` for Failsafe - otherwise the plugin execution will stop on failing tests.
However, if this is specified, the build will not fail in case of failing tests!

To circumvent that, it is possible to specify a custom [rule](https://maven.apache.org/enforcer/enforcer-api/writing-a-custom-rule.html) for [Maven enforcer](https://maven.apache.org/enforcer/maven-enforcer-plugin/) that passes or fails the build depending on custom conditions.

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
            <skipTests>true</skipTests>
        </configuration>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-failsafe-plugin</artifactId>
        <executions>
            <execution>
                <id>Run parallel tests</id>
                <phase>integration-test</phase>
                <goals>
                    <goal>integration-test</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <testFailureIgnore>true</testFailureIgnore>
            <forkCount>${maven.fork.count}</forkCount>
            <reuseForks>false</reuseForks>
            <argLine>-Dfile.encoding=UTF-8</argLine>
            <disableXmlReport>true</disableXmlReport>
        </configuration>
    </plugin>
</plugins>
```

## 3. Aggregation of a single test report after all test runs

We use the [Cluecumber](https://github.com/trivago/cluecumber-report-plugin) plugin to aggregate all generated __.json__ report files into one overall test report.

```xml
<plugin>
    <groupId>com.trivago.rta</groupId>
    <artifactId>cluecumber-report-plugin</artifactId>
    <version>${cluecumber.report.version}</version>
    <executions>
        <execution>
            <id>report</id>
            <phase>post-integration-test</phase>
            <goals>
                <goal>reporting</goal>
            </goals>
            <configuration>
                <sourceJsonReportDirectory>${project.build.directory}/cucumber-report</sourceJsonReportDirectory>
                <generatedHtmlReportDirectory>${project.build.directory}/test-report</generatedHtmlReportDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

# Example project

You can test the complete flow and POM configuration by checking out the [Cucable example project](example-project).

# Additional Information

## Building

Cucable requires Java >= 8 and Maven >= 3.3.9.
It is available in [Maven central](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.trivago.rta%22%20AND%20a%3A%22cucable-plugin%22).

## License

Copyright 2017 trivago N.V.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Credits

This plugin was inspired by the [Cucumber Slices Maven Plugin](https://github.com/DisneyStudios/cucumber-slices-maven-plugin).

This project is being tested on

[![Browserstack](documentation/img/browserstack.png)](http://browserstack.com)
