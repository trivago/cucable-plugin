@featureTag1
@featureTag2
Feature: test feature 3

  @scenarioOutlineTag1
  @scenarioOutlineTag2
  Scenario Outline: This is a scenario outline with candy '<candy>'
    Given I do something with data
      | givenTable1 | givenTable2 |
    When I do something
    Then I expect a result
    And I expect a second result

  @examples1
    Examples:
      | key | value | candy                |
      | 1   | one   |                      |
      | 2   | two   | a bag of gummy bears |
      | 3   | three | a lollypop           |

  @examples2
    Examples:
      | key | value | candy        |
      | 1   | uno   |              |
      | 2   | dos   | chocolate    |
      | 3   | tres  | cotton candy |