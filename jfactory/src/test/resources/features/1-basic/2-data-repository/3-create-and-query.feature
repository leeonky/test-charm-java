@import(java.util.*)
Feature: Create and Query

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Scenario: Create and Query - Create Object and Query It Back
    When execute as follows:
      """
      jFactory.type(Bean.class).property("str", "hello").create();
      jFactory.type(Bean.class).property("str", "world").create();
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).property("str", "hello").query()
      """
    Then the result should be:
      """
      = { class.simpleName= Bean, str= hello }
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).queryAll()
      """
    Then the result should be:
      """
      str[]= [hello world]
      """
