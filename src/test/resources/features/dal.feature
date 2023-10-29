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

  Scenario: create with nested object
    When create "商品":
    """
    name: book
    category: {
      id: 100
      name: B01
    }
    """
    Then all "商品" should:
    """
    : | name | category.id | category.name |
      | book | 100         | B01           |
    """

  Scenario: create with list arg
    When create "商品":
    """
    name: book
    labels: [b1 b2]
    """
    Then all "商品" should:
    """
    : [{
      name: book
      labels: [b1 b2]
    }]
    """

  Scenario: create with list object
    When create "商品":
    """
    name: book
    stocks: | size | count |
            | A    | 100   |
    """
    Then all "商品" should:
    """
    : [{
      name: book
      stocks: | size | count |
              | A    | 100   |
    }]
    """
