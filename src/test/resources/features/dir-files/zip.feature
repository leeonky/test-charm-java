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

  Scenario: unzip with one file
    Given a file "/tmp/test/tmp/file.txt"
    """
    hello
    """
    And a zip file "/tmp/test/dir/file.zip":
      | /tmp/test/tmp/file.txt |
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: [ file.txt ]
    """
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: [{
      name: file.txt
      string: hello
    }]
    """
#
#    Then java.io.File "/tmp/test/dir/file.zip" should:
#    """
#    unzip: {
#      'file.txt': {
#        name: file.txt
#        string: hello
#      }
#    }
#    """
#    Then java.io.File "/tmp/test/dir/file.zip" should:
#    """
#    unzip: {
#      file.txt: hello
#    }
#    """

#    TODO empty zip
#    TODO one file
#    TODO two files
#    TODO two files
#    TODO one empty folder
#    TODO one empty folder
#    TODO one folder with one file
#    TODO one folder with one folder with file
#    TODO one folder with one file and one file

#    TODO list path and files
