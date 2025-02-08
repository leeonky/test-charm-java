Feature: start inspecting

  Scenario: launch main web form
    When launch inspector
    Then you can see page "DAL inspector"

  Rule: DAL_INSPECTOR_ASSERT_DISABLED

    Background:
      Given DAL inspector is in mode 'DAL_INSPECTOR_ASSERT_DISABLED'

    Scenario: Test failed; the automation exits
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

    Scenario: meta property ::inspect hangs the automation; launching the web page displays the DAL code and result
      Given the following data:
        """
        {
          "value": "hello"
        }
        """
      When evaluating the following:
        """
        value::inspect
        """
      Then test is still running after 1s
      When launch inspector
      Then should display DAL expression:
        """
        {}
        """
      And should show the following result:
        """
        java.lang.String
        <hello>
        """

  Rule: DAL_INSPECTOR_ASSERT_FORCED

    Background:
      Given DAL inspector is in mode 'DAL_INSPECTOR_ASSERT_FORCED'

    Scenario: Failed test hangs the automation; launching the web page displays the DAL code and error
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

    Scenario: meta property ::inspect hangs the automation; launching the web page displays the DAL code and result
      Given the following data:
        """
        {
          "value": "hello"
        }
        """
      When evaluating the following:
        """
        value::inspect
        """
      Then test is still running after 1s
      When launch inspector
      Then should display DAL expression:
        """
        {}
        """
      And should show the following result:
        """
        java.lang.String
        <hello>
        """
