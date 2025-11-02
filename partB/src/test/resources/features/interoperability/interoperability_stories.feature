Feature: Interoperability stories
  As a project manager
  I want todos, projects, and categories to work together
  So that my data stays consistent

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  # ==================== I1: Link Todo to Project ====================

  # Normal Flow
  Scenario Outline: Successfully link an existing todo to an existing project
    Given a todo exists with title "<todo_title>"
    And a project exists with title "<project_title>"
    When I link that todo to that project
    Then the response status should be 201
    And the todo should be linked to the project
    And the project should contain that todo in its tasks

    Examples:
      | todo_title        | project_title     |
      | Write report      | Q4 Planning       |
      | Review code       | Dev Sprint        |

  # Error Flow
  Scenario: Attempt to link a todo to a nonexistent project
    Given a todo exists with title "Submit invoice"
    When I try to link that todo to a nonexistent project
    Then the response status should be 404

  # ==================== I2: Link Todo to Category ====================

  # Normal Flow
  Scenario Outline: Successfully link an existing todo to an existing category
    Given a todo exists with title "<todo_title>"
    And a category exists with title "<category_title>"
    When I link that todo to that category
    Then the response status should be 201
    And the todo should be linked to the category

    Examples:
      | todo_title        | category_title    |
      | Buy groceries     | Personal          |
      | Team meeting      | Work              |

  # Error Flow
  Scenario: Attempt to link a todo to a nonexistent category
    Given a todo exists with title "Plan vacation"
    When I try to link that todo to a nonexistent category
    Then the response status should be 404

  # ==================== I3: Move Todo Between Projects/Categories ====================

  # Normal Flow - Move todo between projects
  Scenario: Move a todo from one project to another
    Given a todo exists with title "Refactor database"
    And a project exists with title "Backend Work"
    And that todo is linked to that project
    And another project exists with title "Tech Debt"
    When I unlink that todo from the first project
    And I link that todo to the second project
    Then the response status should be 201
    And the todo should be linked to the second project
    And the first project should not contain that todo

  # Normal Flow - Move todo between categories
  Scenario: Move a todo from one category to another
    Given a todo exists with title "Schedule dentist"
    And a category exists with title "Health"
    And that todo is linked to that category
    And another category exists with title "Urgent"
    When I unlink that todo from the first category
    And I link that todo to the second category
    Then the response status should be 201
    And the todo should be linked to the second category

  # Alternate Flow - Duplicate link handling
  Scenario: Link the same todo-project pair twice
    Given a todo exists with title "Update docs"
    And a project exists with title "Documentation"
    And that todo is linked to that project
    When I link that todo to that project again
    Then the response status should not be 500

  # ==================== I4: View Relationships ====================

  # Normal Flow
  Scenario: View todos that belong to a project
    Given a project exists with title "Mobile App"
    And a todo exists with title "Design UI"
    And that todo is linked to that project
    When I retrieve all tasks for that project
    Then the response status should be 200
    And the response should contain at least 1 todo

  # Normal Flow
  Scenario: View categories that a todo belongs to
    Given a todo exists with title "File taxes"
    And a category exists with title "Finance"
    And that todo is linked to that category
    When I retrieve all categories for that todo
    Then the response status should be 200
    And the response should contain at least 1 category

  # Normal Flow
  Scenario: View projects that a todo belongs to
    Given a todo exists with title "Fix bug"
    And a project exists with title "Bug Fixes"
    And that todo is linked to that project
    When I retrieve all projects for that todo
    Then the response status should be 200
    And the response should contain at least 1 project

  # Error Flow
  Scenario: View tasks for a nonexistent project
    When I try to retrieve tasks for a nonexistent project
    Then the response status should be 200

  # ==================== I5: Delete Parent Object Behavior (Orphan Testing) ====================

  # Normal Flow - Orphan behavior when project deleted
  Scenario: Delete a project with linked todos leaves todos intact
    Given a todo exists with title "Prepare slides"
    And a project exists with title "Conference Talk"
    And that todo is linked to that project
    When I delete that project
    Then the response status should be 200
    And that todo should still exist as an orphan
    And the orphaned todo should be accessible

  # Normal Flow - Orphan behavior when category deleted
  Scenario: Delete a category with linked todos leaves todos intact
    Given a todo exists with title "Water plants"
    And a category exists with title "Home"
    And that todo is linked to that category
    When I delete that category
    Then the response status should be 200
    And that todo should still exist as an orphan
    And the orphaned todo should be accessible

  # Alternate Flow - Full chain then delete
  Scenario: Link todo to both project and category then delete both parents
    Given a todo exists with title "Research topic"
    And a project exists with title "Thesis"
    And a category exists with title "Academic"
    And that todo is linked to that project
    And that todo is linked to that category
    When I delete that project
    And I delete that category
    Then that todo should still exist as an orphan
    And the orphaned todo should be accessible

  # ==================== Additional Error Flows ====================

  # Error Flow - Malformed payload
  Scenario: Try to link with malformed JSON payload
    Given a todo exists with title "Send email"
    When I try to link that todo with malformed JSON
    Then the response status should be 404

  # Error Flow - Empty payload
  Scenario: Try to link with empty JSON payload
    Given a todo exists with title "Call client"
    When I try to link that todo with empty JSON
    Then the response status should be 201
