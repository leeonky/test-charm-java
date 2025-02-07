#@inspector
Feature: DAL_INSPECTOR_FORCED

  Background:
    When DAL inspector is in mode 'DAL_INSPECTOR_FORCED'

  Scenario: assert error, test hang, launch inspector then got expression and error
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
