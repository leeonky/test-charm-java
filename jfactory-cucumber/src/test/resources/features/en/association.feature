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

            Department= | id | name | companyid         |
                        | *  | HR   | ::root.Company.id |

            Employee= []
          }
        }
        """
      Then All data should be:
        """
        Department= {
          id: {...}
          name= HR
          company: {
            id= ::root.DataBase.Company[0].id
            name= /^name.*/
          }
          companyId: *
          employees= []
        }
        """
      Then All data should be:
        """
        Company= {
          id: {...}
          name= /^name.*/
          departments= [{
            id: {...}
            name= HR
            company: {
              id= ::root.DataBase.Company[0].id
              name= /^name.*/
            }
            companyId: *
            employees= []
          }]
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

            Department= | id | name | companyid         |
                        | *  | HR   | ::root.Company.id |

            Employee= []
          }
        }
        """
      Then All data should be:
        """
        Department= {
          id: {...}
          name= HR
          company: {
            id= ::root.Company.id
            name= Acme
          }
          companyId: *
          employees= []
        }
        """
      Then All data should be:
        """
        Company= {
          id: {...}
          name= Acme
          departments= [{
            id: {...}
            name= HR
            company: {
              id= ::root.Company.id
              name= Acme
            }
            companyId: *
            employees= []
          }]
        }
        """

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

            Department= | id | name | companyid                                 |
                        | *  | HR   | (::root.Company::filter: {name= Acme}).id |

            Employee= []
          }
        }
        """
      Then All data should be:
        """
        Department= {
          id: {...}
          name= HR
          company: {
            id= (::root.Company::filter: {name= Acme}).id
            name= Acme
          }
          companyId: *
          employees= []
        }
        """
      Then All data should be:
        """
        Company= [{
          id: {...}
          name= Globex
          departments= []
        }{
          id: {...}
          name= Acme
          departments= [{
            id: {...}
            name= HR
            company: {
              id= (::root.Company::filter: {name= Acme}).id
              name= Acme
            }
            companyId: *
            employees= []
          }]
        }]
        """

    Scenario: Create From Primary
      Given Exists data "Company":
        """
        name: Acme
        departments: | name |
                     | HR   |
        """
      Then All data should be:
        """
        : {
          DataBase: {
            Company= | id | name |
                     | *  | Acme |

            Department= | id | name | companyid         |
                        | *  | HR   | ::root.Company.id |

            Employee= []
          }
        }
        """
      Then All data should be:
        """
        Department= {
          id: {...}
          name= HR
          company: {
            id= ::root.Company.id
            name= Acme
          }
          companyId: *
          employees= []
        }
        """
      Then All data should be:
        """
        Company= {
          id: {...}
          name= Acme
          departments= [{
            id: {...}
            name= HR
            company: {
              id= ::root.Company.id
              name= Acme
            }
            companyId: *
            employees= []
          }]
        }
        """

    Scenario: Dirty Foreign Entity
      Given Exists data "Department":
        """
          | name | company | companyId |
          | HR   | null    | 100       |
        """
      Then All data should be:
        """
        : {
          DataBase: {
            Company= []

            Department= | id | name | companyid |
                        | *  | HR   | 100L      |

            Employee= []
          }
        }
        """
      Then All data should be:
        """
        Department= {
          id: {...}
          name= HR
          company: null
          companyId: 100
          employees= []
        }
        """
