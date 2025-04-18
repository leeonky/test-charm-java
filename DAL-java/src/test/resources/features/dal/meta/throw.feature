Feature: meta ::throw

  Scenario: return exception
    Given the following java class:
    """
    public class Data {
      public void test() {
        throw new UserRuntimeException(new java.lang.IndexOutOfBoundsException());
      }
    }
    """
    Then the following verification for the instance of java class "Data" should pass:
    """
      test::throw.class.simpleName=  IndexOutOfBoundsException
    """

  Scenario: raise error when no throw in meta throw
    Given the following java class:
    """
    public class Data {
      public void test() {
      }
    }
    """
    When use a instance of java class "Data" to evaluate:
    """
      test::throw: {...}
    """
    Then failed with the message:
    """
    Expecting an error to be thrown, but nothing was thrown
    """
    And got the following notation:
    """
      test::throw: {...}
            ^
    """

  Scenario: should not catch throw when got java reflection exception
    Given the following java class:
    """
    public class Data {
      private void test() {
      }
    }
    """
    When use a instance of java class "Data" to evaluate:
    """
    test::throw: {...}
    """
    Then failed with the message:
    """
    Get property `test` failed, property can be:
      1. public field
      2. public getter
      3. public method
      4. Map key value
      5. customized type getter
      6. static method extension
    Method or property `test` does not exist in `#package#Data`
    """
    And got the following notation:
    """
    test::throw: {...}
    ^
    """

  Scenario: return exception from meta properties
    Given the following java class:
    """
    public class Data {
    }
    """
    And register DAL:
    """
    dal.getRuntimeContextBuilder().registerMetaProperty(Data.class, "error", meta-> {
      throw new UserRuntimeException(new java.lang.RuntimeException("error"));
    });
    """
    Then the following verification for the instance of java class "Data" should pass:
    """
    ::error::throw: {
      class.simpleName=  RuntimeException
      message= error
    }
    """

  Scenario: catch input code exception
    Given the following input code:
    """
    throw new java.lang.RuntimeException("Error");
    """
    Then the following verification of input code should pass:
    """
    ::throw.message= Error
    """

  Scenario: should raise interpreter exception first when input code is not valid
    Given the following input code:
    """
    throw new java.lang.RuntimeException("Error");
    """
    When expect run by the following code:
    """
    ::throw invalid
    """
    Then assert error with the message:
    """
    ::throw invalid
            ^

    more than one expression

    The root value was: *throw* java.lang.RuntimeException: Error
    """

  Scenario: catch exception in alias
    Given the following java class:
    """
    public class Data {
      public Container container() {
        return new Container();
      }
    }
    """
    Given the following java class:
    """
    public class Container {
      public Test test() {
        return new Test();
      }
    }
    """
    Given the following java class:
    """
    public class Test {
      public void run() {
        throw new UserRuntimeException(new java.lang.IndexOutOfBoundsException());
      }
    }
    """
    And the following schema class:
    """
    @FieldAliases({
            @FieldAlias(alias = "run", field = "container.test.run"),
    })
    public class DataSchema implements Schema {
    }
    """
    Then the following verification for the instance of java class "Data" should pass:
    """
    is DataSchema which run::throw.class.simpleName=  IndexOutOfBoundsException
    """
