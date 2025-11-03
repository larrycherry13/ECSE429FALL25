Feature: Create Project
  As a user
  I want to create new projects
  So that I can organize related tasks

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario Outline: Normal flow - create project with valid title and description
    When I create a project with title "<title>" and description "<description>"
    Then the response status should be 201
    And that project should be retrievable by its id
  Examples:
    | title      | description       |
    | Team Alpha | Development team  |
    | Sprint 3   | Q4 deliverables   |

  Scenario Outline: Alternate flow - create project with only title
    When I create a project with only title "<title>"
    Then the response status should be 201
    And that project should be retrievable by its id
  Examples:
    | title        |
    | QuickProj    |
    | Marketing    |

  Scenario: Error flow - malformed JSON request
    When I try to create a project with malformed JSON
    Then the response status should be 400
    And no new project should have been created
