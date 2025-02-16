Feature: resume suspended

  Background:
    Given DAL inspector is in mode 'DAL_INSPECTOR_ASSERT_FORCED'
    And launch inspector

  Scenario: resume ::inspect and execute the left dal code
    Given the following data:
      """
      {
        "value": "hello"
      }
      """
    When evaluating the following:
      """
      value= incorrect
      """
    Then test is still running after 1s
    When resume suspended
    Then test failed with error:
      """
      Expected to be equal to: java.lang.String
      <incorrect>
       ^
      Actual: java.lang.String
      <hello>
       ^
      """

  Scenario: auto resume when not checked relative instance
    Given the following data:
      """
      {
        "value": "hello"
      }
      """
    When not check 'Test'
    And evaluating the following:
      """
      value= incorrect
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
