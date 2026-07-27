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
      And you should see:
        """
        WorkBench::eventually: {
          Current.header: 'Try It!'
        }
        """

    Scenario: auto execute expression and get result or error
      When given default input value:
        """
        {
          "message": "hello"
        }
        """
      When you:
        """
        WorkBench[Try It!].DAL.typeIn: message
        """
      Then you should see:
        """
        WorkBench[Try It!]::eventually: {
          DAL.@class= [... result ...]

          Current: {
            header: Result
          } : ```
             java.lang.String
             <hello>
             ```
        }
        """
      And you should see:
        """
        WorkBench[Try It!].Output::eventually: {
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
        WorkBench[Try It!].DAL.typeIn: '= world'
        """
      Then you should see:
        """
        WorkBench[Try It!]::eventually: {
          DAL.@class= [... error ...]

          Current: { header: Error }
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
        WorkBench[Try It!].Output::eventually: {
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

    Scenario: manual execute expression and get result or error
      When given default input value:
        """
        {
          "message": "hello"
        }
        """
      When you:
        """
        AutoExecute.fillIn: false
        WorkBench[Try It!].DAL.typeIn: message
        """
      Then you should see after 1s:
        """
        WorkBench[Try It!]::eventually: {
          DAL.@class= [code-editor]
          Output.Result: ''
        }
        """
      When you:
        """
          WorkBench[Try It!].execute
        """
      Then you should see:
        """
        WorkBench[Try It!]::eventually: {
          DAL.@class= [... result ...]

          Current: {
            header: Result
          } : ```
             java.lang.String
             <hello>
             ```
        }
        """
      When you:
        """
        WorkBench[Try It!].DAL.typeIn: '= world'
        """
      When you:
        """
          WorkBench[Try It!].execute
        """
      Then you should see:
        """
        WorkBench[Try It!]::eventually: {
          DAL.@class= [... error ...]

          Current: { header: Error }
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

    Scenario: editor in editing state
      When shutdown web server
      And you:
        """
        WorkBench[Try It!].DAL.typeIn: message
        """
      Then you should see:
        """
        WorkBench::eventually: {
          Current: {
            DAL.@class= [... executing ...]
          }
        }
        """
