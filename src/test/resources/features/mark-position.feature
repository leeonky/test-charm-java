Feature: mark-position

  Rule: char position

    Scenario: empty string with out mark
      Given the string content:
      """
      """
      Then got marked string content:
      """
      """

    Scenario: no mark
      Given the string content:
      """
      hello
      """
      Then got marked string content:
      """
      hello
      """

    Scenario: mark first char position on single line
      Given the string content:
      """
      hello
      """
      When mark an char position on 0
      Then got marked string content:
      """
      hello
      ^
      """

    Scenario: mark on new line
      Given the string content:
      """


      """
      When mark an char position on 0
      Then got marked string content:
      """

      ^

      """

    Scenario: mark tow char positions on single line
      Given the string content:
      """
      hello
      """
      When mark an char position on 0
      And mark an char position on 5
      Then got marked string content:
      """
      hello
      ^    ^
      """

    Scenario: mark three char positions on single line
      Given the string content:
      """
      hello
      """
      When mark an char position on 0
      When mark an char position on 2
      And mark an char position on 5
      Then got marked string content:
      """
      hello
      ^ ^  ^
      """

    Scenario: mark four char positions on single line
      Given the string content:
      """
      helloWorld
      """
      When mark an char position on 0
      When mark an char position on 2
      And mark an char position on 5
      And mark an char position on 10
      Then got marked string content:
      """
      helloWorld
      ^ ^  ^    ^
      """

    Scenario: mark three char positions without sequence
      Given the string content:
      """
      hello
      """
      When mark an char position on 0
      And mark an char position on 5
      When mark an char position on 2
      Then got marked string content:
      """
      hello
      ^ ^  ^
      """

    Scenario: mark on first line of double lines
      Given the string content:
      """
      hello
      world
      """
      When mark an char position on 0
      When mark an char position on 5
      Then got marked string content:
      """
      hello
      ^    ^
      world
      """

    Scenario: mark on second line of double lines
      Given the string content:
      """
      hello
      world
      """
      When mark an char position on 6
      Then got marked string content:
      """
      hello
      world
      ^
      """

    Scenario: mark char position on multi lines
      Given the string content:
      """
      hello
      world
      line3
      last.
      """
      When mark an char position on 0
      When mark an char position on 3
      When mark an char position on 5
      When mark an char position on 6
      When mark an char position on 9
      When mark an char position on 11
      When mark an char position on 12
      When mark an char position on 17
      When mark an char position on 18
      When mark an char position on 21
      When mark an char position on 23
      Then got marked string content:
      """
      hello
      ^  ^ ^
      world
      ^  ^ ^
      line3
      ^    ^
      last.
      ^  ^ ^
      """

  Rule: whole line

    Scenario: mark whole empty line
      Given the string content:
      """
      """
      When mark line position on 0
      Then got marked string content:
      """

      ^
      """

    Scenario: mark whole line (only new line)
      Given the string content:
      """


      """
      When mark line position on 0
      Then got marked string content:
      """

      ^

      """

    Scenario Outline: mark whole single line with only one letter
      Given the string content:
      """
      a
      """
      When mark line position on <position>
      Then got marked string content:
      """
      a
      ^^
      """
      Examples:
        | position |
        | 0        |
        | 1        |

    Scenario Outline: mark whole single line with many letters
      Given the string content:
      """
      abc
      """
      When mark line position on <position>
      Then got marked string content:
      """
      abc
      ^^^^
      """
      Examples:
        | position |
        | 0        |
        | 1        |
        | 3        |

    Scenario Outline: mark first single line
      Given the string content:
      """
      abc
      efg
      """
      When mark line position on <position>
      Then got marked string content:
      """
      abc
      ^^^^
      efg
      """
      Examples:
        | position |
        | 0        |
        | 1        |
        | 3        |

    Scenario Outline: mark second single line
      Given the string content:
      """
      abc
      efg
      hij
      """
      When mark line position on <position>
      Then got marked string content:
      """
      abc
      efg
      ^^^^
      hij
      """
      Examples:
        | position |
        | 4        |
        | 5        |
        | 6        |

    Scenario: mark multi lines
      Given the string content:
      """
      a
      bc
      def
      ghij
      """
      When mark line position on 1
      When mark line position on 2
      When mark line position on 5
      Then got marked string content:
      """
      a
      ^^
      bc
      ^^^
      def
      ^^^^
      ghij
      """

  Rule: whole column

    Scenario: mark column on empty line
      Given the string content:
      """
      """
      When mark column on 0
      Then got marked string content:
      """

      ^
      """

    Scenario: mark column on new line
      Given the string content:
      """


      """
      When mark column on 0
      Then got marked string content:
      """

      ^

      """

    Scenario: mark first column on single line
      Given the string content:
      """
      hello
      """
      When mark column on 0
      When mark column on 3
      Then got marked string content:
      """
      hello
      ^  ^
      """

    Scenario Outline:  mix char and line mark
      Given the string content:
      """
      hello
      world
      !
      """
      When mark an char position on 0
      When mark an char position on 4
      When mark an char position on 6
      When mark an char position on 10
      When mark line position on <position>
      Then got marked string content:
      """
      hello
      ^   ^
      world
      ^   ^
      ^^^^^^
      !
      """
      Examples:
        | position |
        | 6        |
        | 7        |
        | 8        |
        | 9        |
        | 10       |

    Scenario: mix char line column
      Given the string content:
      """
      hello
      world
      !
      """
      When mark an char position on 0
      When mark an char position on 4
      When mark an char position on 6
      When mark an char position on 10
      When mark line position on 0
      When mark column on 2
      When mark column on 8
      Then got marked string content:
      """
      hello
      ^   ^
        ^
      ^^^^^^
      world
      ^   ^
        ^
      !
      """

  Rule: offset

    Scenario: offset < first position
      Given the string content:
      """
      abc
      """
      When mark an char position on 1
      Then got marked string content with prefix length 1:
      """
      bc
      ^
      """

    Scenario: offset > first position
      Given the string content:
      """
      abc
      """
      When mark an char position on 1
      Then got marked string content with prefix length 2:
      """
      c
      """

    Scenario: offset < first position when mark whole line
      Given the string content:
      """
      abc
      """
      When mark line position on 1
      Then got marked string content with prefix length 1:
      """
      bc
      ^^^
      """

    Scenario: should not mark anything when offset > first position
      Given the string content:
      """
      abc
      """
      When mark line position on 0
      Then got marked string content with prefix length 1:
      """
      bc
      """

    Scenario: set offset
      Given the string content:
      """
      hello
      world
      !
      """
      When mark an char position on 0
      When mark an char position on 4
      When mark an char position on 6
      When mark an char position on 10
      When mark line position on 10
      When mark column on 2
      When mark column on 8
      Then got marked string content with prefix length 3:
      """
      lo
       ^
      world
      ^   ^
        ^
      ^^^^^^
      !
      """

  Rule: cjk-r

    Scenario: cjk-r after position
      Given the string content:
      """
      0你
      """
      When mark an char position on 0
      Then got marked string content:
      """
      0你
      ^
      """

    Scenario: cjk-r is position
      Given the string content:
      """
      0你
      """
      When mark an char position on 1
      Then got marked string content:
      """
      0你
       ^
      """

    Scenario: one cjk-r before position
      Given the string content:
      """
      0你a
      """
      When mark an char position on 2
      Then got marked string content:
      """
      0你a
         ^
      """

    Scenario: two cjk-r before position
      Given the string content:
      """
      0你好a
      """
      When mark an char position on 3
      Then got marked string content:
      """
      0你好a
           ^
      """

    Scenario: two positions in the same line and first is not cjk-r
      Given the string content:
      """
      abc你好
      """
      When mark an char position on 2
      When mark an char position on 3
      Then got marked string content:
      """
      abc你好
        ^^
      """

    Scenario: two positions in the same line and first cjk-r
      Given the string content:
      """
      bc你拉好
      """
      When mark an char position on 2
      When mark an char position on 4
      Then got marked string content:
      """
      bc你拉好
        ^   ^
      """

    Scenario: mark line which has cjk-r
      Given the string content:
      """
      bc你拉好
      """
      When mark line position on 1
      Then got marked string content:
      """
      bc你拉好
      ^^^^^^^^^
      """

    Scenario: cjk-r string
      Given the string content:
      """
      0你1
      好2拉
      0你1
      好2拉
      !
      """
      When mark an char position on 0
      And mark an char position on 2
      And mark an char position on 5
      And mark an char position on 9
      And mark an char position on 12
      And mark an char position on 14
      Then got marked string content:
      """
      0你1
      ^  ^
      好2拉
        ^
      0你1
       ^
      好2拉
      ^  ^
      !
      """
