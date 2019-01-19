Feature: Line breaks in tables

  Scenario: Line break in example table
    Given I am on a page with text '<text>'
      | text         |
      | one\ntwo     |
      | two\n\nthree |

  Scenario: Line break in data table
    Given I do something with data
      | test   |
      | 1\n2   |
      | 2\n\n3 |