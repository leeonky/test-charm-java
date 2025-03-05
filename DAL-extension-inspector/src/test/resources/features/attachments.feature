Feature: attachments

  Background:
    Given launch inspector web server
    And  launch inspector web page

  Scenario: support watch
    Given Inspector in "FORCED" mode
    Given created DAL 'Ins1' with inspector extended
    And the 'Ins1' following input:
    """
        {
          "string": "hello"
        }
        """
    And use DAL 'Ins1' to evaluating the following:
        """
        ::inspect
        """
    When you:
      """
      WorkBench::await[Ins1]: { DAL: '.string::watch' }
      WorkBench[Ins1].execute
      """
    Then you should see:
      """
      WorkBench[Ins1]::eventually: {
        Root: ```
              {
                  string: java.lang.String <hello>
              }
              ```

        Result: ```
                java.lang.String
                <hello>
                ```

        Error: ''

        Inspect: '{}.string::watch'

        Watches= {
            '{}.string': ```
                         java.lang.String
                         <hello>
                         ```
        }
      }
      """
