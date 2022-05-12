Feature: list

  Scenario: empty list
    Given the following json:
    """
    []
    """
    Then the following verification should pass:
    """
    : []
    """
    And the following verification should pass:
    """
    = []
    """

  Scenario: one element
    Given the following json:
    """
    [1]
    """
    Then the following verification should pass:
    """
    : [1]
    """
    When evaluate by:
    """
    : []
    """
    Then failed with the message:
    """
    Different list size
    Expected: <0>
    Actual: <1>
    """
    And got the following notation:
    """
    : []
      ^
    """
    When evaluate by:
    """
    : [2]
    """
    Then failed with the message:
    """
    Expected to match: java.lang.Integer
    <2>
    Actual: java.lang.Integer
    <1>
    """
    And got the following notation:
    """
    : [2]
       ^
    """

  Scenario: default verification operator
    Given the following json:
    """
    [ 1 ]
    """
    When evaluate by:
    """
     = [ /1/ ]
    """
    Then failed with the message:
    """
    Operator = before regex need a string input value
    """
    And got the following notation:
    """
     = [ /1/ ]
     ^
    """

  Scenario: two element
    Given the following json:
    """
    [1, 2]
    """
    Then the following verification should pass:
    """
    : [1 2]
    """
    And the inspect should:
    """
    : [[0]: 1, [1]: 2]
    """
    When evaluate by:
    """
    : [1 3]
    """
    Then failed with the message:
    """
    Expected to match: java.lang.Integer
    <3>
    Actual: java.lang.Integer
    <2>
    """
    And got the following notation:
    """
    : [1 3]
         ^
    """

  Scenario: raise error when no closing bracket
    When evaluate by:
    """
    : [1
    """
    Then failed with the message:
    """
    Should end with `]`
    """
    And got the following notation:
    """
    : [1
        ^
    """

  Scenario: support incomplete List
    Given the following json:
    """
    [ 1, 2, 3 ]
    """
    Then the following verification should pass:
    """
    : [1 ...]
    """
    And the inspect should:
    """
    : [[0]: 1, ...]
    """
    And the following verification should pass:
    """
    : [... 3]
    """
    And the inspect should:
    """
    : [..., [-1]: 3]
    """

  Scenario: should raise error when invalid incomplete List
    When evaluate by:
    """
    : [... 1 ... 2]
    """
    Then failed with the message:
    """
    Invalid ellipsis
    """
    And got the following notation:
    """
    : [... 1 ... 2]
             ^
    """
    When evaluate by:
    """
    : [0 ... 1 ...]
    """
    When evaluate by:
    """
    : [0 ... 1]
    """
    Then failed with the message:
    """
    Invalid ellipsis
    """
    And got the following notation:
    """
    : [0 ... 1]
         ^
    """

  Scenario: support different verification operator in element
    Given the following json:
    """
    [ 1 ]
    """
    Then the following verification should pass:
    """
    = [ : /1/ ]
    """
    And the inspect should:
    """
    = [[0]: /1/]
    """

  Scenario Outline: support expression element
    Given the following json:
    """
    [ 3 ]
    """
    Then the following verification should pass:
    """
    : [ <opt>1+2 ]
    """
    And the inspect should:
    """
    : [[0]<inspect> 1 + 2]
    """
    Examples:
      | opt | inspect |
      |     | :       |
      | :   | :       |
      | =   | =       |

  Scenario: optional comma
    Given the following json:
    """
    [1, 2, 3]
    """
    Then the following verification should pass:
    """
    :[1, 2 3,]
    """
    And the inspect should:
    """
    : [[0]: 1, [1]: 2, [2]: 3]
    """

  Scenario: comma as logic and in parentheses
    Given the following json:
    """
    [ true, false ]
    """
    Then the following verification should pass:
    """
    : [(true,true), false]
    """

  Scenario: support schema expression
    Given the following json:
    """
    [ "1" ]
    """
    Then the following verification should pass:
    """
    : [ is String ]
    """
    And the inspect should:
    """
    : [[0] is String]
    """
    And the following verification should failed:
    """
    : [ is Integer ]
    """
    And got the following notation:
    """
    : [ is Integer ]
           ^
    """
    And the following verification should pass:
    """
    : [ is String: {length: 1} ]
    """
    And the inspect should:
    """
    : [[0] is String: {length: 1}]
    """

  Scenario: change first element index
    Given the following java class:
    """
    public class Table extends java.util.ArrayList<Row> {
      public Table() {
        Row row = new Row();
        row.v1 = 11;
        row.v2 = 12;
        add(row);

        row = new Row();
        row.v1 = 21;
        row.v2 = 22;
        add(row);
      }
    }
    """
    And the following java class:
    """
    public class Row {
      public int v1, v2;
    }
    """
    And set the first element index to 1 of list "Table"
    Then use a instance of java class "Table" to evaluate:
    """
    : {
      [1]: {
        v1: 11
        v2: 12
      }
      [2]: {
        v1: 21
        v2: 22
      }
    }
    """
    And the following verification should pass:
    """
    [-2]: {
      v1: 11
      v2: 12
    }
    """
    And the following verification should pass:
    """
    : | v1 | v2 |
    1 | 11 | 12 |
    2 | 21 | 22 |
    """
    And the following verification should pass:
    """
    :   | >> |  1 |  2 |
        | v1 | 11 | 21 |
        | v2 | 12 | 22 |
    """
    And the following verification should pass:
    """
    : [{
      v1: 11
      v2: 12
    }{
      v1: 21
      v2: 22
    }]
    """
    And the following verification should pass:
    """
    : [{
      v1: 11
      v2: 12
    } ...]
    """
    And the following verification should pass:
    """
    : [... {
      v1: 21
      v2: 22
    }]
    """
    And the following verification should pass:
    """
    : | v1 | v2 |
      | 11 | 12 |
      | 21 | 22 |
    """
    And the following verification should pass:
    """
    : >>| v1 | 11 | 21 |
        | v2 | 12 | 22 |
    """

  Scenario: list "contains"
    Given the following json:
    """
    [1, 2, 3, 4]
    """
    Then the following verification should pass:
    """
    : [... 1 3 ...]
    """
    And the inspect should:
    """
    : [..., : 1, : 3, ...]
    """
    When evaluate by:
    """
    : [... 1 5 ...]
    """
    Then failed with the message:
    """
    No such element
    """
    And got the following notation:
    """
    : [... 1 5 ...]
             ^
    """

  Scenario: list contains object
    Given the following json:
    """
    [{
      "name": "Tom",
      "age": 18
    }, {
      "name": "John",
      "age": 20
    }]
    """
    Then the following verification should pass:
    """
    : [... {
      name: Tom
    } ...]
    """
    And the inspect should:
    """
    : [..., : {name: 'Tom'}, ...]
    """
    When evaluate by:
    """
    : [...
    {
      name: John
    } {
      age: 30
    }
    ...]
    """
    Then failed with the message:
    """
    No such element
    """
    And got the following notation:
    """
    : [...
    {
      name: John
    } {
      ^
      age: 30
    }
    ...]
    """

#  TODO contains in table
#  TODO contains not allow set index or key in table

#    TODO try to support [1 ... 2] ?