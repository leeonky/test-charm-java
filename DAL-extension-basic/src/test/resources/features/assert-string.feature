Feature: assert string

  Rule: equal to

    Scenario: show escaped string in expect and actual
      Given the following json:
      """
      {
        "value": "hello\t";
      }
      """
      When evaluate by:
      """
      value= "hello"
      """
      Then failed with the message:
      """
      value= "hello"
             ^

      Expected to be equal to: java.lang.String
      <hello>
            ^
      Actual: java.lang.String
      <hello\t>
            ^

      The root value was: {
          value: java.lang.String <hello\t>
      }
      """

    Scenario: show further diff info when text in multi line
      Given the following json:
      """
      {
        "value": "hello\nworld";
      }
      """
      When evaluate by:
      """
      value= "hello\nWorld"
      """
      Then failed with the message:
      """
      value= "hello\nWorld"
             ^

      Expected to be equal to: java.lang.String
      <hello\nWorld>
              ^
      Actual: java.lang.String
      <hello\nworld>
              ^

      Detail:
      Expect: | Actual:
      --------|--------
      hello   | hello
      World   | world
      ^       | ^

      The root value was: {
          value: java.lang.String <hello\nworld>
      }
      """

    Scenario: expect null equal to string
      Given the following json:
      """
      {
        "value": null
      }
      """
      When evaluate by:
      """
      value= "hello\nWorld"
      """
      Then failed with the message:
      """
      value= "hello\nWorld"
             ^

      Expected to be equal to: java.lang.String
                               ^
      <hello\nWorld>
      Actual: null
              ^

      The root value was: {
          value: null
      }
      """

  Rule: match

    Scenario: show escaped string in expect and actual
      Given the following json:
      """
      {
        "value": "hello\t";
      }
      """
      When evaluate by:
      """
      value: "hello"
      """
      Then failed with the message:
      """
      value: "hello"
             ^

      Expected to match: java.lang.String
      <hello>
            ^
      Actual: java.lang.String
      <hello\t>
            ^

      The root value was: {
          value: java.lang.String <hello\t>
      }
      """

    Scenario: show further diff info when text in multi line
      Given the following json:
      """
      {
        "value": "hello\nworld";
      }
      """
      When evaluate by:
      """
      value: "hello\nWorld"
      """
      Then failed with the message:
      """
      value: "hello\nWorld"
             ^

      Expected to match: java.lang.String
      <hello\nWorld>
              ^
      Actual: java.lang.String
      <hello\nworld>
              ^

      Detail:
      Expect: | Actual:
      --------|--------
      hello   | hello
      World   | world
      ^       | ^

      The root value was: {
          value: java.lang.String <hello\nworld>
      }
      """

    Scenario: expect null match string
      Given the following json:
      """
      {
        "value": null
      }
      """
      When evaluate by:
      """
      value: "hello\nWorld"
      """
      Then failed with the message:
      """
      value: "hello\nWorld"
             ^

      Expected to match: java.lang.String
                         ^
      <hello\nWorld>
      Actual: null
              ^

      The root value was: {
          value: null
      }
      """

    Scenario: not allow convert number to string implicitly
      When evaluate by:
      """
      5: '5'
      """
      Then failed with the message:
      """
      5: '5'
         ^

      Cannot compare between java.lang.Integer
      <5>
      and java.lang.String
      <5>

      The root value was: null
      """

    Scenario: not allow convert boolean to string implicitly
      When evaluate by:
      """
      true: 'true'
      """
      Then failed with the message:
      """
      true: 'true'
            ^

      Cannot compare between java.lang.Boolean
      <true>
      and java.lang.String
      <true>

      The root value was: null
      """
