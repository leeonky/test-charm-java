Feature: basic
  Rule: web server ready, web page opened

    Background:
      Given launch inspector web server
      And  launch inspector web page

    Scenario: launch server and page
      Then you should see:
      """
      title: 'DAL inspector'
      """

    Scenario: execute expression and get result
      When given default input value:
        """
        {
          "message": "hello"
        }
        """
      When try dal on page:
        """
        message
        """
      Then yon can see the 'Root':
        """
        {
            message: java.lang.String <hello>
        }
        """
      And yon can see the 'Error':
        """
        """
      And yon can see the 'Result':
        """
        java.lang.String
        <hello>
        """
      And yon can see the 'Inspect':
        """
        message
        """
      When append try dal on page:
        """
        = world
        """
      Then yon can see the Error:
        """
        message= world
                 ^

        Expected to be equal to: java.lang.String
        <world>
         ^
        Actual: java.lang.String
        <hello>
         ^
        """

#    Scenario: auto switch to result when no error
#      When try dal on page:
#        """
#        'hello'
#        """
#      And page switch to 'Result' and yon can see:
#        """
#        java.lang.String
#        <hello>
#        """
#
#    Scenario: auto switch to error when got error
