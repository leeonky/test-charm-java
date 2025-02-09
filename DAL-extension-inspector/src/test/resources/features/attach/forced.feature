Feature: attach in forced mode

  Background:
    Given DAL inspector is in mode 'DAL_INSPECTOR_ASSERT_FORCED'

  Rule: Web page not pre-opened

    Scenario: failed test will hang, then open web page will catch the code and result
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
        java.lang.AssertionError:
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

    Scenario: ::inspect will hang, then open web page will catch the code and result
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
