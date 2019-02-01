Feature: Line breaks in tables

  Scenario Outline: Line break in example table
    Given I am on a page with text '<text>'
    Examples:
      | text         |
      | one\ntwo     |
      | two\n\nthree |

  Scenario: Line break in data table
    Given I do something with data
      | test   |
      | 1\n2   |
      | 2\n\n3 |

  Scenario: Line break in data table
    Given I do something with data
      | test      | test2           |
      | ${test()} | ${test(x(),+1)} |