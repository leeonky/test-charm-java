Feature: list consistency

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: single and list

    Background:
      Given the following bean class:
        """
        public class Bean {
            public String status;
        }
        """
      And the following bean class:
        """
        public class BeanList {
            public List<Bean> beans;
            public String status;
        }
        """
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(String.class)
                    .list("beans").direct("status")
                    .direct("status");
        });
        """

    Scenario: single property effect all element property
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property("status", "new").create();
        """
      Then the result should:
        """
        <<beans<<0, 1>>, ::root>>.status= new
        """

    Scenario: single property effect element property which has default factory value
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[1]!", null)
                .property("status", "new").create();
        """
      Then the result should:
        """
        : {
          <<beans[1], ::this>>.status= new
          beans[0]: null
        }
        """

    Scenario: element impact element property
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[1].status", "new")
                .create();
        """
      Then the result should:
        """
        <<beans[1], ::root>>.status= new
        """

#  Rule: reader and writer in list
#
#    Background:
#      Given the following bean class:
#        """
#        public class Bean {
#            public String status;
#        }
#        """
#      And the following bean class:
#        """
#        public class BeanList {
#            public List<Bean> beans;
#            public String status;
#        }
#        """
#
#    Scenario: define reader and writer in list property
#      And operate:
#        """
#        jFactory.factory(BeanList.class).spec(ins -> {
#            Spec<BeanList> spec = ins.spec();
#            spec.consistent(String.class)
#                    .list("beans").property("status")
#                      .read(s->s)
#                      .write(s->s)
#                    .direct("status");
#        });
#        """
#      When build:
#        """
#        jFactory.type(BeanList.class)
#                .property("beans[0]!", null)
#                .property("beans[1]!", null)
#                .property("status", "new").create();
#        """
#      Then the result should:
#        """
#        <<beans<<0, 1>>, ::root>>.status= new
#        """
