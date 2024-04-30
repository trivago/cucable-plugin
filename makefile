help: ## Show this help.
	@grep -hE '^[A-Za-z0-9_ \-]*?:.*##.*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
.PHONY: help

build-and-test: ## Build the plugin and run demo tests
	mvn clean install -f=plugin-code/pom.xml -ntp; \
	cd example-project; \
	mvn clean verify -ntp || true; \
	cd ..

show-versions: ## Show most recent dependency versions
	mvn versions:display-dependency-updates -ntp -f=plugin-code/pom.xml
.PHONY: show-versions