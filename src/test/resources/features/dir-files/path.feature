Feature: dir/file with java path

  Background:
    Given root folder "/tmp/test/dir"

  Scenario: file in list verification should list sub files
    Then java.nio.Path "/tmp/test/dir" should:
    """
    = []
    """
    Given a file "/tmp/test/dir/file1.txt"
    """
    hello1
    """
    Given a file "/tmp/test/dir/file2.txt"
    """
    hello2
    """
    Then java.nio.Path "/tmp/test/dir/" should:
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
    When java.nio.Path "/tmp/test/dir" should:
    """
    = {}
    """
    Given a file "/tmp/test/dir/file.txt"
    """
    hello
    """
    Then java.nio.Path "/tmp/test/dir/file.txt" should:
    """
    : {
      name: file.txt
      string: hello
    }
    """

  Scenario: compare file with string should use file name
    Given a folder "/tmp/test/dir/folder1"
    And a folder "/tmp/test/dir/folder2"
    And a file "/tmp/test/dir/file.txt"
    """
    any
    """
    Then java.nio.Path "/tmp/test/dir" should:
    """
    : ['file.txt' 'folder1' 'folder2']
    """

  Scenario: access folder by name as 'property' name
    Given a folder "/tmp/test/dir/folder1"
    Given a folder "/tmp/test/dir/folder2"
    And java.nio.Path "/tmp" should:
    """
    : {
      test/dir: {
        folder1: []
        folder2: []
      }
    }
    """

  Scenario: access file by name as 'property' name
    Given a folder "/tmp/test/dir/folder1"
    Given a file "/tmp/test/dir/folder1/file1.txt"
    """
    file1
    """
    Given a folder "/tmp/test/dir/folder2"
    Given a file "/tmp/test/dir/folder2/file2.txt"
    """
    file2
    """
    Then java.nio.Path "/tmp" should:
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
    Then java.nio.Path "/tmp" should:
    """
    : {
      test/dir/folder1/'file1.txt'.string: file1
      test/dir/folder2/'file2.txt'.string: file2
    }
    """
    Then java.nio.Path "/tmp" should:
    """
    : {
      test/dir: {
        folder1/'file1.txt'.string: file1
        folder2/'file2.txt'.string: file2
      }
    }
    """

  Scenario: get file content by extension
    Given a file "/tmp/test/dir/file.txt"
    """
    hello-world
    """
    Then java.nio.Path "/tmp" should:
    """
    : {
      test/dir/file.txt= hello-world
    }
    """

  Scenario: checking folder with file group and extension
    Given a file "/tmp/test/dir/file.txt"
    """
    hello-world
    """
    Then java.nio.Path "/tmp" should:
    """
    : {
      test/dir= {
        file.txt= hello-world
      }
    }
    """
    Given a file "/tmp/test/dir/file2.txt"
    """
    unexpected
    """
    Then java.nio.Path "/tmp" should failed:
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
    Given a file "/tmp/test/dir/file.txt"
    """
    hello-world
    """
    Then the following should pass:
    """
    '/tmp/'.path: {
      test/dir= {
        file.txt= hello-world
      }
    }
    """
