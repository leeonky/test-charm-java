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

    Scenario: execute expression and get result or error
      When given default input value:
        """
        {
          "message": "hello"
        }
        """
      When you:
        """
        TryIt.DAL: message
        """
      Then you should see:
        """
        TryIt::eventually: {
          Current: { type: Result }
                 : ```
                   java.lang.String
                   <hello>
                   ```
        }
        """
      And you should see:
        """
        TryIt: {
          Root: ```
                {
                    message: java.lang.String <hello>
                }
                ```

          Error: ''

          Inspect: message
        }
        """
      When you:
        """
        TryIt.appendDAL: '= world'
        """
      Then you should see:
        """
        TryIt::eventually: {
          Current: { type: Error }
                 : ```
                   message= world
                            ^

                   Expected to be equal to: java.lang.String
                   <world>
                    ^
                   Actual: java.lang.String
                   <hello>
                    ^
                   ```
        }
        """
      And you should see:
        """
        TryIt: {
          Root: ```
                {
                    message: java.lang.String <hello>
                }
                ```

          Result: ''

          Inspect: ```
                   message= 'world'
                   ```
        }
        """
