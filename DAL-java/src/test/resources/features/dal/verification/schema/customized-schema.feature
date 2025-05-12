Feature: customized schema

  Scenario: customized schema verification
    Given the following schema class:
    """
    @Partial
    public class SchemaVerify implements Schema {
        public void verify(Data data) {
            throw new AssertionError((String)data.property("message").instance());
        }
    }
    """
    And the following json:
    """
    {
      "message": "a message"
    }
    """
    When evaluate by:
    """
    is SchemaVerify
    """
    Then failed with the message:
    """
    Expected to match schema `SchemaVerify` but was not
        a message
    """
    And got the following notation:
    """
    is SchemaVerify
       ^
    """

  Scenario: should handle other type of exception with right position
    Given the following schema class:
    """
    @Partial
    public class SchemaVerify implements Schema {
        public void verify(Data data) {
            throw new java.lang.RuntimeException("error");
        }
    }
    """
    And the following json:
    """
    {
      "message": "a message"
    }
    """
    When evaluate by:
    """
    is SchemaVerify
    """
    Then failed with the message:
    """
    Expected to match schema `SchemaVerify` but was not
        error
    """
    And got the following notation:
    """
    is SchemaVerify
       ^
    """
