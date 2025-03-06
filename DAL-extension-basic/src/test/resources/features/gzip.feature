Feature: gzip

  Scenario: gzip byte[]
    Given the following java class:
    """
    public class Data {
      public byte[] data = new byte[] {100, 101};
    }
    """
    Then the following should pass:
    """
    data.gzip= ``` HEX
               1F 8B 08 00  00 00 00 00  00 00 4B 49  05 00 8B 29
               90 7D 02 00  00 00
               ```
    """

  Scenario: gzip string
    Given the following java class:
    """
    public class Data {
      public String data = "hello";
    }
    """
    Then the following should pass:
    """
    data.gzip= ``` HEX
               1F 8B 08 00  00 00 00 00  00 00 CB 48  CD C9 C9 07
               00 86 A6 10  36 05 00 00  00
               ```
    """

  Scenario: gzip doc type of byte[]
    Given the following java class:
    """
    public class Data {
      public byte[] data = new byte[] {100, 101};
    }
    """
    Then the following should pass:
    """
    data.gzip= ``` HEX GZIP
               64 65
               ```
    """

  Scenario: gzip doc type of string
    Given the following java class:
    """
    public class Data {
      public String data = "hello";
    }
    """
    Then the following should pass:
    """
    data.gzip= ``` GZIP
               hello
               ```
    """

  Scenario: support ungzip
    Then the following should pass:
    """
    'hello'.gzip.ungzip.string= hello
    """
