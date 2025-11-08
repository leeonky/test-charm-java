Feature: Association with Foreign Key

  Rule: Create Primary

    Scenario: Create Primary - Create Primary Entity
      Given Exists data "Company":
        | name |
        | Acme |
      Then All data should be:
        """
        : {
          DataBase: {
            Company= | id | name |
                     | *  | Acme |

            Department= []

            Employee= []
          }

          Company= {
            id= ::root.DataBase.Company[0].id
            name= Acme
            departments= []
          }
        }
        """

  Rule: Create Foreign

    Scenario: With Default Primary
      Given Exists data "Department":
        | name |
        | HR   |
      Then All data should be:
        """
        : {
          DataBase: {
            Company= | id | name      |
                     | *  | /^name.*/ |

            Department= | id | name | companyid                     |
                        | *  | HR   | ::root.DataBase.Company[0].id |

            Employee= []
          }
        }
        """

    Scenario: With Primary Entity Property Value
      Given Exists data "Department":
        | name | company.name |
        | HR   | Acme         |
      Then All data should be:
        """
        : {
          DataBase: {
            Company= | id | name |
                     | *  | Acme |

            Department= | id | name | companyid                     |
                        | *  | HR   | ::root.DataBase.Company[0].id |

            Employee= []
          }
        }
        """
#      Then All data should be:
#        """
#        Department: {
#          id= *
#          name= HR
#  #        company: {...}
#  #        employees= []
#        }
#        """
  #    Then All data should be:
  #      """
  #      Company= {
  #        id= *
  #        name= /^name.*/
  #        departments: [{...}]
  #      }
  #      """

    Scenario: Attach the Existed Primary Entity
      Given Exists data "Company":
        | name   |
        | Globex |
        | Acme   |
      Given Exists data "Department":
        | name | company.name |
        | HR   | Acme         |
      Then All data should be:
        """
        : {
          DataBase: {
            Company= | id | name   |
                     | *  | Globex |
                     | *  | Acme   |

            Department= | id | name | companyid                                          |
                        | *  | HR   | (::root.DataBase.Company::filter: {name= Acme}).id |

            Employee= []
          }
        }
        """

