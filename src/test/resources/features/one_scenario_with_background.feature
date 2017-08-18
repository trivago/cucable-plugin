Feature: FeatureName

  # Scenario with background

  Background:
    Given BackgroundGivenStep
    And BackgroundGivenStep2

  @tag1
  @tag2
  Scenario: ScenarioName
    Given GivenStep
    When WhenStep
    And AndStep1
    And AndStep2
    Then ThenStep