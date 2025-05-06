Feature: value

  Scenario: reference current input value via ::value
    Given the following json:
      """
      {
        "key": "value"
      }
      """
    Then the following verification should pass:
      """
      ::value= {key= value}
      """
    When evaluate by:
      """
      ::value= {key= not}
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
      ::value= {key= not}
                     ^
      """

  Scenario: reference current input value via ::value in object scope
    Given the following json:
      """
      {
        "key": "value"
      }
      """
    Then the following verification should pass:
      """
      :{
        ::value= {key= value}
      }
      """
    When evaluate by:
      """
      : {
        ::value= {key= not}
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
        ::value= {key= not}
                       ^
      }
      """

