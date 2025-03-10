Feature: dir/file with java File

  Background:
    Given root folder "/tmp/work/test/dir"

  Scenario: file in list verification should list sub files
    Then java.io.File "/tmp/work/test/dir" should:
    """
    = []
    """
    Given a file "/tmp/work/test/dir/file1.txt"
    """
    hello1
    """
    Given a file "/tmp/work/test/dir/file2.txt"
    """
    hello2
    """
    Then java.io.File "/tmp/work/test/dir/" should:
    """
    : [{
      name: file1.txt
      string: hello1
    }{
      name: file2.txt
      string: hello2
    }]
    """

  Scenario: file in object verification should list sub files, key is file name, value is file
    When java.io.File "/tmp/work/test/dir" should:
    """
    = {}
    """
    Given a file "/tmp/work/test/dir/file.txt"
    """
    hello
    """
    Then java.io.File "/tmp/work/test/dir/file.txt" should:
    """
    : {
      name: file.txt
      string: hello
    }
    """

  Scenario: compare file with string should use file name
    Given a folder "/tmp/work/test/dir/folder1"
    And a folder "/tmp/work/test/dir/folder2"
    And a file "/tmp/work/test/dir/file.txt"
    """
    any
    """
    Then java.io.File "/tmp/work/test/dir" should:
    """
    : ['file.txt' 'folder1' 'folder2']
    """

  Scenario: access folder by name as 'property' name
    Given a folder "/tmp/work/test/dir/folder1"
    Given a folder "/tmp/work/test/dir/folder2"
    And java.io.File "/tmp/work" should:
    """
    : {
      test/dir: {
        folder1: []
        folder2: []
      }
    }
    """

  Scenario: access file by name as 'property' name
    Given a folder "/tmp/work/test/dir/folder1"
    Given a file "/tmp/work/test/dir/folder1/file1.txt"
    """
    file1
    """
    Given a folder "/tmp/work/test/dir/folder2"
    Given a file "/tmp/work/test/dir/folder2/file2.txt"
    """
    file2
    """
    Then java.io.File "/tmp/work" should:
    """
    : {
      test/dir= {
        folder1= {
          'file1.txt'.string: file1
        }
        folder2= {
          'file2.txt'.string: file2
        }
      }
    }
    """
    Then java.io.File "/tmp/work" should:
    """
    : {
      test/dir/folder1/'file1.txt'.string: file1
      test/dir/folder2/'file2.txt'.string: file2
    }
    """
    Then java.io.File "/tmp/work" should:
    """
    : {
      test/dir: {
        folder1/'file1.txt'.string: file1
        folder2/'file2.txt'.string: file2
      }
    }
    """

  Scenario: get file content by extension
    Given a file "/tmp/work/test/dir/file.txt"
    """
    hello-world
    """
    Then java.io.File "/tmp/work" should:
    """
    : {
      test/dir/file.txt= hello-world
    }
    """

  Scenario: checking folder with file group and extension
    Given a file "/tmp/work/test/dir/file.txt"
    """
    hello-world
    """
    Then java.io.File "/tmp/work" should:
    """
    : {
      test/dir= {
        file.txt= hello-world
      }
    }
    """
    Given a file "/tmp/work/test/dir/file2.txt"
    """
    unexpected
    """
    Then java.io.File "/tmp/work" should failed:
    """
    : {
      test/dir= {
        file.txt= hello-world
      }
    }
    """
    And error message should be:
    """

    : {
      test/dir= {
              ^
        file.txt= hello-world
      }
    }

    Unexpected fields `file2.txt` in test/dir
    """

  Scenario: string to file
    Given a file "/tmp/work/test/dir/file.txt"
    """
    hello-world
    """
    Then the following should pass:
    """
    '/tmp/work'.file: {
      test/dir= {
        file.txt= hello-world
      }
    }
    """

  Scenario: raise error when file-group not exist
    Then java.io.File "/tmp/work/test/dir" should failed:
    """
    : {
      un-exist.txt: 'any'
    }
    """
    And error message should be:
    """

    : {
      un-exist.txt: 'any'
      ^
    }

    Get property `un-exist` failed, property can be:
      1. public field
      2. public getter
      3. public method
      4. Map key value
      5. customized type getter
      6. static method extension
    java.io.FileNotFoundException: File or File Group <un-exist> not found
    """

  Scenario: raise error when file not exist with file group
    Given a file "/tmp/work/test/dir/file.txt"
    """
    hello-world
    """
    Then java.io.File "/tmp/work/test/dir" should failed:
    """
    : {
      file.not-exist: 'any'
    }
    """
    And error message should be:
    """

    : {
      file.not-exist: 'any'
           ^
    }

    Get property `not-exist` failed, property can be:
      1. public field
      2. public getter
      3. public method
      4. Map key value
      5. customized type getter
      6. static method extension
    java.io.FileNotFoundException: File `file.not-exist` not exist
    """

  Scenario: return file object when extension not registered
    Given a file "/tmp/work/test/dir/file.not-registered"
    """
    hello-world
    """
    Then java.io.File "/tmp/work/test/dir" should:
    """
    : {
      file.not-registered: {
        class.simpleName: 'File'
        name: file.not-registered
        string: hello-world
      }
    }
    """

  Scenario: checking all files with file group
    Given a file "/tmp/work/test/dir/file.txt"
    """
    hello-world
    """
    Given a file "/tmp/work/test/dir/file.log"
    """
    any
    """
    Given a file "/tmp/work/test/dir/another.txt"
    """
    any
    """
    Then java.io.File "/tmp/work/test/dir" should failed:
    """
    : {
      file= {
        txt: hello-world
      }
    }
    """
    And error message should be:
    """

    : {
      file= {
          ^
        txt: hello-world
      }
    }

    Unexpected fields `log` in file
    """

  Scenario: list verification by file group
    Given a file "/tmp/work/test/dir/file.txt"
    """
    hello-world
    """
    Given a file "/tmp/work/test/dir/file.log"
    """
    a-log
    """
    Then java.io.File "/tmp/work/test/dir" should:
    """
    : {
      file: [{
        name: file.log
        string: a-log
      }
      {
        name: file.txt
        string: hello-world
      }]
    }
    """

  Rule: dump

    Scenario: inspect empty dir
      Then java.io.File "/tmp/work/test/dir" should dump:
      """
      java.io.File /tmp/work/test/dir/
      """

    Scenario: dump file
      Given a file "/tmp/work/test/dir/file1.txt"
      """
      hello1
      """
      And set file attribute "/tmp/work/test/dir/file1.txt"
      """
      rwxr-xr-x root root 2022-10-09T06:47:01Z
      """
      Then java.io.File "/tmp/work/test/dir/file1.txt" should dump:
      """
      java.io.File
      rwxr-xr-x root root      6 2022-10-09T06:47:01Z file1.txt
      """

    Scenario: dump folder with file
      Given root folder "/tmp/work/test/dir/sub"
      Given a file "/tmp/work/test/dir/sub/file1.txt"
      """
      hello1
      """
      And set file attribute "/tmp/work/test/dir/sub/file1.txt"
      """
      rwxr-xr-x root root 2022-10-09T06:47:01Z
      """
      Given a file "/tmp/work/test/dir/sub/file2.txt"
      """
      world
      """
      And set file attribute "/tmp/work/test/dir/sub/file2.txt"
      """
      rwxr-xr-x root root 2022-10-09T06:47:11Z
      """
      Given root folder "/tmp/work/test/dir/sub2"
      Then java.io.File "/tmp/work/test/dir" should dump:
      """
      java.io.File /tmp/work/test/dir/
      sub/
          rwxr-xr-x root root      6 2022-10-09T06:47:01Z file1.txt
          rwxr-xr-x root root      5 2022-10-09T06:47:11Z file2.txt
      sub2/
      """
