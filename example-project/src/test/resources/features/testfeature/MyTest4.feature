@featureTag
Feature: skip a scenario

  @skipMe
  Scenario: This is a scenario that should not be generated
    Given this is step 1
    Then I expect step 2