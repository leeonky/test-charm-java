Feature: string

  Scenario: encode to bytes
    * string "你好" should:
    """
    encode: {
#      utf8: [0xE4, 0xBD, 0xA0, 0xE5, 0xA5, 0xBD]
      utf8: [-28y, -67, -96, -27, -91, -67]
    }
    """
    * string "你好" should:
    """
    utf8: [-28y, -67, -96, -27, -91, -67]
    """

  Scenario: base64
    * string "AQID" should:
    """
    : {
      base64: [1, 2, 3]
      base64.base64= AQID
    }
    """

  Scenario: utf8 and gbk
    * string "你好" should:
    """
    utf8.decode: {
      utf8: 你好
    }
    """
    * string "你好" should:
    """
    : {
      utf8.utf8= 你好
      gbk.gbk= 你好
    }
    """

  Scenario: ascii and iso8859-1
    * string "hello" should:
    """
    ascii.ascii= 'hello' and iso8859_1.iso8859_1= 'hello'
    """
