Feature: attachments

  Background:
    Given launch inspector web server
    And  launch inspector web page
    Given Inspector in "FORCED" mode
    Given created DAL 'Ins1' with inspector extended

  Scenario: support watch
    Given the 'Ins1' following input:
      """
      {
        "string": "hello"
      }
      """
    And use DAL 'Ins1' to evaluating the following:
      """
      ::inspect
      """
    When you2:
      """
      WorkBench::await[Ins1]: { DAL: '.string::watch' }
      WorkBench[Ins1].execute
      """
    Then you should see2:
      """
      WorkBench[Ins1].Output: {
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

#  Scenario: support watch binary as image
#    Given the 'Ins1' binary input:
#      """
#      FF D8
#      """
#    And use DAL 'Ins1' to evaluating the following:
#      """
#      ::inspect
#      """
#    When you:
#      """
#      WorkBench::await[Ins1]: { DAL: '::watch' }
#      WorkBench[Ins1].execute
#      """
#    Then you should see:
#      """
#      WorkBench[Ins1].Watches= {
#            '{}': {
#              <<image.attribute[src] download.attribute[href]>> is URL: {
#                path: '/attachments'
#                query: /name=Ins1&index=0.*/
#              }
#            }
#      }
#      """
#    And "http://www.a.com:10082/attachments?name=Ins1&index=0" should response:
#    """
#    : {
#      code: 200
#      body: ``` HEX
#            FF D8
#            ```
#    }
#    """
