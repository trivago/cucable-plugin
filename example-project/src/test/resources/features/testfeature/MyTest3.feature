@featureTag1
@featureTag2
Feature: test feature 3

  @scenarioOutlineTag1
  @scenarioOutlineTag2
  Scenario Outline: This is a scenario outline with <candy>
    Given this is step 1
      | givenTable1 | givenTable2 |
    When I search for key <key>
    Then I see the value '<value>'
    And I get <candy>

    Examples:
      | key | value | candy                |
      | 1   | one   ||
      | 2   | two   | a bag of gummy bears |
      | 3   | three | a lollypop           |