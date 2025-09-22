Feature: multi properties consistency

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: multiple properties consistency

    Scenario: two properties consistency
      Given the following bean class:
        """
        public class Person {
          public String fullName, firstName, lastName, familyName, givenName;
        }
        """
      And the following spec class:
        """
        public class APerson extends Spec<Person> {
          public void main() {
            consistent(String.class)
              .direct("fullName")
              .properties("firstName", "lastName")
                .read((first,last) -> first+" "+last).write(s -> s.split(" "))
              .properties("familyName", "givenName")
                .read(names -> names[0]+" "+names[1]).write(s->s.split(" ")[0], s->s.split(" ")[1]);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(APerson.class).property("fullName", "James Anderson").create();
        """
      Then the result should:
        """
        : {
          fullName: 'James Anderson'
          firstName: James
          lastName: Anderson
          familyName: James
          givenName: Anderson
        }
        """
      When build:
        """
        jFactory.clear().spec(APerson.class).property("firstName", "James").property("lastName", "Anderson").create();
        """
      Then the result should:
        """
        : {
          fullName: 'James Anderson'
          firstName: James
          lastName: Anderson
          familyName: James
          givenName: Anderson
        }
        """

  Rule: need merge

    Scenario: merge multi properties
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                BiFunction<String,String,String> join = (s1, s2)-> s1+"#"+s2;
                Function<String,Object[]> divide = s->s.split("#");
                consistent(String.class)
                  .<String, String>properties("str1", "str2")
                    .read(join)
                    .write(divide)
                  .direct("str3");

                consistent(String.class)
                  .<String, String>properties("str1", "str2")
                    .read(join)
                    .write(divide)
                  .direct("str4");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str4", "hello#world").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: world
          str3: hello#world
          str4: hello#world
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "good#bye").create();
        """
      Then the result should:
        """
        : {
          str1: good
          str2: bye
          str3: good#bye
          str4: good#bye
        }
        """

  Rule: part of properties matched no merge

    Background:
      Given the following bean class:
        """
        public class Order {
          public int quantity;
          public BigDecimal unitPrice, total, unitDiscount, discountTotal;
        }
        """

    Scenario: compose: null not_null and decomposer: not_null null
      And the following spec class:
        """
        public class OrderSpec extends Spec<Order> {
            public void main() {
              consistent(BigDecimal.class)
                .<Integer, BigDecimal>properties("quantity", "unitPrice")
                  .write(total -> new Object[]{2, total.divide(BigDecimal.valueOf(2))})
                .direct("total");

              consistent(BigDecimal.class)
                .<Integer, BigDecimal>properties("quantity", "unitDiscount")
                  .read((qty, discount) -> discount.multiply(BigDecimal.valueOf(qty)))
                .direct("discountTotal");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("total", 100).create();
        """
      Then the result should:
        """
        : {
          quantity: 2
          unitPrice: 50
          total: 100
          unitDiscount: 1
          discountTotal: 2
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("quantity", 50).create();
        """
      Then the result should:
        """
        : {
          quantity: 50
          unitPrice: 1
          total: 2
          unitDiscount: 2
          discountTotal: 100
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("unitPrice", 50).create();
        """
      Then the result should:
        """
        : {
          quantity: 2
          unitPrice: 50
          total: 3
          unitDiscount: 3
          discountTotal: 6
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("discountTotal", 50).create();
        """
      Then the result should:
        """
        : {
          quantity: 2
          unitPrice: 2
          total:  4
          unitDiscount: 4
          discountTotal: 50
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("unitDiscount", 10).create();
        """
      Then the result should:
        """
        : {
          quantity: 2
          unitPrice: 2.5
          total:  5
          unitDiscount: 10
          discountTotal: 20
        }
        """

# TODO 2/3/4 properties
# TODO merge multi properties
# TODO multi properties select first item
#  TODO  should merge when only composen and only decomposer in same type
#  TODO  should raise error when only composen and only decomposer in same type
#  TOOD resolve order
#  TODO merge item in list
