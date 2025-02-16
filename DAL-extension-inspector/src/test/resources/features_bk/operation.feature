Feature: operation

  Background:
    Given DAL inspector is in mode 'DAL_INSPECTOR_ASSERT_FORCED'
    And launch inspector

  Scenario: sync and show all DAL instance
    When evaluating the following by another DAL "Test2":
    """
    ::inspect
    """
    Then you can see the DAL instances:
      | Test | Test2 |
    And current DAL instance is "Test2"

