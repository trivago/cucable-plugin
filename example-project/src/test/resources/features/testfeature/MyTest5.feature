Feature: MyTest6
  This is a feature description

  @test
  Scenario Outline: Test
  This is a scenario description

    Given this is a given step
    Then I do something with data
      | taskId | accountName | type   | scopeId   |
      | 515555 | Name        | <type> | <scopeID> |

    Examples:
      | type   | scopeID |
      | Master | 3       |
      | Slave  | 3       |