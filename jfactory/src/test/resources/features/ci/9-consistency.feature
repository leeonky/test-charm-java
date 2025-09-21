Feature: consistency

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

  Rule: merge

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

  Rule: resolution order

    Scenario: not merge when compose is null and not null and decomposer is not null and null
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                Function<String,String> toUpper = s -> s.toUpperCase();
                Function<String,String> toLower = s -> s.toLowerCase();

                consistent(String.class)
                .<String>property("str1")
                    .write(toLower)
                .<String>property("str2")
                    .read(String::toLowerCase)
                    .write(String::toUpperCase);

                consistent(String.class)
                .<String>property("str1")
                    .read(toUpper)
                .<String>property("str3")
                    .read(String::toLowerCase)
                    .write(String::toUpperCase);
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: /^str2.*/
          str3: HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: HELLO
          str3: HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1: /^str2.*/
          str2: /^str2.*/
          str3: HELLO
        }
        """

    Scenario: not merge when compose is not null and null and decomposer is null and not null
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                Function<String,String> toUpper = s -> s.toUpperCase();
                Function<String,String> toLower = s -> s.toLowerCase();

                consistent(String.class)
                .<String>property("str1")
                    .write(toLower)
                .<String>property("str2")
                    .read(String::toLowerCase)
                    .write(String::toUpperCase);

                consistent(String.class)
                .<String>property("str1")
                    .read(toUpper)
                .<String>property("str3")
                    .read(String::toLowerCase)
                    .write(String::toUpperCase);
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: /^str2.*/
          str3: HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: HELLO
          str3: HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1: /^str2.*/
          str2: /^str2.*/
          str3: HELLO
        }
        """

    Scenario: not merge when compose is not null and null and decomposer is null and not null no matter data type
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2;
          public int i;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                .<String>property("str1")
                    .write(s->s)
                .direct("str2");

                consistent(Integer.class)
                .<String>property("str1")
                    .read(Integer::parseInt)
                .direct("i");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "100").create();
        """
      Then the result should:
        """
        : {
          str1: '100'
          str2: /^str2.*/
          i: 100
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "200").create();
        """
      Then the result should:
        """
        : {
          str1: '200'
          str2: '200'
          i: 200
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("i", 300).create();
        """
      Then the result should:
        """
        : {
          str1: /^str2.*/
          str2: /^str2.*/
          i: 300
        }
        """

    Scenario: not merge when compose is null and not null and decomposer is not null and null no matter data type
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2;
          public int i;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(Integer.class)
                .<String>property("str1")
                    .write(Object::toString)
                .direct("i");

                consistent(String.class)
                .<String>property("str1")
                    .read(s->s)
                .direct("str2");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "100").create();
        """
      Then the result should:
        """
        : {
          str1: '100'
          str2: '100'
          i: 1
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "200").create();
        """
      Then the result should:
        """
        : {
          str1: '2'
          str2: '200'
          i: 2
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("i", 300).create();
        """
      Then the result should:
        """
        : {
          str1: '300'
          str2: '300'
          i: 300
        }
        """

    Scenario: not merge part of properties matched when compose is null and not null and decomposer is not null and null
      Given the following bean class:
        """
        public class Order {
          public int quantity;
          public BigDecimal unitPrice, total, unitDiscount, discountTotal;
        }
        """
      And the following spec class:
        """
        public class OrderSpec extends Spec<Order> {
            public void main() {
              consistent(BigDecimal.class)
                .<Integer, BigDecimal>properties("quantity", "unitPrice")
                  .read((qty, price) -> price.multiply(BigDecimal.valueOf(qty)))
                .direct("total");

              consistent(BigDecimal.class)
                .<Integer, BigDecimal>properties("quantity", "unitDiscount")
                  .write(amount -> new Object[]{1, amount})
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
          quantity: 1
          unitPrice: 1
          total: 100
          unitDiscount: 1
          discountTotal: 1
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("discountTotal", 50).create();
        """
      Then the result should:
        """
        : {
          quantity: 1
          unitPrice: 2
          total:  4
          unitDiscount: 50
          discountTotal: 50
        }
        """


# TODO 2/3/4 properties
# TODO merge multi properties
# TODO multi properties select first item
#  TODO  should merge when only composen and only decomposer in same type
#  TODO  should raise error when only composen and only decomposer in same type
#  TOOD resolve order
#  TODO merge item in list