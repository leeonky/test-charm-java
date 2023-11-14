Feature: single

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
    { name tom
      color: red
    }
    """
    Then got following exception:
    """
    message: ```

             { name tom
                    ^
               color: red
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
      | book | '100'       | B01           |
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

  Scenario: create with list object and specify index, skipped element will be null
    When create "商品":
    """
    stocks: | size |
          1 | A    |
    """
    Then all "商品" should:
    """
    [0].stocks: {
      [0]: null,
      [1].size= A
    }
    """

  Scenario: create with list object and skip one row with ***, skipped element will be null
    When create "商品":
    """
    stocks: | size |
            | ***  |
            | A    |
    """
    Then all "商品" should:
    """
    [0].stocks: {
      [0]: null,
      [1].size= A
    }
    """

  Scenario: create with list object and skip all colmun with *, skipped will be attach or create any same type object
    When create "商品":
    """
    stocks: | size |
            | *    |
            | A    |
    """
    Then all "商品" should:
    """
    [0].stocks: {
      [0].class.simpleName= ProductStock
      [1].size= A
    }
    """

  Scenario: pass raw map to input property
    When create "商品":
    """
    name: book
    object= {
      key: k
      value: v
    }
    """
    Then all "商品" should:
    """
    : [{
      name: book
      object: {
        key: k
        value: v
      }
    }]
    """

  Scenario: pass raw empty map to input property
    When create "商品":
    """
    name: book
    object= {}
    """
    Then all "商品" should:
    """
    : [{
      name: book
      object= {}
    }]
    """

  Scenario: pass raw list to input property
    When create "商品":
    """
    name: book
    object= [a b c]
    """
    Then all "商品" should:
    """
    : [{
      name: book
      object= [a b c]
    }]
    """

  Scenario: pass raw list with object element to input property
    When create "商品":
    """
    name: book
    object= [{
      key: k
      value: v
    }]
    """
    Then all "商品" should:
    """
    : [{
      name: book
      object= [{
        key: k
        value: v
      }]
    }]
    """

  Scenario: pass raw empty list to input property
    When create "商品":
    """
    name: book
    object= []
    """
    Then all "商品" should:
    """
    : [{
      name: book
      object= []
    }]
    """

  Scenario: link any exist object when use : {...}
    When create "商品":
    """
    name: book
    """
    When create "库存":
    """
    product: {...}
    size: A1
    count: 1
    """
    Then all "商品" should:
    """
    : [{ name: book }]
    """
    Then all "库存" should:
    """
    : [{
      product.name: book
      size: A1
      count: 1
    }]
    """

  Scenario: create object when use : {...} and no any exist instance
    When create "库存":
    """
    product: {...}
    size: A1
    count: 1
    """
    Then all "商品" should:
    """
    ::size= 1
    """
    Then all "库存" should:
    """
    : [{
      product: {...}
      size: A1
      count: 1
    }]
    """

  Scenario: when use : [] pass raw empty list to property
    When create "商品":
    """
    name: book
    object: []
    stocks: []
    """
    Then all "商品" should:
    """
    : [{
      name: book
      object: []
      stocks: []
    }]
    """
