Feature: return

  Scenario: register return hook
    Given the following java class:
    """
    public class DataValue {
      public int i = 100;
      public int j = 0;
    }
    """
    Given the following java class:
    """
    public class InputValue {
      public DataValue data = new DataValue();
    }
    """
    And register DAL:
    """
      dal.getRuntimeContextBuilder()
      .registerReturnHook(d -> d.resolved().cast(DataValue.class).ifPresent(v -> v.j=20));
    """
    Then the following verification for the instance of java class "InputValue" should pass:
    """
    : {
      data: { i= 100 }
      data.j= 20
    }
    """

  Scenario: return hook via interface
    Given the following java class:
    """
    public class DataValue implements ReturnHook {
      public int i = 100;
      public int j = 0;
      public void onReturn() {
        j = 20;
      }
    }
    """
    Given the following java class:
    """
    public class InputValue {
      public DataValue data = new DataValue();
    }
    """
    Then the following verification for the instance of java class "InputValue" should pass:
    """
    : {
      data: { i= 100 }
      data.j= 20
    }
    """
