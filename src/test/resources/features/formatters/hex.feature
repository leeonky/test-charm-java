Feature: hex

  Scenario: equal to hex
    * string "你好" should:
    """
    bytes= ``` HEX
           E4 BD A0 E5 A5 BD
           ```
    """

  Scenario: raise error when not equal
    When evaluate by:
    """
    '你好'.bytes= ``` HEX
                  E4 BD A0 E5 A5 BF
                  ```
    """
    Then failed with the message:
    """

    '你好'.bytes= ``` HEX
                  E4 BD A0 E5 A5 BF
                  ```
                  ^

    Expected to be equal to:                                            | Actual:
    --------------------------------------------------------------------|--------------------------------------------------------------------
    Binary size 6                                                       | Binary size 6
    00000000: E4 BD A0 E5  A5 BF                                 ...... | 00000000: E4 BD A0 E5  A5 BD                                 ......
                               ^                                        |                            ^

    The root value was: null
    """

#  TODO list byte use ==
#  TODO : use convertor
