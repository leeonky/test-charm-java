Feature: should

  Scenario: should with 1 arg
    Given the following json:
      """
      {
        "value": "hello"
      }
      """
    Then the following verification should pass:
      """
      value::should.startsWith: hel
      """
    When evaluate by:
      """
      value::should.startsWith: nothel
      """
    Then failed with the message:
      """
      Expected: java.lang.String
      <hello>
      Should startsWith: java.lang.String
      <nothel>
      """
    And got the following notation:
      """
      value::should.startsWith: nothel
                                ^
      """

  Scenario: predicate method not exist
    Given the following json:
      """
      {
        "value": "hello"
      }
      """
    When evaluate by:
      """
      value::should.notExist: any
      """
    Then failed with the message:
      """
      Predicate method notExist not exist in java.lang.String
      <hello>
      """
    And got the following notation:
      """
      value::should.notExist: any
                    ^
      """

  Scenario: raise error when missing parameter
    Given the following java class:
      """
      public class Bean {
        public boolean test(String s1, String s2) {
          return true;
        }
      }
      """
    When use a instance of java class "Bean" to evaluate:
      """
      ::should.test: any
      """
    Then failed with the message:
      """
      Failed to invoke predicate method `test` of #package#Bean {}, maybe missing parameters
      """
    And got the following notation:
      """
      ::should.test: any
                     ^
      """

  Scenario: should with multi parameters
    Given the following java class:
      """
      public class Bean {
        public boolean test(String s1, String s2) {
          return s1.equals(s2);
        }
      }
      """
    Then the following verification for the instance of java class "Bean" should pass:
      """
      ::should.test[t1]: t1
      """

  Scenario: predicate method should return boolean value
    Given the following java class:
      """
      public class Bean {
        public int test(String s1) {
          return s1.length();
        }
      }
      """
    When use a instance of java class "Bean" to evaluate:
      """
      ::should.test: 'hello'
      """
    Then failed with the message:
      """
      Predicate method `test` return type should boolean but java.lang.Integer
      <5>
      """
    And got the following notation:
      """
      ::should.test: 'hello'
                     ^
      """

  Scenario: negative
    Given the following json:
      """
      {
        "value": "hello"
      }
      """
    Then the following verification should pass:
      """
      value::should::not.startsWith: xx
      """
    When evaluate by:
      """
      value::should::not.startsWith: hel
      """
    Then failed with the message:
      """
      Expected: java.lang.String
      <hello>
      Should not startsWith: java.lang.String
      <hel>
      """
    And got the following notation:
      """
      value::should::not.startsWith: hel
                                     ^
      """
