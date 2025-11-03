Feature: Delete Project
  As a user
  I want to remove old or completed projects
  So that my workspace stays organized

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - create and verify project exists
    When I create a project with title "TempProject" and description "to be removed"
    Then the response status should be 201
    And that project should be retrievable by its id

  Scenario: Alternate flow - create project with minimal data
    When I create a project with only title "MinimalProject"
    Then the response status should be 201

  Scenario: Error flow - delete invalid project id
    When I delete a project that does not exist
    Then the response status should be 404
