@featureTag
Feature: MyTest1

  Scenario: Scenario 1 in MyTest1

  @scenario1Tag1
  @scenario1Tag2
  Scenario: Scenario 2 in MyTest1
    Given I do something

  @scenario2Tag1
  Scenario: Scenario 3 in MyTest1
    Given this is a given step
    When I do something
    Then I expect a result

  Scenario: Scenario 4 in MyTest1

  Scenario: Scenario 5 in MyTest1
    Given this is a given step
    When I do something with data
      | country | Germany |
      | city    |         |
    Then I expect a result