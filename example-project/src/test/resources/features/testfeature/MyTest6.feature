Feature: Scenario with docstring

  @test
  Scenario: Docstring scenario

#    This is a comment

    Given this contains a docstring
    """
    First line
    Second line
    """
    Then Account is created with data
    """
   {
"user": "user@test.com",
"data" : {
"password": "123456"
}
}
    """