Feature: dir/file with java File

  Background:
    Given root folder "/tmp/test/dir"

  Scenario: empty folder
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

  Scenario: folder with sub-folders
    Given a folder "/tmp/test/dir/folder1"
    Given a folder "/tmp/test/dir/folder2"
    Then java.io.File "/tmp/test/dir" should:
    """
    : ['folder1' 'folder2']
    """
    And java.io.File "/tmp" should:
    """
    : {
      test/dir: {
        folder1: []
        folder2: []
      }
    }
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
    Then java.io.File "/tmp" should:
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
    Then java.io.File "/tmp" should:
    """
    : {
      test/dir/folder1/'file1.txt'.string: file1
      test/dir/folder2/'file2.txt'.string: file2
    }
    """
    Then java.io.File "/tmp" should:
    """
    : {
      test/dir: {
        folder1/'file1.txt'.string: file1
        folder2/'file2.txt'.string: file2
      }
    }
    """
