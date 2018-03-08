# cucable-test-project

Test project to simulate the behavior of Cucable - a Maven plugin for [Cucumber](https://cucumber.io) scenarios that simplifies parallel test runs.

This test project always uses the latest Cucable version.

## Change plugin configuration

The whole plugin configuration is managed via the pom.xml file in this test project.

## Run the project

To run the project you need to have at least Java 8 and Maven 3.3.9 installed on your system.

Just run the Maven command ```mvn clean verify``` to see the runner and feature generation of Cucable in action.

The runner and feature files are generated inside the `target` directory.