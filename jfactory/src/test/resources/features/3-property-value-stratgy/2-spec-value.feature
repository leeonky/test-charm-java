Feature: Spec Value

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """

  Scenario: Spec Value - Specify Spec Value in Spec
    When register as follows:
      """
      jFactory.factory(Bean.class).spec(spec -> spec.property("str").value("hello"));
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      str= hello
      """

  Scenario: Lambda Spec Value - Use a Lambda for Spec Value
    When register as follows:
      """
      jFactory.factory(Bean.class).spec(spec -> spec.property("str").value(() -> "from_lambda"));
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      str= from_lambda
      """

  Scenario: Null Spec Value — Treat Null as a Literal Null Value, not as a Null Supplier<Object>
    When register as follows:
      """
      jFactory.factory(Bean.class).spec(spec -> spec.property("str").value(null));
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      str= null
      """

  Scenario: Sub Property Spec Value - Not Allowed to Define Sub Property Spec Value
    Given the following bean definition:
      """
      public class BeanHolder {
        public Bean bean;
      }
      """
    And the following spec definition:
      """
      public class BeanHolderSpec extends Spec<BeanHolder> {
        @Override
        public void main() {
          property("bean.str").value("hello");
        }
      }
      """
    When evaluating the following code:
      """
      jFactory.createAs(BeanHolderSpec.class);
      """
    Then the result should be:
      """
      ::throw.message: 'Property chain `bean.str` is not supported in the current operation'
      """

  Scenario: Self Reference - Support Reference Self in property value Lambda
    Given the following bean definition:
      """
      public class Bean {
        public Bean bean;
      }
      """
    And register as follows:
      """
      jFactory.factory(Bean.class).spec(spec -> spec
        .property("bean").value(spec.instance().reference()));
      """
    When evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      bean= ::root
      """

  Scenario: Value Override - Prioritize Spec Value Over Default Value
    And register as follows:
      """
      jFactory.factory(Bean.class).spec(spec -> spec
        .property("str").value("spec-value")
        .property("str").defaultValue("default-value")
      );
      """
    When evaluating the following code:
      """
      jFactory.type(Bean.class).create()
      """
    Then the result should be:
      """
      str= spec-value
      """
