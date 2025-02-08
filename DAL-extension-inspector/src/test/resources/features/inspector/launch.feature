Feature: launch inspector

  Scenario: launch main web form
    When launch inspector
    Then you can see page "DAL inspector"

  Scenario: DAL_INSPECTOR_ASSERT_FORCED: test failed and hang then launch inspector and got expression and error
    Given DAL inspector is in mode 'DAL_INSPECTOR_ASSERT_FORCED'
    Given the following data:
      """
      {
        "value": "hello"
      }
      """
    When evaluating the following:
      """
      : {
        value= incorrect
      }
      """
    Then test is still running after 1s
    When launch inspector
    Then should display the same DAL expression
    And should show the following result:
      """
      : {
        value= incorrect
               ^
      }

      Expected to be equal to: java.lang.String
      <incorrect>
       ^
      Actual: java.lang.String
      <hello>
       ^

      The root value was: {
          value: java.lang.String <hello>
      }
      """

  Scenario: DAL_INSPECTOR_DISABLED: test failed and exit no hanging
    Given DAL inspector is in mode 'DAL_INSPECTOR_ASSERT_DISABLED'
    Given the following data:
      """
      {
        "value": "hello"
      }
      """
    When evaluating the following:
      """
      : {
        value= incorrect
      }
      """
    Then test failed with error:
      """
      Expected to be equal to: java.lang.String
      <incorrect>
       ^
      Actual: java.lang.String
      <hello>
       ^
      """
