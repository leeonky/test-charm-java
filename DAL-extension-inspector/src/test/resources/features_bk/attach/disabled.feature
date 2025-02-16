Feature: attach in disabled mode

  Background:
    Given DAL inspector is in mode 'DAL_INSPECTOR_ASSERT_DISABLED'

  Rule: Web page and Server not pre-opened

    Scenario: failed tests will not suspend
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

    Scenario: ::inspect will suspend, then open web page will catch the code and result
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

  Rule: Web page and Server pre-opened

    Background:
      Given launch inspector

    Scenario: Web page opened, test fails and exits.
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

    Scenario: Web page opened, ::inspect will suspend
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
      Then should display DAL expression:
        """
        {}
        """
      And should show the following result:
        """
        java.lang.String
        <hello>
        """

  Rule: Web page pre-opened without Server

    Background:
      Given launch inspector
      And restart inspector

    Scenario: failed tests will not suspend
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

    Scenario: ::inspect will suspend, then open web page will catch the code and result - 2
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
      And should display DAL expression:
        """
        {}
        """
      And should show the following result:
        """
        java.lang.String
        <hello>
        """
