Feature: Flat List
  Use `: []` to Create List by JFactory

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following declarations:
      """
      Collector collector = jFactory.collector(java.util.LinkedList.class);
      """

  Scenario: List
    When "collector" collect with the following properties:
      """
      : [hello world]
      """
    Then the result should be:
      """
      : {
        ::properties= {
          '[0]'= hello
          '[1]'= world
        }
        ::build: {
          ::this= [hello world]
          ::object.class.simpleName= LinkedList
        }
      }
      """

  Scenario: Empty List
    When "collector" collect with the following properties:
      """
      : []
      """
    Then the result should be:
      """
      : {
        ::properties= {}
        ::build: {
          ::this= []
          ::object.class.simpleName= LinkedList
        }
      }
      """
