@featureTag
Feature: test feature

  Scenario: This is a scenario without steps

  @scenario1Tag1
  @scenario1Tag2
  Scenario: This is a scenario with one step
    Given this is step 1

  @scenario2Tag1
  Scenario: This is another scenario with 3 steps
    Given this is step 1
    When I do step 2
    Then I expect step 3

  Scenario: This is another scenario without steps

  Scenario: this is a scenario with steps containing a data table
    Given this is step 1
    When I search for
      | country | Germany |
      | city    |         |
    Then I get search results from Germany