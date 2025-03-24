help: ## Show this help.
	@grep -hE '^[A-Za-z0-9_ \-]*?:.*##.*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
.PHONY: help

build-and-test: ## Build the plugin and run demo tests
	plugin-code/mvnw clean install -f=plugin-code/pom.xml -ntp; \
	cd example-project; \
	../plugin-code/mvnw clean verify -ntp || true; \
	cd ..

show-versions: ## Show most recent dependency versions
	plugin-code/mvnw versions:display-dependency-updates versions:display-plugin-updates -ntp -f=plugin-code/pom.xml
.PHONY: show-versions

deploy: ## Deploy the plugin
	plugin-code/mvnw clean deploy -B -Prelease -no-transfer-progress -f=plugin-code/pom.xml

set-maven-version: ## Change the version of the Maven wrapper
	@if test -z "$(MAVEN_VERSION)"; then echo "No MAVEN_VERSION set!"; exit 1; fi
	mvn wrapper:3.3.2:wrapper -Dmaven=${MAVEN_VERSION} -f=plugin-code/pom.xml
	@plugin-code/mvnw --version
.PHONY: set-maven-version