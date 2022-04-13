Feature: dir/file with java File

  Scenario: empty folder
    Given root folder "/tmp/test/dir"
    Then java.io.File "/tmp/test/dir" should:
    """
    = []
    """
    And java.io.File "/tmp/test/dir" should:
    """
    = {}
    """

  Scenario: single file
    Given a file "/tmp/test/file.txt"
    """
    hello
    """
    Then java.io.File "/tmp/test/file.txt" should:
    """
    : {
      name: file.txt
      string: hello
    }
    """
    Then java.io.File "/tmp/test/file.txt" should:
    """
    : 'file.txt'
    """
