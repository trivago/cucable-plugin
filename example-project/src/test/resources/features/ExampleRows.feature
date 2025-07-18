@exampleRows
Feature: ExampleRows

  Scenario Outline: This is a scenario outline with '<candy>'
    Given I do something with '<candy>'
    When I do something
    And I do something with '<candy>'
    And Scuzați-mă <amount>
    Then I expect a result

    Examples:
      | candy      | amount |
      | gummy bear | 1      |
      | lollypop   | 2      |

