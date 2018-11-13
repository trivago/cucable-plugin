package steps;

import cucumber.api.java.en.Given;

public class ExampleSteps {
    @Given("^this is step 1$")
    public void step1()  {
        System.out.println("This is step 1");
    }
}
