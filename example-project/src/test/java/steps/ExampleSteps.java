package steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.no.Gitt;
import io.cucumber.java.no.Og;
import io.cucumber.java.ro.Atunci;
import io.cucumber.java.ro.Cand;
import io.cucumber.java.ro.Datefiind;

@SuppressWarnings("unused")
public class ExampleSteps {
    @Given("^this is a given step$")
    public void thisIsAGivenStep() {
    }

    @When("^I do something$")
    public void iDoSomething() {
    }

    @Then("^I expect a result$")
    public void iExpectAResult() {
    }

    @And("^I expect a second result$")
    public void iExpectASecondResult() {
    }

    @Given("^this is background given step$")
    public void thisIsBackgroundGivenStep() {
    }

    @And("^this is another background step$")
    public void thisIsAnotherBackgroundStep() {
    }

    @When("^I do something with data$")
    public void iDoSomethingWithData(DataTable dataTable) {
    }

    @Given("^this is a background step with data$")
    public void thisIsABackgroundStepWithData(DataTable dataTable) {
    }

    @And("^this is a background step$")
    public void thisIsABackgroundStep() {
    }

    @Given("^this is a given step with a docstring$")
    public void thisIsAGivenStepWithADocstring(DocString docString) {
    }

    @Then("^I do something with a docstring$")
    public void iDoSomethingWithADocstring(DocString docString) {
    }

    @Gitt("^for å unngå at firmaet går konkurs$")
    public void norwegian1() {
    }

    @Og("^må regnskapsførerere bruke en regnemaskin for å legge sammen tall$")
    public void norwegian2() {
    }

    @Datefiind("^Bună dimineața$")
    public void romanian1() {
    }

    @Cand("^Scuzați-mă!$")
    public void romanian2() {
    }

    @Atunci("^Vă doresc o zi plăcută!$")
    public void romanian3() {
    }

    @Cand("^Scuzați-mă <test>$")
    public void romanianDataTable(DataTable dataTable) {
    }

    @Cand("^Scuzați-mă (\\d)$")
    public void romanianValue(int value) {
    }

    @Given("^I am on a page with text '(.*)'$")
    public void iAmOnAPageWithTextText(String text) {
    }
}
