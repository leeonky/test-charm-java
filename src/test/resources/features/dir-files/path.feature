Feature: dir/file with java path

  Background:
    Given root folder "/tmp/test/dir"

  Scenario: empty folder
    Then java.nio.Path "/tmp/test/dir" should:
    """
    = []
    """
    And java.nio.Path "/tmp/test/dir" should:
    """
    = {}
    """

  Scenario: single file
    Given a file "/tmp/test/file.txt"
    """
    hello
    """
    Then java.nio.Path "/tmp/test/file.txt" should:
    """
    : {
      name: file.txt
      string: hello
    }
    """
    Then java.nio.Path "/tmp/test/file.txt" should:
    """
    : 'file.txt'
    """

  Scenario: folder with sub-folders
    Given a folder "/tmp/test/dir/folder1"
    Given a folder "/tmp/test/dir/folder2"
    Then java.nio.Path "/tmp/test/dir" should:
    """
    : ['folder1' 'folder2']
    """
    And java.nio.Path "/tmp" should:
    """
    : {
      test/dir: {
        folder1: []
        folder2: []
      }
    }
    """

  Scenario: two files
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

  Scenario: folder and file
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

  Scenario: file extension
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

  Scenario: checking file count with extension
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

  Scenario: string to path
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
