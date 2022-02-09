Feature: mul-div

  Scenario Outline: mul precedence
    When evaluate by:
    """
      <input> <operator> <value> * 2
    """
    Then the result should:
    """
    : <result>
    """
    And the inspect should:
    """
    <input> <operator> <value> * 2
    """
    Examples:
      | input | operator | value | result |
      | 10    | +        | 1     | 12     |
      | 10    | -        | 1     | 8      |
      | 10    | *        | 2     | 40     |
      | 12    | /        | 2     | 12     |
      | 10    | >        | 6     | false  |
      | 4     | <        | 3     | true   |
      | 10    | >=       | 6     | false  |
      | 5     | <=       | 4     | true   |
      | 6     | !=       | 3     | false  |

  Scenario Outline: mul parentheses with logic opt
    When evaluate by:
    """
    <input> <opt> 10*true
    """
    Then failed with the message:
    """
    Operands should be number but 'java.lang.Integer' and 'java.lang.Boolean'
    """
    Examples:
      | input | opt  |
      | true  | &&   |
      | true  | and  |
      | false | \|\| |
      | false | or   |

  Scenario: mul with which
    Given the following json:
    """
    {
      "number": 10,
      "operand": 5,
      "obj": {
        "number": 10,
        "operand": 2
      }
    }
    """
    Then the following verification should pass:
    """
    obj which number*operand
    """
    Then the result should:
    """
    : 20
    """

  Scenario Outline: mul with judgement
    * the following verification should pass:
    """
    4 <opt> 2*2
    """
    Examples:
      | opt |
      | :   |
      | =   |

  Scenario Outline: div precedence
    When evaluate by:
    """
      <input> <operator> <value> / 2
    """
    Then the result should:
    """
    : <result>
    """
    And the inspect should:
    """
    <input> <operator> <value> / 2
    """
    Examples:
      | input | operator | value | result |
      | 10    | +        | 2     | 11     |
      | 10    | -        | 2     | 9      |
      | 10    | *        | 2     | 10     |
      | 12    | /        | 2     | 3      |
      | 4     | >        | 6     | true   |
      | 4     | <        | 6     | false  |
      | 4     | >=       | 6     | true   |
      | 4     | <=       | 6     | false  |
      | 3     | !=       | 6     | false  |

  Scenario Outline: div parentheses with logic opt
    When evaluate by:
    """
    <input> <opt> 10/true
    """
    Then failed with the message:
    """
    Operands should be number but 'java.lang.Integer' and 'java.lang.Boolean'
    """
    Examples:
      | input | opt  |
      | true  | &&   |
      | true  | and  |
      | false | \|\| |
      | false | or   |

  Scenario: div with which
    Given the following json:
    """
    {
      "number": 10,
      "operand": 5,
      "obj": {
        "number": 10,
        "operand": 2
      }
    }
    """
    Then the following verification should pass:
    """
    obj which number/operand
    """
    Then the result should:
    """
    : 5
    """

  Scenario Outline: div with judgement
    * the following verification should pass:
    """
    2 <opt> 4/2
    """
    Examples:
      | opt |
      | :   |
      | =   |
