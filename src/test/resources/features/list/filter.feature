Feature: filter

  Scenario: filter property by match object verification
    Given the following json:
    """
    [{
      "type": "A",
      "value": "1"
    }, {
      "type": "B",
      "value": "b"
    }, {
      "type": "A",
      "value": "2"
    }]
    """
    Then the following should pass:
    """
    ::filter: {type= A}
    = | type | value |
      | A    | '1'   |
      | A    | '2'   |
    """

  Scenario: filter property by equal to object verification
    Given the following json:
    """
    [{
      "type": "A",
      "value": "1"
    }, {
      "type": "A",
      "value": "1",
      "any": "any"
    }]
    """
    Then the following should pass:
    """
    ::filter= {type= A value= '1'}
    = | type | value |
      | A    | '1'   |
    """

  Scenario: filter property by match list element verification
    Given the following json:
    """
    [[{
      "type": "A",
      "value": "1"
    }], [{
      "type": "B",
      "value": "b"
    }], [{
      "type": "A",
      "value": "2"
    }]]
    """
    Then the following should pass:
    """
    ::filter: [{type= A}]
      =    | type | value |
    [0][0] | A    | '1'   |
    [1][0] | A    | '2'   |
    """

  Scenario: filter property by equal to list element verification
    Given the following json:
    """
    [[{
      "type": "A",
      "value": "1"
    }], [{
      "type": "A",
      "value": "1",
      "any": "any"
    }]]
    """
    Then the following should pass:
    """
    ::filter= [{type= A, value: '1'}]
      =    | type | value |
    [0][0] | A    | '1'   |
    """

  Scenario: should raise syntax error
    When evaluate by:
    """
    ::filter: {a + b}
    """
    Then failed with the message:
    """

    ::filter: {a + b}
                 ^

    Expect a verification operator

    The root value was: null
    """

  Scenario: raise error when input is not list
    When evaluate by:
    """
    ::filter: {a: 1}
    """
    Then failed with the message:
    """

    ::filter: {a: 1}
      ^

    Invalid input value, expect a List but: null

    The root value was: null
    """
