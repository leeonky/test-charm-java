Feature: dal

  Scenario: create object list with given spec
    When create some "商品":
    """
    : | name | color |
      | book | red   |
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: ignore : create object list with given spec
    When create some "商品":
    """
    | name | color |
    | book | red   |
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: create single object in collection style
    When create some "商品":
    """
    name: book
    color: red
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: create any object in one step
    When create
    """
    商品: {
      name: book
      color: red
    }

    品类: | id | name |
          | C1 | it   |
          | C2 | xx   |
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """
    Then all "品类" should:
    """
    : | id | name |
      | C1 | it   |
      | C2 | xx   |
    """
