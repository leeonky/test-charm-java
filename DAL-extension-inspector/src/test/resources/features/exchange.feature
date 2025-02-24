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
          instances: [Ins1 Ins2]
        }
        """

    Scenario: update instance names on page when create more DAL instance with inspector extension
      When launch inspector web page
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      Then you should see:
        """
        ::eventually : {
          instances: [Ins1 Ins2]
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
          instances: [Ins1 Ins2]
        }
        """

    Scenario: update instance names on page when create more DAL instance with inspector extension
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      Then you should see:
        """
        ::eventually : {
          instances: [Ins1 Ins2]
        }
        """

  Rule: launch page and inspect active
    Background: Given DAL Ins1
      When launch inspector web server
      And launch inspector web page
      And created DAL 'Ins1' with inspector extended
      And you should see:
      """
      ::eventually : { instances: [Ins1] }
      """

    Scenario: inspect will suspend, web page will catch the code and result
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

#    Scenario: inspect will not suspend, when skip inspect on web page
#      When you:
#        """
#        skip: [Ins1]
#        """
#      And use DAL 'Ins1' to evaluating the following:
#        """
#        message::inspect
#        """
#      Then DAL 'Ins1' test finished with the following result
#        """
#        java.lang.String
#        <hello>
#        """

# muli DAL with same name
# release
# auto release by button
# auto release by uncheck
