Feature: View Project
  As a user
  I want to view project details
  So that I can see project information

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - retrieve project by ID
    When I create a project with title "Review" and description "Code review session"
    And I retrieve that project by its id
    Then the response status should be 200

  Scenario: Alternate flow - retrieve immediately after creation
    When I create a project with title "Testing" and description "QA testing"
    Then the response status should be 201
    And that project should be retrievable by its id

  Scenario: Error flow - retrieve non-existent project
    When I retrieve that project by its id
    Then the response status should be 404
