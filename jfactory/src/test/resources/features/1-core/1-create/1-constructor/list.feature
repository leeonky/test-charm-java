Feature: List Constructor

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  @import(com.github.leeonky.util.TypeReference) @import(java.util.*)
  Scenario Outline: Default Collection Construct - All supported Array / Base Collection type
    When evaluating the following code:
      """
      jFactory.type(<target>).create();
      """
    Then the result should be:
      """
      class.simpleName= '<expect>'
      """
    Examples:
      | target                                   | expect        |
      | String[].class                           | String[]      |
      | new TypeReference<Iterable<String>>(){}  | ArrayList     |
      | new TypeReference<Set<String>>(){}       | LinkedHashSet |
      | new TypeReference<ArrayList<String>>(){} | ArrayList     |

  Scenario: Custom Collection Constructor by Size - Create a Collection in Custom Collection Constructor by Size
    And register as follows:
      """
      jFactory.factory(String[].class).constructor(instance -> new String[instance.collectionSize()]);
      """
    When evaluating the following code:
      """
      jFactory.type(String[].class).property("[0]", "hello").create();
      """
    Then the result should be:
      """
      : {
        class.simpleName= 'String[]',
        ::this= [hello]
      }
      """
