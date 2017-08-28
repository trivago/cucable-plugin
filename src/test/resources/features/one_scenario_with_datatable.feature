Feature: FeatureName

  # Scenario with data table

  @tag1
  @tag2
  Scenario: ScenarioName
    Given GivenStep
      | data1 | test1 |
      | data2 | test2 |
    When WhenStep
    And AndStep1
    And AndStep2
    Then ThenStep