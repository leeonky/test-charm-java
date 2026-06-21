Feature: proxy object

  Scenario: define Object for DAL via ProxyObject
    Given the following java class:
      """
      public class Data implements ProxyObject {
        public Object getValue(Object key) {
          return key + "_value";
        }
      }
      """
    Then the following verification for the instance of java class "Data" should pass:
      """
      key: key_value
      """

  Scenario: default keySet of ProxyObject is empty
    Given the following java class:
      """
      public class Data implements ProxyObject {
        public Object getValue(Object key) {
          return "any";
        }
      }
      """
    Then the following verification for the instance of java class "Data" should pass:
      """
      ::keys: []
      """
