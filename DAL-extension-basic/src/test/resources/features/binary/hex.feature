Feature: hex

  Scenario: bytes equal to hex
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

  Scenario: input-stream equal to hex
    Given root folder "/tmp/work/test/dir"
    Given a file "/tmp/work/test/dir/file1.txt"
    """
    你好
    """
    Then the following should pass:
    """
    "file:/tmp/work/test/dir/file1.txt".url.openStream= ``` HEX
                                                        E4 BD A0 E5 A5 BD
                                                        ```
    """

  Scenario: raise error when input-stream not equal
    Given root folder "/tmp/work/test/dir"
    Given a file "/tmp/work/test/dir/file1.txt"
    """
    你好
    """
    When evaluate by:
    """
    "file:/tmp/work/test/dir/file1.txt".url.openStream= ``` HEX
                                                        E4 BD A0 E5 A5 BF
                                                        ```
    """
    Then failed with the message:
    """

    "file:/tmp/work/test/dir/file1.txt".url.openStream= ``` HEX
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

  Scenario: convert to bytes and equal to hex
    Given a class object with string "你好" and can be converted to bytes
    Then the following should pass:
    """
    : ``` HEX
      E4 BD A0 E5 A5 BD
      ```
    """

  Scenario: convert to input-stream and equal to hex
    Given a class object with string "你好" and can be converted to input-stream
    Then the following should pass:
    """
    : ``` HEX
      E4 BD A0 E5 A5 BD
      ```
    """
