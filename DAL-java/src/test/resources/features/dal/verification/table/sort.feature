Feature: sort

  Scenario: support sort list by header from a to z
    When the following json:
    """
    [{
      "name": "Tom"
    },{
      "name": "John"
    }]
    """
    Then the following verification should pass:
    """
    = | ￪ name |
      | 'John' |
      | 'Tom'  |
    """
    And the inspect should:
    """
    = | ￪ name |
    | = 'John' |
    | = 'Tom' |
    """
    Then the following verification should pass:
    """
    = | + name |
      | 'John' |
      | 'Tom'  |
    """
    And the inspect should:
    """
    = | + name |
    | = 'John' |
    | = 'Tom' |
    """
    When evaluate by:
    """
    = | ￪ name |
      | 'John' |
      | 'Lucy' |
    """
    And got the following notation:
    """
    = | ￪ name |
      | 'John' |
      | 'Lucy' |
        ^
    ^^^^^^^^^^^^^
    """

  Scenario: support sort list by header from z to a
    When the following json:
    """
    [{
      "name": "John"
    },{
      "name": "Tom"
    }]
    """
    Then the following verification should pass:
    """
    = | ￬ name |
      | 'Tom'  |
      | 'John' |
    """
    And the inspect should:
    """
    = | ￬ name |
    | = 'Tom' |
    | = 'John' |
    """

  Scenario: support sort list by multi headers before assertion
    When the following json:
    """
    [{
      "name": "Tom",
      "age": 10
    },{
      "name": "John",
      "age": 10
    },{
      "name": "Tomas",
      "age": 20
    }]
    """
    Then the following verification should pass:
    """
    = | ￪ name   | ￬￬ age |
      | 'Tomas'  | 20     |
      | 'John'   | 10     |
      | 'Tom'    | 10     |
    """
    And the inspect should:
    """
    = | ￪ name | ￬￬ age |
    | = 'Tomas' | = 20 |
    | = 'John' | = 10 |
    | = 'Tom' | = 10 |
    """

  Scenario: support custom sorter
    Given the following java class:
      """
      public class Data {
        public List<Integer> list = Arrays.asList(1, 2);
      }
      """
    And register DAL:
      """
      dal.getRuntimeContextBuilder()
        .registerCustomSorter(Integer.class, i-> -1*(int)i);
      """
    Then the following verification for the instance of java class "Data" should pass:
      """
      list: | +intValue |
            | 2         |
            | 1         |
      """
    And the following verification for the instance of java class "Data" should pass:
      """
      (+list)= [2 1]
      """
    And the following verification for the instance of java class "Data" should pass:
      """
      list= +[2 1]
      """
    And the following verification for the instance of java class "Data" should pass:
      """
      list= -[1 2]
      """

