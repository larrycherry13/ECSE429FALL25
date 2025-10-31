User Stories – Part B (ECSE-429)

Overview
Each team member defined five user stories related to using the REST API todo list manager.
Stories follow the required format:
"As a <role>, I want to <do something>, so that <value/benefit>."

These stories cover four domains:
- Todos
- Projects
- Categories
- Interoperability between these (linking / relationships / cleanup)


---------------------------------
Laurent – Todos
---------------------------------

T1. As a project manager, I want to create a new todo with a title and description so that I can track a task that needs to be done.

T2. As a team member, I want to retrieve a todo by its ID so that I can see its details and current status.

T3. As a project manager, I want to mark a todo as completed so that I can quickly tell which work items are finished.

T4. As a team member, I want to update the description of an existing todo so that I can clarify what needs to be done without creating a new task.

T5. As a project manager, I want to delete a todo so that outdated or duplicate tasks do not clutter the task list.


---------------------------------
Noah – Projects
---------------------------------

P1. As a team lead, I want to create a new project so that I can group related todos under one initiative.

P2. As a project owner, I want to update a project’s name or description so that the project reflects the current scope.

P3. As a team lead, I want to list all todos in a specific project so that I can see everything assigned to that project.

P4. As a project owner, I want to add an existing todo to a project so that the todo is tracked under that project.

P5. As a project owner, I want to delete a project so that I can remove work that is no longer active.


---------------------------------
Vladimir – Categories
---------------------------------

C1. As a user, I want to create a category so that I can label todos by theme (for example, "school", "work", "urgent").

C2. As a user, I want to rename or update a category so that I can keep my organization consistent as priorities change.

C3. As a user, I want to assign a todo to a category so that I can filter or search todos by that category.

C4. As a user, I want to view all todos in a given category so that I can see everything that relates to that category.

C5. As a user, I want to delete a category so that unused or duplicate categories don’t keep showing up.


---------------------------------
Marcello – Interoperability
---------------------------------

I1. As a project manager, I want to link an existing todo to an existing project so that I can track responsibility for that project.

I2. As a user, I want to link an existing todo to an existing category so that I can classify that todo.

I3. As a user, I want to move a todo from one project/category to another so that I can reorganize work when priorities shift.

I4. As a project manager, I want to view relationships (which todos belong to which project/category) so that I can audit ownership.

I5. As a project manager, I want to remove or delete a project or category and understand what happens to the linked todos so that I know whether data is cleaned up or left behind.

(Note: We observed during exploratory testing that deleting a project or category does NOT remove the linked todo. The todo remains accessible. This behavior is captured in I5.)
