package some.template;

import cucumber.api.CucumberOptions;

@CucumberOptions(
        features = {"target/parallel/features/[FEATURE_FILE_NAME].feature"},
        plugin = {"json:target/cucumber-report/[FEATURE_FILE_NAME].json"}
)
public class CucableJavaTemplate {

}