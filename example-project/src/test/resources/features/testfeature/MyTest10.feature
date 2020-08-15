@us1
Feature: My feature with tags and tagged examples

  Scenario Outline: Tag test
    Given this is a given step
    When I do something
    Then I am on a page with text '<text>'

    @env
    Examples:
      | text |
      | one  |
      | two  |

    @env2
    Examples:
      | text  |
      | three |
      | four  |

    @env3:
    Examples:
      | text |
      | five |
      | six  |
