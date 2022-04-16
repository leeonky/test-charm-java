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

  Scenario: zip has a empty folder
    Given a folder "/tmp/test/tmp/empty"
    And a zip file "/tmp/test/dir/file.zip":
      | /tmp/test/tmp/empty |
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: [empty]
    """
    And java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip= {
      empty= []
      empty= {}
    }
    """

  Scenario: one folder with one file
    Given a file "/tmp/test/tmp/file.txt"
    """
    hello
    """
    Given a folder "/tmp/test/tmp/empty"
    And a zip file "/tmp/test/dir/file.zip":
      | /tmp/test/tmp/file.txt |
      | /tmp/test/tmp/empty    |
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: [empty file.txt]
    """
    And java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip= {
      empty= []
      empty= {}
      file.txt: hello
    }
    """

  Scenario: folder with file
    Given a folder "/tmp/test/tmp/folder"
    Given a file "/tmp/test/tmp/folder/file.txt"
    """
    hello
    """
    And a zip file "/tmp/test/dir/file.zip":
      | /tmp/test/tmp/folder |
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: [folder],
    unzip: {
      folder: [file.txt]
      folder: {
        file.txt: hello
      }
    }
    """

  Scenario: folder with folder has a file
    Given a folder "/tmp/test/tmp/folder"
    Given a folder "/tmp/test/tmp/folder/child"
    Given a file "/tmp/test/tmp/folder/child/file.txt"
    """
    hello
    """
    And a zip file "/tmp/test/dir/file.zip":
      | /tmp/test/tmp/folder |
    Then java.io.File "/tmp/test/dir/file.zip" should:
    """
    unzip: [folder],
    unzip: {
      folder/child= {
        file.txt: hello
      }
    }
    """

  Scenario: raise error when extension not exist in group
    Given a folder "/tmp/test/tmp/folder"
    Given a file "/tmp/test/tmp/folder/file.txt"
    """
    hello
    """
    Given a file "/tmp/test/tmp/file.txt"
    """
    hello
    """
    And a zip file "/tmp/test/dir/file.zip":
      | /tmp/test/tmp/file.txt |
      | /tmp/test/tmp/folder   |
    Then java.io.File "/tmp/test/dir/file.zip" should failed:
    """
    unzip: {
      file.json: txt
    }
    """
    And error message should be:
    """

    unzip: {
      file.json: txt
           ^
    }
    Get property `json` failed, property can be:
      1. public field
      2. public getter
      3. public no args method
      4. Map key value
      5. customized type getter
      6. static method extension
    File `file.json` not exist
    Implicit list mapping is not allowed in current version of DAL, use `json[]` instead
    """
    Then java.io.File "/tmp/test/dir/file.zip" should failed:
    """
    unzip: {
      folder/file.json: txt
    }
    """
    And error message should be:
    """

    unzip: {
      folder/file.json: txt
                  ^
    }
    Get property `json` failed, property can be:
      1. public field
      2. public getter
      3. public no args method
      4. Map key value
      5. customized type getter
      6. static method extension
    File `file.json` not exist
    Implicit list mapping is not allowed in current version of DAL, use `json[]` instead
    """

  Scenario: nested zip files
    Given a folder "/tmp/test/tmp/folder"
    Given a folder "/tmp/test/tmp/zip1"
    Given a file "/tmp/test/tmp/zip1/file1.txt"
    """
    hello
    """
    Given a file "/tmp/test/tmp/zip1/file2.txt"
    """
    world
    """
    And a zip file "/tmp/test/tmp/folder/zip1.zip":
      | /tmp/test/tmp/zip1 |
    Given a file "/tmp/test/tmp/folder/foo.txt"
    """
    foo
    """
    Given a file "/tmp/test/tmp/bar.txt"
    """
    bar
    """
    And a zip file "/tmp/test/dir/zip.zip":
      | /tmp/test/tmp/folder  |
      | /tmp/test/tmp/bar.txt |

    Then java.io.File "/tmp/test/dir" should:
    """
    : {
      zip.zip: [
        bar.txt
        folder
      ]
    }
    """
    Then java.io.File "/tmp/test/dir" should:
    """
    = {
      zip.zip: {
        bar.txt: bar
        folder= {
          foo.txt= foo
          zip1.zip: {
            zip1: {
            file1.txt: hello
            file2.txt: world
            }
          }
        }
      }
    }
    """
    Then java.io.File "/tmp/test/dir" should:
    """
    zip.zip= {
        bar.txt= bar
        folder/foo.txt= foo
        folder/zip1.zip/zip1/file1.txt= hello
        folder/zip1.zip/zip1/file2.txt= world
    }
    """
