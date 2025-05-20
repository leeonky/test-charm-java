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
      :    | type | value |
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
      :    | type | value |
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

  Scenario: filter! result should have at least one element
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
    {}::filter!: {type= A}
      : [* *]
    """

  Scenario: raise error when no data in filter!
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
    When evaluate by:
    """
    {}::filter!: {type= C}
    """
    Then failed with the message:
    """
    {}::filter!: {type= C}
              ^

    Filtered result is empty, try again

    The root value was: [
        {
            type: java.lang.String <A>,
            value: java.lang.String <1>
        },
        {
            type: java.lang.String <B>,
            value: java.lang.String <b>
        },
        {
            type: java.lang.String <A>,
            value: java.lang.String <2>
        }
    ]
    """

  Scenario: filter(2) result should return top 2 of filtered result
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
    }, {
      "type": "A",
      "value": "3"
    }]
    """
    Then the following should pass:
    """
    {}::filter(2): {type= A}
      : | type | value |
        | A    | '1'   |
        | A    | '2'   |
    """

  Scenario: raise error when filter(2) result less than two elements
    Given the following json:
    """
    [{
      "type": "A",
      "value": "1"
    }, {
      "type": "B",
      "value": "b"
    }]
    """
    When evaluate by:
    """
    {}::filter(2): {type= A}
    """
    Then failed with the message:
    """
    {}::filter(2): {type= A}
               ^

    There are only 1 elements, try again

    The root value was: [
        {
            type: java.lang.String <A>,
            value: java.lang.String <1>
        },
        {
            type: java.lang.String <B>,
            value: java.lang.String <b>
        }
    ]
    """

  Scenario: filter result is adaptive list
    Given the following json:
    """
    [{
      "type": "A",
      "value": "1"
    }, {
      "type": "B",
      "value": "b"
    }]
    """
    When evaluate by:
    """
    ::filter: {type= A}
      = {
        type= A
        value= 1
      }
    """

  Scenario: filter adaptive list
    Given the following java class:
      """
      public class Test {
        public AdaptiveList<String> getList() {
          return AdaptiveList.staticList(Arrays.asList("a", "abc"));
        }
      }
      """
    Then the following should pass:
      """
      list::filter: {length= 3}
      = [abc]
      """
