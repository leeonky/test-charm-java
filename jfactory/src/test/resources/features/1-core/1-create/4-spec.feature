Feature: Spec

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Rule: Type Spec

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
        }
        """

    Scenario: Base Spec for a Type - Define Default Base Rules for a Type and Create an Object with the Base Spec
      When register as follows:
        """
        jFactory.factory(Bean.class).spec(ins -> ins.spec()
          .property("stringValue").value("hello")
          .property("intValue").value(100)
        );
        """
      And evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        : {
          stringValue= hello
          intValue= 100
        }
        """

    Scenario: Trait - Define Naming Spec as a Trait
      When register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("hello", ins -> ins.spec().property("stringValue").value("hello"));
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).traits("hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Regex Trait Registration — Define a Regex Trait, then Match and Bind Captured Params on Create
      When register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("string_(.*)", ins -> ins.spec().property("stringValue").value(ins.traitParams()[0]));
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).traits("string_hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

  Rule: Use Spec Class

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
        }
        """

    Scenario: Spec Class - Define a Spec as a Class and Create an Object by Spec
      Given the following class definition:
        """
        import com.github.leeonky.jfactory.Spec;
        public class ABean extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Spec Name - Use a Spec by Name
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("ABean").property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Custom Spec Name - Define a New Spec Name in the Spec Class
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {
          protected String getName() { return "OneBean"; }
        }
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("OneBean").property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Trait in Spec Class - Define Traits in a Spec Class and Create an Object by Trait
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {

          @Trait
          public void helloTrait() {
            property("stringValue").value("hello");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).traits("helloTrait").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Trait Name via Annotation — Use @Trait("name") instead of method name
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {

          @Trait("helloTrait") //Define Trait with a Name instead of method
          public void t1() {
            property("stringValue").value("hello");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).traits("helloTrait").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Regex Trait in Spec Class - Match the Trait Name and Bind Captured Groups to Trait Method Parameters
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {

          @Trait("value_(.*)_(.*)")
          public void stringTrait(String s, int i) {
            property("stringValue").value(s);
            property("intValue").value(i);
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).traits("value_hello_100").create();
        """
      Then the result should be:
        """
        : {
          stringValue= hello
          intValue= 100
        }
        """

  Rule: Global Spec Class

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
        }
        """

    Scenario: Global Spec Class - Define a Global Spec Class to Apply to All Types
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
            property("stringValue").value("globalHello");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        stringValue= globalHello
        """

    Scenario: Remove Global Spec Class - Remove a Global Spec Class from the Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
            property("stringValue").value("globalHello");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        jFactory.removeGlobalSpec(Bean.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        stringValue= /^stringValue.*/
        """

  Rule: Spec Overlay

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3;
        }
        """

    Scenario: Spec Class and Type Spec - Type Spec as the base Spec of Spec Class
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value1").value("spec_class");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(ins -> ins.spec()
          .property("value2").value("base_type")
        );
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        : {
          value1= spec_class
          value2= base_type
        }
        """

    Scenario: Spec Class and Global Spec Class - Global Spec Class as the base Spec of Spec Class
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
              property("value1").value("global_spec");
          }
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
              property("value2").value("spec_class");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        : {
          value1= global_spec
          value2= spec_class
        }
        """

    Scenario: Type Spec and Global Spec Class - Type Spec as the base Spec of Global Spec Class
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
              property("value1").value("global_spec");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        jFactory.factory(Bean.class).spec(ins -> ins.spec()
          .property("value2").value("base_type")
        );
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        : {
          value1= global_spec
          value2= base_type
        }
        """

    Scenario: Spec Class / Global Spec Class / Type Spec - Type Spec as the base Spec of Global Spec Class and Global Spec Class as the base of the Spec Class
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
              property("value1").value("global_spec");
          }
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
              property("value2").value("spec_class");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        jFactory.factory(Bean.class).spec(ins -> ins.spec()
          .property("value3").value("base_type")
        );
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        : {
          value1= global_spec
          value2= spec_class
          value3= base_type
        }
        """

#    Scenario: Base Spec Trait - Define Traits in Base Spec and Use it in Creation
#      Given the following spec definition:
#        """
#        @Global
#        public class GlobalBeanSpec extends Spec<Bean> {
#          @Trait
#          public void global_spec() {
#              property("value2").value("global");
#          }
#        }
#        """
#      Given the following spec definition:
#        """
#        public class BeanSpec extends Spec<Bean> {
#        }
#        """
#      And register as follows:
#        """
#        jFactory.register(GlobalBeanSpec.class);
#        jFactory.factory(Bean.class).spec("base_type",
#          ins -> ins.spec().property("value1").value("base"));
#        """
#      When evaluating the following code:
#        """
#        jFactory.spec(BeanSpec.class).traits("base_type", "global_spec").create();
#        """
#      Then the result should be:
#        """
#        : {
#          value1= base
#          value2= global
#        }
#        """


#TODO Spec class and Type spec (merge, trait override)
#TODO regex trait(lambda, spec class)
#TODO global spec class as base type
#TODO global spec and base type (merge, trait override)
#TODO Spec class / global Spec class / type spec (merge, trait override)
#TODO error handler missing name missing pattern
