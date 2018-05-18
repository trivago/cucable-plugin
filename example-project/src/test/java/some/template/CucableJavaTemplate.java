package some.template;

import cucumber.api.CucumberOptions;

@CucumberOptions(
        features = {"target/parallel/features/[CUCABLE:FEATURE].feature"},
        plugin = {"json:target/cucumber-report/[CUCABLE:RUNNER].json"}
)
public class CucableJavaTemplate {

}