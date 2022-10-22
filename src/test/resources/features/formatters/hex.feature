Feature: hex

  Scenario: equal to hex
    * string "你好" should:
    """
    bytes= ``` HEX
           E4 BD A0 E5 A5 BD
           ```
    """
