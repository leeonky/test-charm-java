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

    Scenario: attach exist suspended test when open new page
      Given Inspector in "FORCED" mode
      And created DAL 'Ins1' with inspector extended
      And use DAL 'Ins1' to evaluating the following:
        """
        1=2
        """
      When launch inspector web page
      Then you should see:
        """
        WorkBench::eventually: {
          Current: {
            header: 'Ins1'
            connected: true
            DAL.value: '1=2'
          }
        }
        """

  Rule: inspect not active, page opened and start web server
    Background:
      Given launch inspector web server
      And launch inspector web page
      And shutdown web server

    Scenario: show all DAL instance name on page
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
        WorkBench::eventually: {
          Current: {
            header: 'Ins1'
            connected: true
          }
        }
        """
      And you should see:
        """
        WorkBench[Ins1]: {
          ::eventually: {
            DAL.value: ```
                       {}
                       ```

            Current: { header: Result }
                   : ```
                     java.lang.String
                     <hello>
                     ```
          }
          Output: {
            Root: ```
                  java.lang.String
                  <hello>
                  ```

            Error: ''

            Inspect: '{}'
          }
       }
       """
      Examples:
        | mode   |
        | FORCED |
        | AUTO   |

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
        WorkBench::eventually: { Current.header: 'Try It!' }
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
        WorkBench.Current.header: Ins1
        """
      And you should see:
        """
        WorkBench[Ins1]: {
          ::eventually: {
            DAL.value: ```
                       1=2
                       ```

            Current: { header: Error }
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
          Output: {
            Root: ```
                  null
                  ```

            Result: ''

            Inspect: '1= 2'
          }
       }
       """
      Examples:
        | mode   |
        | AUTO   |
        | FORCED |

  Rule: release suspend test
    Background: Given FORCED mode DAL Ins1
      When launch inspector web server
      And launch inspector web page
      And created DAL 'Ins1' with inspector extended
      Given Inspector in "FORCED" mode
      And you should see:
      """
      ::eventually : { Monitors[Ins1].value: on }
      """
      Given the 'Ins1' following input:
        """
        {
          "message": "hello"
        }
        """

    Scenario: release ::inspect from current workbench and test got result
      Given use DAL 'Ins1' to evaluating the following:
        """
        message::inspect
        """
      When you:
        """
        Release[Ins1]
        """
      Then DAL 'Ins1' test finished with the following result
        """
        = ```
          hello
          ```
        """
      And you should see:
        """
        WorkBench.Current.connected: false
        """

    Scenario: release failed test from current workbench and test got result
      Given use DAL 'Ins1' to evaluating the following:
        """
        message= world
        """
      When you:
        """
        Release[Ins1]
        """
      Then DAL 'Ins1' test finished with the following result
        """
        message= ```
                 Expected to be equal to: java.lang.String
                 <world>
                  ^
                 Actual: java.lang.String
                 <hello>
                  ^
                 ```
        """

    Scenario: release suspend from remote instance checkbox
      Given use DAL 'Ins1' to evaluating the following:
        """
        message::inspect
        """
      When you:
        """
        Monitors[Ins1]: false
        """
      Then DAL 'Ins1' test finished with the following result
        """
        = ```
          hello
          ```
        """

    Scenario: release all suspend by release all
      Given use DAL 'Ins1' to evaluating the following:
        """
        message= world
        """
      When you:
        """
        ReleaseAll
        """
      Then DAL 'Ins1' test finished with the following result
        """
        message= ```
                 Expected to be equal to: java.lang.String
                 <world>
                  ^
                 Actual: java.lang.String
                 <hello>
                  ^
                 ```
        """
      And you should see:
        """
        WorkBench.Current.connected: false
        """

    Scenario: inspect same DAL twice should reuse workbench
      Given use DAL 'Ins1' to evaluating the following:
        """
        ::inspect
        """
      And you:
        """
        WorkBench.Current.header: 'Ins1'

        ReleaseAll
        """
      When use DAL 'Ins1' to evaluating the following:
        """
        message::inspect
        """
      Then you should see:
        """
        WorkBench.Current::eventually: {
          header: Ins1
          connected: true
        }
        """
      And you should see:
        """
        WorkBench[Ins1]: {
          ::eventually: {
            DAL.value: ```
                       {}
                       ```

            Current: { header: Result }
                   : ```
                     java.lang.String
                     <hello>
                     ```
          }
          Output: {
            Root: ```
                  java.lang.String
                  <hello>
                  ```

            Error: ''

            Inspect: '{}'
          }
       }
       """

    Scenario: pass failed test from current workbench and got default result
      Given use DAL 'Ins1' to evaluating the following:
        """
        message= world
        """
      When you:
        """
        Pass[Ins1]
        """
      Then DAL 'Ins1' test finished with the following result
        """
        = null
        """
      And you should see:
        """
        WorkBench::eventually: { Current.connected: false }
        """

  Rule: concurrent during server and open page
    Background: Given FORCED mode DAL Ins1
      Given Inspector in "FORCED" mode

    Scenario: inspect then launch web page,
      Given created DAL 'Ins1' with inspector extended
      When use DAL 'Ins1' to evaluating the following:
        """
        ::inspect
        """
      And launch inspector web page
      Then you should see:
        """
        WorkBench::eventually: {
          Current: {
            header: Ins1
            connected: true

            DAL.value: ```
                       {}
                       ```
          }
        }
        """

    Scenario: web page pre-opened; then inspect
      Given launch inspector web server
      And launch inspector web page
      And shutdown web server
      When created DAL 'Ins1' with inspector extended
      And use DAL 'Ins1' to evaluating the following:
        """
        ::inspect
        """
      Then you should see:
        """
        WorkBench::eventually: {
          Current: {
            header: Ins1
            connected: true

            DAL.value: ```
                       {}
                       ```
          }
        }
        """

  Rule: clear/disconnected
    Background: workbench connected
      Given Inspector in "FORCED" mode
      And launch inspector web server
      And launch inspector web page
      And created DAL 'Ins1' with inspector extended
      And use DAL 'Ins1' to evaluating the following:
        """
        ::inspect
        """
      And you should see:
        """
        WorkBench::eventually: { Current.connected: true }
        """

    Scenario: should clear connected when disconnected
      When shutdown web server
      Then you should see:
        """
        WorkBench::eventually: {
          Current: {
            DAL.@class= [... disconnected ...]
            connected: false
          }
        }
        """

# muli DAL with same name
