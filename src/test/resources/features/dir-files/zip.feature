Feature: zip file

  Background:
    Given root folder "/tmp/test/dir"
    Given root folder "/tmp/test/tmp"

  Scenario: unzip empty zip is empty folder
    Given an empty zip file "/tmp/test/dir/empty.zip"
    Then java.io.File "/tmp/test/dir/empty.zip" should:
    """
    unzip: []
    """
    And java.io.File "/tmp/test/dir/empty.zip" should:
    """
    unzip= {}
    """

  Scenario: unzip files
    Given a file "/tmp/test/tmp/file1.txt"
    """
    hello
    """
    And a file "/tmp/test/tmp/file2.txt"
    """
    world
    """
    And a zip file "/tmp/test/dir/file.zip":
      | /tmp/test/tmp/file1.txt |
      | /tmp/test/tmp/file2.txt |
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: [ file1.txt  file2.txt ]
    """
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: [{
      name: file1.txt
      string: hello
    }{
      name: file2.txt
      string: world
    }]
    """
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip= {
      'file1.txt': {
        name: file1.txt
        string: hello
      }
      'file2.txt': {
        name: file2.txt
        string: world
      }
    }
    """
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: {
      file1.txt: hello
      file2.txt: world
    }
    """

  Scenario: file not exist in zip
    Given an empty zip file "/tmp/test/dir/empty.zip"
    Then java.io.File "/tmp/test/dir/empty.zip" should failed:
    """
    unzip= {
      'not-exist.txt': {...}
    }
    """
    And error message should be:
    """

    unzip= {
    ^
      'not-exist.txt': {...}
    }
    Get property `not-exist.txt` failed, property can be:
      1. public field
      2. public getter
      3. public no args method
      4. Map key value
      5. customized type getter
      6. static method extension
    File or File Group <not-exist.txt> not found
    Implicit list mapping is not allowed in current version of DAL, use `not-exist.txt[]` instead
    """

  Scenario: verify all files in zip
    Given a file "/tmp/test/tmp/file.txt"
    """
    hello
    """
    And a zip file "/tmp/test/dir/file.zip":
      | /tmp/test/tmp/file.txt |
    Then java.io.File "/tmp/test/dir/file.zip" should failed:
    """
    unzip= {}
    """
    And error message should be:
    """

    unzip= {}
         ^
    Unexpected fields `file.txt` in unzip
    """

#    TODO one empty folder
#    TODO one empty folder
#    TODO one folder with one file
#    TODO one folder with one folder with file
#    TODO one folder with one file and one file

#    TODO list path and files
