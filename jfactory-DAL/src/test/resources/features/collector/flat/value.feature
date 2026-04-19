Feature: Single Value

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following declarations:
      """
      Collector collector = jFactory.collector();
      """

  Scenario Outline: Single value
    When "collector" collect and build with the following properties:
      """
      : <given>
      """
    Then the result should be:
      """
      = <expect>
      """
    Examples:
      | given | expect |
      | 100   | 100    |
      | hello | hello  |
