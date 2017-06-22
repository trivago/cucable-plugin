![cucable logo](documentation/img/cucable.png)

[![Apache V2 License](http://img.shields.io/badge/license-Apache%20V2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [What is Cucable](#what-is-cucable)
- [Maven dependency](#maven-dependency)
- [Data flow](#data-flow)
  - [1. Generation of runners and features](#1-generation-of-runners-and-features)
    - [Parameters](#parameters)
      - [sourceRunnerTemplateFile](#sourcerunnertemplatefile)
      - [sourceFeatureDirectory](#sourcefeaturedirectory)
      - [generatedFeatureDirectory](#generatedfeaturedirectory)
      - [generatedRunnerDirectory](#generatedrunnerdirectory)
    - [Example](#example)
      - [Source feature file](#source-feature-file)
      - [Runner template file](#runner-template-file)
      - [Generated Scenarios](#generated-scenarios)
      - [Generated runners](#generated-runners)
  - [2. Running them with Maven failsafe](#2-running-them-with-maven-failsafe)
  - [3. Aggregation of a single test report after all test runs](#3-aggregation-of-a-single-test-report-after-all-test-runs)
  - [4. Passing or failing of the build according to the test results](#4-passing-or-failing-of-the-build-according-to-the-test-results)
  - [Example POM](#example-pom)
- [Building](#building)
- [Future improvements](#future-improvements)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# What is Cucable

Cucable is a Maven plugin for [Cucumber](https://cucumber.io) scenarios that simplifies running scenarios in parallel.

This plugin has two purposes:

- Generating single Cucumber features from .feature files
- Generating Cucumber runners for every generated feature file

Those generated runners and features can then be used with [Maven Failsafe](http://maven.apache.org/surefire/maven-failsafe-plugin/) in order to parallelize test runs.

This plugin was inspired by the [Cucumber Slices Maven Plugin](https://github.com/DisneyStudios/cucumber-slices-maven-plugin).

# Maven dependency

```
<dependency>
    <groupId>com.trivago.rta</groupId>
    <artifactId>cucable-plugin</artifactId>
    <version>0.0.4</version>
</dependency>
```

# Data flow

The typical flow is

1. Generation of runners and features
2. Running them with Maven failsafe
3. Aggregation of a single test report after all test runs
4. *Optional* passing or failing of the build according to the test results

The following sections break down the above steps.

## 1. Generation of runners and features

```
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
                <sourceRunnerTemplateFile>src/test/resources/parallel/cucable.template</sourceRunnerTemplateFile>
                <sourceFeatureDirectory>src/test/resources/features</sourceFeatureDirectory>
                <generatedFeatureDirectory>src/test/resources/parallel/features</generatedFeatureDirectory>
                <generatedRunnerDirectory>src/test/java/parallel/runners</generatedRunnerDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Parameters

#### sourceRunnerTemplateFile

The path to a text file (e.g. _src/test/resources/parallel/cucable.template_) with **[FEATURE_FILE_NAME]** placeholders for the generated feature file name.
This file will be used to generate runners for every generated feature file.

Example:
```
package com.example;

import com.example.YourTestRunner;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(YourTestRunner.class)
@CucumberOptions(
    monochrome = false,
    features = {"classpath:parallel/features/[FEATURE_FILE_NAME].feature"},
    format = {"json:target/cucumber-report/[FEATURE_FILE_NAME].json"},
    strict = false,
    dryRun = false,
    glue = {"com.example.glue"},
    tags = {"~@ignore"}
)
public class [FEATURE_FILE_NAME] {
}

```

#### sourceFeatureDirectory

The path where your __existing__ Cucumber .feature files are located (e.g. _src/test/resources/features_).

#### generatedFeatureDirectory

The path where the __generated__ Cucumber .feature files should be located (e.g. _src/test/resources/parallel_).

**Hint:** This directory should be located under a valid resource folder to be included as a test source by Maven.

**Caution:** This directory will be wiped prior to the feature file generation!

#### generatedRunnerDirectory

The path where the __generated__ runner classes should be located (e.g. _src/test/java/parallel/runners_).

**Hint:** This directory should be located under a valid source folder to be included as a test source by Maven.

**Caution:** This directory will be wiped prior to the runner file generation!

### Example

Below, you can see a full example of what Cucable does.

#### Source feature file

This is our source feature file. It contains a scenario and a scenario outline with two examples.

```
Feature: This is the feature name

    Scenario: First scenario
        Given I am on the start page
        And I click the login button
        Then I see an error message

    Scenario Outline: Second scenario
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
The **[FEATURE_FILE_NAME]** placeholder will be automatically replaced with the name of each generated scenario.

It is possible to specify a custom runner using ```@RunWith(MyCustomCucumberRunner.class)```!

By specifying _tags_, we can also split the tests even further if needed. In this case we just ignore all tests that are annotated with ```ignore```.

The _format_ option tells Cucumber where to put the json report files for the aggregated test report.

<pre>
package parallel.runners;

import com.trivago.trupi.runner.MyCustomCucumberRunner;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(MyCustomCucumberRunner.class)
@CucumberOptions(
    monochrome = false,
    features = {"classpath:parallel/features/<b>[FEATURE_FILE_NAME]</b>.feature"},
    format = {"json:target/cucumber-report/<b>[FEATURE_FILE_NAME]</b>.json"},
    strict = false,
    dryRun = false,
    glue = {"com.trivago.glue"},
    tags = {"~@ignore"}
)
public class <b>[FEATURE_FILE_NAME]</b> {
}
</pre>

#### Generated Scenarios

For each scenario, a single feature file is created: 

*Example_00001_IT.feature*

```
Feature: This is the feature name

Scenario: First scenario
Given I am on the start page
And I click the login button
Then I see an error message
```

Note that for the scenario outlines, each example is converted to its own scenario and feature file:

*Example_00002_IT.feature*

<pre>
Feature: This is the feature name

Scenario: Second scenario
Given I am on the start page
And I add <b>12</b> items
And I navigate to the shopping basket
Then I see <b>12</b> items
</pre>

*Example_00003_IT.feature*

<pre>
Feature: This is the feature name

Scenario: Second scenario
Given I am on the start page
And I add <b>85</b> items
And I navigate to the shopping basket
Then I see <b>85</b> items
</pre>

#### Generated runners

The generated runners point to each one of the generated feature files.

This is an example for one of the generated runners - note how the placeholders are now replaced with the name of the feature to run:

*Example_00003_IT.java*

<pre>
package parallel.runners;

import com.trivago.trupi.runner.MyCustomCucumberRunner;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(TrupiCucumberRunner.class)
@CucumberOptions(
    monochrome = false,
    features = {"classpath:parallel/features/<b>Example_00001_IT</b>.feature"},
    format = {"json:target/cucumber-report/<b>Example_00001_IT.feature</b>.json"},
    strict = false,
    dryRun = false,
    glue = {"com.trivago.glue"},
    tags = {"~@ignore"}
)
public class <b>Example_00001_IT</b> {
}
</pre>

## 2. Running them with Maven failsafe

This will skip the unit tests (if any) and run the generated runner classes with Failsafe.
Since all generated runner classes from the step before end with ___IT__, they are automatically considered integration tests and run with failsafe.

If all tests should be run regardless of their result, it is important to set ```<testFailureIgnore>true</testFailureIgnore>``` for Failsafe - otherwise the plugin execution will stop on failing tests.
However, if this is specified, the build will not fail in case of failing tests! To circumvent that, it is possible to specify a custom Maven fail rule.

```
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
                <goal>verify</goal>
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
```

## 3. Aggregation of a single test report after all test runs

We use the [Maven Cucumber Reporting](https://mvnrepository.com/artifact/net.masterthought/cucumber-reporting) library to aggregate all generated __.json__ report files into one overall test report.

```
<plugin>
    <groupId>net.masterthought</groupId>
    <artifactId>maven-cucumber-reporting</artifactId>
    <executions>
        <execution>
            <id>report</id>
            <phase>post-integration-test</phase>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}</outputDirectory>
                <cucumberOutput>${project.build.directory}/cucumber-report</cucumberOutput>
                <projectName>My Report</projectName>
                <buildNumber>1</buildNumber>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 4. Passing or failing of the build according to the test results

You can use a custom Maven fail rule that passes or fails the complete build based on test failures. It could check the Failsafe summary report that is generated for each test run.
Without this rule we would have a successful build every time in case we specify.

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <dependencies>
        <dependency>
            <groupId>com.trivago.rta</groupId>
            <artifactId>your-custom-maven-fail-rule</artifactId>
            <version>${fail.rule.version}</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <phase>post-integration-test</phase>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <yourRule implementation="com.example.YourFailRule"/>
                </rules>
                <fail>true</fail>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Example POM

This is the complete Maven profile that is used when invoking

```mvn clean verify -Pparallel```

So all specified plugins will execute one after the other.

```
<profiles>
    <profile>
        <id>parallel</id>
        <build>
            <plugins>
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
                                <sourceRunnerTemplateFile>src/test/resources/parallel/cucable.template</sourceRunnerTemplateFile>
                                <sourceFeatureDirectory>src/test/resources/features</sourceFeatureDirectory>
                                <generatedFeatureDirectory>src/test/resources/parallel/features</generatedFeatureDirectory>
                                <generatedRunnerDirectory>src/test/java/parallel/runners</generatedRunnerDirectory>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
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
                                <goal>verify</goal>
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
                <plugin>
                    <groupId>net.masterthought</groupId>
                    <artifactId>maven-cucumber-reporting</artifactId>
                    <executions>
                        <execution>
                            <id>report</id>
                            <phase>post-integration-test</phase>
                            <goals>
                                <goal>generate</goal>
                            </goals>
                            <configuration>
                                <outputDirectory>${project.build.directory}</outputDirectory>
                                <cucumberOutput>${project.build.directory}/cucumber-report</cucumberOutput>
                                <projectName>My Report</projectName>
                                <buildNumber>1</buildNumber>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-enforcer-plugin</artifactId>
                    <dependencies>
                        <dependency>
                            <groupId>com.trivago.rta</groupId>
                            <artifactId>your-custom-maven-fail-rule</artifactId>
                            <version>${fail.rule.version}</version>
                        </dependency>
                    </dependencies>
                    <executions>
                        <execution>
                            <phase>post-integration-test</phase>
                            <goals>
                                <goal>enforce</goal>
                            </goals>
                            <configuration>
                                <rules>
                                    <yourRule implementation="com.example.YourFailRule"/>
                                </rules>
                                <fail>true</fail>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
        <pluginRepositories>
            <pluginRepository>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
                <id>parallel</id>
                <name>libs-release</name>
                <url>http://artifactory.rta.trivago.trv:8081/artifactory/libs-release</url>
            </pluginRepository>
        </pluginRepositories>
    </profile>
            
```
# Building

Cucable requires Java 8 and it uses Maven for its dependencies.

# Future improvements

* Offer the possibility to generate runners and features directly in the target folder
* Support running specific scenarios in parallel
* Support running the whole test suite multiple times
* Support running single tests multiple times

# License

Copyright 2017 trivago GmbH

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.