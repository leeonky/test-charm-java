Feature: prepare data by dal

  Scenario: create one object with given spec
    When create "商品":
    """
    name: book
    color: red
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: create one object with given spec in {}
    When create "商品":
    """
    : {
      name: book
      color: red
    }
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: create one object with given spec in {}, (ignore begin :)
    When create "商品":
    """
    {
      name: book
      color: red
    }
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: error notation in {}
    When try to create "商品":
    """
    {
      name
      color: red
    }
    """
    Then got following exception:
    """
    message: ```

             {
               name
               color: red
               ^
             }

             Expect a verification operator
             ```
    """

#  Scenario: create object list with given spec
#    When create "商品":
#    """
#    : | name | color |
#      | book | red   |
#    """
#    Then all "商品" should:
#    """
#    : | name | color |
#      | book | red   |
#    """
