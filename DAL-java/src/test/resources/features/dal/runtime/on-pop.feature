Feature: on pop

  Scenario: call method before data pop
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
      .registerOnPop(d -> d.resolved().cast(DataValue.class).ifPresent(v -> v.j=20));
    """
    Then the following verification for the instance of java class "InputValue" should pass:
    """
    : {
      data: { i= 100 }
      data.j= 20
    }
    """