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
    When you:
      """
      WorkBench::await[Ins1]: { DAL.typeIn: '.string::watch' }
      WorkBench[Ins1].execute
      """
    Then you should see:
      """
      WorkBench[Ins1].Output::eventually: {
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

  Scenario: support watch binary as image
    Given the 'Ins1' binary input:
      """
      FF D8 FF E0 EE
      """
    And use DAL 'Ins1' to evaluating the following:
      """
      ::inspect
      """
    When you:
      """
      WorkBench::await[Ins1]: { DAL.typeIn: '::watch' }
      WorkBench[Ins1].execute
      """
    Then you should see:
      """
      WorkBench[Ins1].Output.Watches::eventually= {
            '{}': {
              <<image.attribute[src] download.attribute[href]>> is URI: {
                path: '/attachments'
                query: /name=Ins1&index=0.*/
              }
            }
      }
      """
    And "http://www.a.com:10082/attachments?name=Ins1&index=0" should response:
      """
      : {
        headers: {
          'Content-Type': 'image/jpeg'
        }

        body: ``` HEX
              FF D8 FF E0 EE
              ```
      }
      """

  Scenario: support watch input-stream
    Given the 'Ins1' input Java Instance:
      """
      public class InputData {
        public java.io.InputStream stream() {
          return new java.io.ByteArrayInputStream("hello".getBytes());
        };
      }
      """
    And use DAL 'Ins1' to evaluating the following:
      """
      ::inspect
      """
    When you:
      """
      WorkBench::await[Ins1]: { DAL.typeIn: '.stream::watch' }
      WorkBench[Ins1].execute
      """
    And "http://www.a.com:10082/attachments?name=Ins1&index=0" should response:
      """
      body.string= hello
      """

  Scenario: support watch Byte[]
    Given the 'Ins1' input Java Instance:
      """
      public class InputData {
        public Byte[] stream() {
            return new Byte[] { (byte)0x01, (byte)0x02 };
        };
      }
      """
    And use DAL 'Ins1' to evaluating the following:
      """
      ::inspect
      """
    When you:
      """
      WorkBench::await[Ins1]: { DAL.typeIn: '.stream::watch' }
      WorkBench[Ins1].execute
      """
    And "http://www.a.com:10082/attachments?name=Ins1&index=0" should response:
      """
      body: ``` HEX
            01 02
            ```
      """

  Scenario: same property name in watch
    Given the 'Ins1' following input:
      """
      [{
        "string": "hello"
      },{
        "string": "world"
      },{
        "string": "!"
      }]
      """
    And use DAL 'Ins1' to evaluating the following:
      """
      ::inspect
      """
    When you:
      """
      WorkBench::await[Ins1]: { DAL.typeIn: '.string[]::watch[]' }
      WorkBench[Ins1].execute
      """
    Then you should see:
      """
      WorkBench[Ins1].Output::eventually: {
        Root: ```
              [
                  {
                      string: java.lang.String <hello>
                  },
                  {
                      string: java.lang.String <world>
                  },
                  {
                      string: java.lang.String <!>
                  }
              ]
              ```

        Result: ```
              [
                  java.lang.String <hello>,
                  java.lang.String <world>,
                  java.lang.String <!>
              ]
                ```

        Error: ''

        Inspect: '{}.string[]::watch[]'

        Watches::eventually= {
            '{}.string[]': ```
                         java.lang.String
                         <hello>
                         ```

            '{}.string[] (1)': ```
                         java.lang.String
                         <world>
                         ```

            '{}.string[] (2)': ```
                         java.lang.String
                         <!>
                         ```
        }
      }
      """
