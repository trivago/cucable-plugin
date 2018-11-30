Feature: MyTest2

  Background: Background 1 in MyTest2
    Given this is a background step with data
      | bg1 | bg2 |
    And this is a background step

  Scenario: Scenario 1 in MyTest2
    Given this is a given step
    When I do something with data
      | scenarioData1 | scenarioData2 |
      | scenarioData3 | scenarioData4 |
    Then I expect a result

  Scenario: Scenario 2 in MyTest2
    Then I expect a result

  Scenario: Scenario 3 in MyTest2
