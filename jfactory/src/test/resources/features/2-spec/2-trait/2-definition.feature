Feature: Trait Spec Definition in Different Object Creation Forms

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

    Scenario: Type-Factory with Global Spec-Class - Define Trait in Type Factory, Global Spec Class and Global Spec Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          @Trait("global-spec-class")
          public void globalSpecClass() {
            property("value2").value("global-spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("type-factory", spec -> spec.property("value1").value("type-factory"));
        jFactory.specFactory(GlobalBeanSpec.class)
          .spec("global-spec-factory", spec -> spec.property("value3").value("global-spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("type-factory", "global-spec-class", "global-spec-factory").create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory
          value2= global-spec-class
          value3= global-spec-factory
        }
        """

    Scenario: Ineffective Trait During Type Creation - Trait Defined in Non-Global Spec Class Have No Effect in Type Creation
      Given the following spec definition:
        """
        public class NonGlobalBeanSpec extends Spec<Bean> {
          @Trait("trait")
          public void trait() {
            property("value1").value("any-value");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(NonGlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `trait` not exist"
        """

    Scenario: Ineffective Trait During Type Creation - Trait Defined in Non-Global Spec Factory Have No Effect in Type Creation
      Given the following spec definition:
        """
        public class NonGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(NonGlobalBeanSpec.class)
        .spec("trait", spec -> spec.property("value1").value("any-value"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `trait` not exist"
        """

  Rule: Spec Creation

    Scenario: Type-Factory and Spec-Class with Global Spec Class - Define Trait in Type Factory, Spec Class, Spec Factory, Global Spec Class and Global Spec Factory
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3, value4, value5;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("type-factory", spec -> spec.property("value5").value("type-factory"));
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait("spec-class")
          public void specClass() {
            property("value1").value("spec-class");
          }
        }
        """
      And the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          @Trait("global-spec-class")
          public void globalSpecClass() {
            property("value3").value("global-spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class)
          .spec("spec-factory", spec -> spec.property("value2").value("spec-factory"));
        jFactory.specFactory(GlobalBeanSpec.class)
          .spec("global-spec-factory", spec -> spec.property("value4").value("global-spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("spec-class", "spec-factory", "global-spec-class", "global-spec-factory", "type-factory").create();
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
