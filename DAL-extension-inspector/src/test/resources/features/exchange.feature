Feature: exchange

  Rule: inspect not active, web server started and launch page
    Background:
      When launch inspector web server

    Scenario: show all DAL instance name on page
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      When launch inspector web page
      Then you should see:
        """
        ::eventually : {
//        TODO refactor
          Monitors=  | value |
               Ins1: | on   |
               Ins2: | on   |
        }
        """

    Scenario: update instance names on page when create more DAL instance with inspector extension
      When launch inspector web page
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      Then you should see:
        """
        ::eventually : {
          Monitors=  | value |
               Ins1: | on   |
               Ins2: | on   |
        }
        """

  Rule: inspect not active, launch opened and start web server
    Background:
      Given launch inspector web server
      And launch inspector web page
      And shutdown web server

    Scenario: show all DAL instance name on page
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      When launch inspector web server
      Then you should see:
        """
        ::eventually : {
          Monitors=  | value |
               Ins1: | on   |
               Ins2: | on   |
        }
        """

    Scenario: update instance names on page when create more DAL instance with inspector extension
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      Then you should see:
        """
        ::eventually : {
          Monitors=  | value |
               Ins1: | on   |
               Ins2: | on   |
        }
        """

  Rule: launch page and inspect active
    Background: Given DAL Ins1
      When launch inspector web server
      And launch inspector web page
      And created DAL 'Ins1' with inspector extended
      And you should see:
      """
      ::eventually : { Monitors[Ins1].value: on }
      """

    Scenario Outline: ::inspect will suspend, web page will catch the code and result in both AUTO and FORCE mode
      Given Inspector in "<mode>" mode
      And the 'Ins1' following input:
        """
        {
          "message": "hello"
        }
        """
      When use DAL 'Ins1' to evaluating the following:
        """
        message::inspect
        """
      Then 'Ins1' test still run after 1s
      And you should see:
        """
        WorkBench.Current: {
          name: 'Ins1'
        }
        """
      And you should see:
        """
        WorkBench[Ins1]: {
          ::eventually: {
            DAL.value: ```
                       {}
                       ```

            Current: { type: Result }
                   : ```
                     java.lang.String
                     <hello>
                     ```
          }

          Root: ```
                java.lang.String
                <hello>
                ```

          Error: ''

          Inspect: '{}'
       }
       """
      Examples:
        | mode   |
        | AUTO   |
        | FORCED |

    Scenario: ::inspect will not suspend, when skip inspect on web page in AUTO mode
      Given Inspector in "AUTO" mode
      Given the 'Ins1' following input:
        """
        {
          "message": "hello"
        }
        """
      When you:
        """
        Monitors[Ins1]: false
        """
      And use DAL 'Ins1' to evaluating the following:
        """
        message::inspect
        """
      Then DAL 'Ins1' test finished with the following result
        """
        = ```
          hello
          ```
        """
      And you should see:
        """
        WorkBench.Current: {
          name: 'Try It!'
        }
        """

    Scenario: ::inspect will still suspend, when skip inspect on web page in FORCED mode
      Given Inspector in "FORCED" mode
      Given the 'Ins1' following input:
        """
        {
          "message": "hello"
        }
        """
      When you:
        """
        Monitors[Ins1]: false
        """
      And use DAL 'Ins1' to evaluating the following:
        """
        message::inspect
        """
      Then 'Ins1' test still run after 1s

    Scenario Outline: failed test will suspend, web page will catch the code and result in both AUTO and FORCE mode
      Given Inspector in "<mode>" mode
      When use DAL 'Ins1' to evaluating the following:
        """
        1=2
        """
      Then 'Ins1' test still run after 1s
      And you should see:
        """
        WorkBench.Current: {
          name: 'Ins1'
        }
        """
      And you should see:
        """
        WorkBench[Ins1]: {
          ::eventually: {
            DAL.value: ```
                       1=2
                       ```

            Current: { type: Error }
                   : ```
                     1=2
                       ^

                     Expected to be equal to: java.lang.Integer
                     <2>
                      ^
                     Actual: java.lang.Integer
                     <1>
                      ^
                     ```
          }

          Root: ```
                null
                ```

          Result: ''

          Inspect: '1= 2'
       }
       """
      Examples:
        | mode   |
        | AUTO   |
        | FORCED |

# muli DAL with same name
# release
# auto release by button
# auto release by uncheck
