package steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.java.no.Gitt;
import cucumber.api.java.no.Og;
import cucumber.api.java.ro.Atunci;
import cucumber.api.java.ro.Cand;
import cucumber.api.java.ro.Datefiind;

public class ExampleSteps {
    @Given("^this is step (\\d+)$")
    public void thisIsStep(int arg0) throws Throwable {
    }

    @When("^I do step (\\d+)$")
    public void iDoStep(int arg0) throws Throwable {
    }

    @Then("^I expect step (\\d+)$")
    public void iExpectStep(int arg0) throws Throwable {
    }

    @When("^I search for$")
    public void iSearchFor() throws Throwable {
    }

    @Then("^I get search results from Germany$")
    public void iGetSearchResultsFromGermany() throws Throwable {
    }

    @Given("^this is background step (\\d+)$")
    public void thisIsBackgroundStep(int arg0) throws Throwable {
    }

    @Then("^I expect a result$")
    public void iExpectAResult() throws Throwable {
    }

    @Given("^As a User \"([^\"]*)\" I'm authorized$")
    public void asAUserIMAuthorized(String arg0) throws Throwable {
    }

    @Then("^Account is created with data:$")
    public void accountIsCreatedWithData() throws Throwable {
    }

    @Given("^this contains a docstring$")
    public void thisContainsADocstring() throws Throwable {
    }

    @Datefiind("^Bună dimineața$")
    public void bunaDimineata() throws Throwable {
    }

    @Cand("^Scuzați-mă!$")
    public void scuzatiMa() throws Throwable {
    }

    @Atunci("^Vă doresc o zi plăcută!$")
    public void vaDorescOZiPlacuta() throws Throwable {
    }

    @Then("^Account is created with data!$")
    public void accountIsCreatedWithData2() throws Throwable {
    }

    @Gitt("^for å unngå at firmaet går konkurs$")
    public void finnish1() throws Throwable {
    }

    @Og("^må regnskapsførerere bruke en regnemaskin for å legge sammen tall$")
    public void finnish2() throws Throwable {
    }

    @Cand("^Scuzați-mă <test>$")
    public void scuzatiMaTest2() throws Throwable {
    }

    @Given("^this is step (\\d+) with a data table$")
    public void thisIsStepWithADataTable(int arg0) throws Throwable {
    }

    @When("^I search for key <key>$")
    public void iSearchForKeyKey() throws Throwable {
    }

    @Then("^I see the value '<value>'$")
    public void iSeeTheValueValue() throws Throwable {
    }

    @And("^I get <candy>$")
    public void iGetCandy() throws Throwable {
    }
}
