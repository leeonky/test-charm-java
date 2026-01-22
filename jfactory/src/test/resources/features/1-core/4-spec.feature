Feature: Spec

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Rule: Constructor Overlay

#    Scenario: Base Spec Trait - Use Traits which was Defined in Base Spec
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
