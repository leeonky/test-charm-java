Feature: Spec Definition in Different Object Creation Forms

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following bean definition:
      """
      public class Bean {
        public String value1, value2, value3, value4, value5;
      }
      """

  Rule: Type Creation

    Scenario: Type-Factory - Define Spec in Type Factory
      Given register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("value1").value("type-factory"));
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value1= type-factory
        """

    Scenario: Type-Factory with Global Spec-Class - Define Spec in Type Factory, Global Spec Class and Global Spec Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
            property("value2").value("global-spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("value1").value("type-factory"));
        jFactory.specFactory(GlobalBeanSpec.class).spec(spec -> spec
          .property("value3").value("global-spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory
          value2= global-spec-class
          value3= global-spec-factory
        }
        """

    Scenario: Ineffective Spec During Type Creation - Spec Defined in Non-Global Spec Classes Have No Effect in Type Creation
      Given the following spec definition:
        """
        public class AnyBeanSpec extends Spec<Bean> {
          public void main() {
            property("value1").value("any-value");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(AnyBeanSpec.class).spec(spec -> spec
          .property("value2").value("any-value"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        : {
          value1= /value1.*/
          value2= /value2.*/
        }
        """

  Rule: Spec Creation

    Scenario: Type-Factory and Spec-Class - Define Spec in Type Factory, Spec Class and Spec Factory
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
              property("value2").value("spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("value1").value("type-factory"));
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
          .property("value3").value("spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory
          value2= spec-class
          value3= spec-factory
        }
        """

    Scenario: Type-Factory and Spec-Class with Global Spec Class - Define Spec in Type Factory, Spec Class, Spec Factory, Global Spec Class and Global Spec Factory
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3, value4, value5;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("value5").value("type-factory"));
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
              property("value1").value("spec-class");
          }
        }
        """
      And the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
              property("value3").value("global-spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
          .property("value2").value("spec-factory"));
        jFactory.specFactory(GlobalBeanSpec.class).spec(spec -> spec
          .property("value4").value("global-spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        : {
          value1= spec-class
          value2= spec-factory
          value3= global-spec-class
          value4= global-spec-factory
          value5= type-factory
        }
        """

    Scenario: Ineffective Spec During Spec Creation - Spec Defined in Non-Matching Spec Classes Have No Effect in Spec Creation
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {
          public void main() {
              property("value1").value("any-value");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(NonMatchingBeanSpec.class).spec(spec -> spec
          .property("value2").value("any-value"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        : {
          value1= /value1.*/
          value2= /value2.*/
        }
        """

    Scenario: Base-Spec Resolution - Base Decided at Create Time, Not at Register Time
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean>{
        }
        """
      And the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean>{
          public void main() {
            property("value1").value("global base");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        jFactory.register(GlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value1= 'global base'
        """
