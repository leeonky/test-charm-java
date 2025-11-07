Feature: entries

  Scenario: support to get all keys as a list
    Given the following java class:
    """
    public class Bean {
      public String key1="hello", key2="world";
    }
    """
    Then the following verification for the instance of java class "Bean" should pass:
    """
    ::entries= [[key1, hello] [key2, world]]
    """
