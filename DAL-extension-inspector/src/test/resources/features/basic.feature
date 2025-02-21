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
      When you:
      """
      TryIt.DAL: message
      """
      Then you should see:
        """
        TryIt::eventually: {
          Root: ```
                {
                    message: java.lang.String <hello>
                }
                ```

          Error: ''

          Result: ```
                  java.lang.String
                  <hello>
                  ```

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
          Root: ```
                {
                    message: java.lang.String <hello>
                }
                ```

          Error: ```
                 message= world
                          ^

                 Expected to be equal to: java.lang.String
                 <world>
                  ^
                 Actual: java.lang.String
                 <hello>
                  ^
                 ```

          Result: ''

          Inspect: ```
                   message= 'world'
                   ```
        }
        """

    Scenario: auto switch to result when no error
      When you:
      """
      TryIt.DAL: ```
                 'hello'
                 ```
      """
      Then you should see:
        """
        TryIt::eventually: {
          Current: ```
                   java.lang.String
                   <hello>
                   ```
        }
        """
#
#    Scenario: auto switch to error when got error
