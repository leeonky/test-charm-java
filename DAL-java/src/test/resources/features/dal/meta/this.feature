Feature: value

  Scenario: reference current input value via ::this
    Given the following json:
      """
      {
        "key": "value"
      }
      """
    Then the following verification should pass:
      """
      ::this= {key= value}
      """
    When evaluate by:
      """
      ::this= {key= not}
      """
    Then failed with the message:
      """
      Expected to be equal to: java.lang.String
      <not>
       ^
      Actual: java.lang.String
      <value>
       ^
      """
    And got the following notation:
      """
      ::this= {key= not}
                    ^
      """

  Scenario: reference current input value via ::this in object scope
    Given the following json:
      """
      {
        "key": "value"
      }
      """
    Then the following verification should pass:
      """
      :{
        ::this= {key= value}
      }
      """
    When evaluate by:
      """
      : {
        ::this= {key= not}
      }
      """
    Then failed with the message:
      """
      Expected to be equal to: java.lang.String
      <not>
       ^
      Actual: java.lang.String
      <value>
       ^
      """
    And got the following notation:
      """
      : {
        ::this= {key= not}
                      ^
      }
      """

