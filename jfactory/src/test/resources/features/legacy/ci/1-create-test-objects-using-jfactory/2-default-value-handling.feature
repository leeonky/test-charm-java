Feature: Default Value Handling

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """


  @import(com.github.leeonky.util.*)
  Scenario: Custom Default Value Strategy - Define a Custom Default Value Factory by Type
    Given the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """
    When register as follows:
      """
      jFactory.registerDefaultValueFactory(String.class, new DefaultValueFactory<String>() {
        @Override
          public <T> String create(BeanClass<T> beanType, ObjectProperty<T> objectProperty) {
            return  "hello-" + beanType.getSimpleName();
          }
        });
      """
    And evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      str= hello-Bean
      """

  Scenario: Custom Property Default Value - Define a Class-Specific Default Value for a Property
    Given the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """
    When register as follows:
      """
      jFactory.factory(Bean.class).spec(ins -> ins.spec().property("str").defaultValue("hello_" + ins.getSequence()));
      """
    And evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      str= hello_1
      """

  Scenario: Use Lambda in Default Value - Define a Class-Specific Lambda Default Value for a Property
    Given the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """
    When register as follows:
      """
      jFactory.factory(Bean.class).spec(ins -> ins.spec().property("str").defaultValue(() -> "from_lambda"));
      """
    And evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      str= from_lambda
      """

  Scenario: Default Value Skipping - Support Ignoring Default Value Generation
    Given the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """
    When register as follows:
      """
      jFactory.ignoreDefaultValue(propertyWriter -> "str".equals(propertyWriter.getName()));
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      str= null
      """

  Scenario: Ignore Property - Ignoring Default Value Generation in Spec
    Given the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """
    When register as follows:
      """
      jFactory.factory(Bean.class).spec(ins -> ins.spec().ignore("str"));
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      str= null
      """

  Scenario: Value Override - Prioritize Input Property Values Over Any Defaults
    Given the following bean definition:
      """
      public class Bean {
        public String str1, str2, str3;
      }
      """
    And register as follows:
      """
      jFactory.factory(Bean.class).spec(ins -> ins.spec()
        .property("str1").ignore()
        .property("str2").defaultValue("any")
      );
      """
    When evaluating the following code:
      """
      jFactory.type(Bean.class)
        .property("str1", "hello")
        .property("str2", "world")
        .property("str3", "!")
        .create()
      """
    Then the result should be:
      """
      : {str1= hello, str2= world, str3= '!'}
      """
