Feature: MyTest5
  This is a feature description

  @test
  Scenario Outline: Test <type> with scope <scopeID>
  This is a scenario description

    Given this is a given step
    Then I do something with data
      | taskId | accountName | type   | scopeId   |
      | 515555 | Name        | <type> | <scopeID> |

    Examples:
      | type | scopeID |
      | This | 53      |
      | That | 31      |

  @test
  Scenario Outline: Second test <type> with scope <scopeID>
  This is a scenario description

    Given this is a given step
    Then I do something with data
      | taskId | accountName | type   | scopeId   |
      | 515555 | Name        | <type> | <scopeID> |

    Examples:
      | type           | scopeID |
      | Something else | 1000    |
