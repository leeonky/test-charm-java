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
      When build:
        """
        jFactory.clear().spec(APerson.class).property("firstName", "James").property("givenName", "Anderson").create();
        """
      Then the result should:
        """
        : {
          fullName: /^James lastName.*/
          firstName: James
          lastName: /^lastName.*/
          familyName: James
          givenName: Anderson
        }
        """

    Scenario: one of properties has higher priority then consistency item has higher priority
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
        jFactory.clear().spec(APerson.class).property("givenName", "Tom").create();
        """
      Then the result should:
        """
        : {
          fullName: /^familyName.* Tom/
          firstName: /^familyName.*/
          lastName: Tom
          familyName: /^familyName.*/
          givenName: Tom
        }
        """

    Scenario: three properties consistency
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4, str5, str6;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                .<String, String, String>properties("str1", "str2", "str3")
                    .read((s1,s2,s3)->s1+s2+s3)
                    .write(s->s.substring(0,1), s->s.substring(1,2), s->s.substring(2,3))
                .<String, String, String>properties("str4", "str5", "str6")
                    .read((s1,s2,s3)->s1+s2+s3)
                    .write(s->s.substring(0,1), s->s.substring(1,2), s->s.substring(2,3));
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str2.*/
          str3: /^str3.*/
          str4: s
          str5: t
          str6: r
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "x").create();
        """
      Then the result should:
        """
        : {
          str1: x
          str2: /^str2.*/
          str3: /^str3.*/
          str4: x
          str5: s
          str6: t
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

  Rule: part of properties matched for merge

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4, str5, str6;
        }
        """

    Scenario: no merge compose: null not_null and decomposer: not_null null
      And the following spec class:
        """
        public class BeanSpec extends Spec<Bean> {
            public void main() {
              consistent(String.class)
                .<String, String>properties("str1", "str2")
                  .write(s -> new Object[]{s, s})
                .direct("str4");

              consistent(String.class)
                .<String, String>properties("str1", "str3")
                  .read((s1, s2) -> s1+"#"+s2)
                .direct("str5");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= /^str4.*/, str3= /^str3.*/, str4= /^str4.*/, str5= /^hello#str3.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        str1= /^str4.*/, str2= hello, str3= /^str3.*/, str4= /^str4.*/, str5= /^str4.*#str3.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str4", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= hello, str3= /^str3.*/, str4= hello, str5= /^hello#str3.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        str1= /^str4.*/, str2= /^str4.*/, str3= hello, str4= /^str4.*/, str5= /^str4.*#hello/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str5", "hello").create();
        """
      Then the result should:
        """
        str1= /^str4.*/, str2= /^str4.*/, str3= /^str3.*/, str4= /^str4.*/, str5= hello
        """

    Scenario Outline: property overlap composer and decomposer conflict - raise error
      And the following spec class:
        """
        public class BeanSpec extends Spec<Bean> {
            public void main() {
              Function<Object[], String> reader1 = s -> {
                  throw new RuntimeException("Should not be called");
              };
              Function<Object[], String> reader2 = s -> {
                  throw new RuntimeException("Should not be called");
              };
              Function<String, Object[]> writer1 = s -> {
                  throw new RuntimeException("Should not be called");
              };
              Function<String, Object[]> writer2 = s -> {
                  throw new RuntimeException("Should not be called");
              };

              consistent(String.class)
                .<String, String>properties("str1", "str2")
                  <composer1>
                  <decomposer1>
                .direct("str4");

              consistent(String.class)
                .<String, String>properties("str1", "str3")
                  <composer2>
                  <decomposer2>
                .direct("str5");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).create();
        """
      Then should raise error:
        """
        message.table: [
          ['Conflict consistency on property <str1, str2> and <str1, str3>, property overlap:'],
          ['', type composer decomposer],
          [#package#BeanSpec.main(BeanSpec.java:21) java.lang.String '<composer1Pos>' '<decomposer1Pos>'],
          [#package#BeanSpec.main(BeanSpec.java:27) java.lang.String '<composer2Pos>' '<decomposer2Pos>']
        ]
        """
      Examples:
        | composer1      | composer2      | decomposer1     | decomposer2     | composer1Pos       | composer2Pos       | decomposer1Pos     | decomposer2Pos     |
        |                |                | .write(writer1) | .write(writer2) | null               | null               | (BeanSpec.java:23) | (BeanSpec.java:29) |
        | .read(reader1) | .read(reader2) |                 |                 | (BeanSpec.java:22) | (BeanSpec.java:28) | null               | null               |
        | .read(reader1) | .read(reader2) | .write(writer1) | .write(writer2) | (BeanSpec.java:22) | (BeanSpec.java:28) | (BeanSpec.java:23) | (BeanSpec.java:29) |
        | .read(reader1) | .read(reader2) |                 | .write(writer2) | (BeanSpec.java:22) | (BeanSpec.java:28) | null               | (BeanSpec.java:29) |
        | .read(reader1) | .read(reader2) | .write(writer1) |                 | (BeanSpec.java:22) | (BeanSpec.java:28) | (BeanSpec.java:23) | null               |
        | .read(reader1) |                | .write(writer1) | .write(writer2) | (BeanSpec.java:22) | null               | (BeanSpec.java:23) | (BeanSpec.java:29) |
        |                | .read(reader2) | .write(writer1) | .write(writer2) | null               | (BeanSpec.java:28) | (BeanSpec.java:23) | (BeanSpec.java:29) |

    Scenario: property overlap, different type, compose: null not_null and decomposer: not_null null no merge
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

  Rule: resolution order

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4, str5;
        }
        """

    Scenario: should resolve property which has writer first and reader last
      And the following spec class:
        """
        public class BeanSpec extends Spec<Bean> {
            public void main() {
            consistent(String.class)
                .<String, String>properties("str1", "str2")
                .read((s1, s2) -> s1+"#"+s2)
                .direct("str4");

            consistent(String.class)
                .<String, String>properties("str1", "str2")
                .write(s -> new Object[]{s, s})
                .direct("str5");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= /^str5.*/,  str4= /^hello#str5.*/, str5= /^str5.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        str1= /^str5.*/, str2= hello,  str4= /^str5.*#hello/, str5= /^str5.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str4", "hello").create();
        """
      Then the result should:
        """
        str1= /^str5.*/, str2= /^str5.*/,  str4= hello, str5= /^str5.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str5", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= hello,  str4= hello#hello, str5= hello
        """

    Scenario: overlap property resolution order
      And the following spec class:
        """
        public class BeanSpec extends Spec<Bean> {
            public void main() {
            consistent(String.class)
                .<String, String>properties("str1", "str2")
                .read((s1, s2) -> s1+"#"+s2)
                .direct("str4");

            consistent(String.class)
                .<String, String>properties("str1", "str3")
                .write(s -> new Object[]{s, s})
                .direct("str5");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= /^str2.*/, str3=/^str5.*/, str4= /^hello#str2.*/, str5= /^str5.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        str1= /^str5.*/, str2= hello, str3=/^str5.*/, str4= /^str5.*#hello/, str5= /^str5.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        str1= /^str5.*/, str2= /^str2.*/, str3=hello,  str4= /^str5.*#str2.*/, str5= /^str5.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str4", "hello").create();
        """
      Then the result should:
        """
        str1= /^str5.*/, str2= /^str2.*/, str3=/^str5.*/,  str4= hello, str5= /^str5.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str5", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= /^str2.*/, str3= hello,  str4= /hello#str2.*/, str5= hello
        """
