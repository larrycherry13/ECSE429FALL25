Feature File Index – Part B (ECSE-429)

This index maps each user story (from UserStories.md) to the Cucumber .feature file
in partB/cucumber/src/test/resources/features/.

Each .feature file:
- Uses a Background section to set initial conditions.
- Contains Scenario Outlines with Examples tables.
- Includes at least three flows: Normal, Alternate, Error.
- Assumes cleanup will be handled by Hooks.java so tests can run in any order.


────────────────────────
Todos (Laurent)
────────────────────────

File: todos/create_todo.feature
Covers stories:
- T1. Create a new todo.

Scenarios inside:
- Normal flow: create todo with valid title + description → expect 201 Created and retrievable by ID.
- Alternate flow: create todo with title only (no description) → still 201, doneStatus defaults to false (undocumented but observed).
- Error flow: create todo with invalid or malformed JSON → expect 4xx / 400.
Includes Examples tables with different titles/descriptions.

File: todos/view_todo.feature
Covers stories:
- T2. Retrieve a todo by ID.

Scenarios inside:
- Normal flow: GET /todos/{id} → expect 200 and correct fields.
- Alternate flow: GET /todos?title=<title> and verify it appears in the list.
- Error flow: GET /todos/{badId} → expect 404 Not Found.

File: todos/complete_todo.feature
Covers stories:
- T3. Mark todo completed.

Scenarios inside:
- Normal flow: update doneStatus to true → expect 200.
- Alternate flow: mark it complete again → expect 200 / idempotent.
- Error flow: set doneStatus on a non-existent ID → expect 404.

File: todos/update_todo.feature
Covers stories:
- T4. Update the description of an existing todo.

Scenarios inside:
- Normal flow: PUT /todos/{id} with new description → expect 200 and updated body.
- Alternate flow: PUT /todos/{id} with same description → still 200, no unintended side effects.
- Error flow: PUT /todos/{badId} → expect 404.
Also includes malformed JSON update as part of error coverage.

File: todos/delete_todo.feature
Covers stories:
- T5. Delete a todo.

Scenarios inside:
- Normal flow: DELETE /todos/{id} → expect 200, and afterward GET /todos/{id} returns 404.
- Alternate flow: DELETE /todos/{id} twice → first 200, second 404.
- Error flow: DELETE /todos/{nonsenseId} → expect 400.
Note: observed behavior is that DELETE returns `{}` as body. This is captured.


────────────────────────
Projects (Noah)
────────────────────────

File: projects/project_stories.feature
Covers stories:
- P1. Create a project.
- P2. Update a project.
- P3. View todos in a project.
- P4. Add todo to a project.
- P5. Delete a project.

Scenarios (to be filled by Projects owner):
- Normal flow: Create project (POST /projects) → 201.
- Alternate flow: Add existing todo to a project → 201 / 200.
- Error flow: Try to access or update a project that doesn’t exist → 404.
Background ensures service is running and clean state.
Hooks will handle cleanup.


────────────────────────
Categories (Vladimir)
────────────────────────

File: categories/category_stories.feature
Covers stories:
- C1. Create a category.
- C2. Update a category.
- C3. Assign a todo to a category.
- C4. List todos in a category.
- C5. Delete a category.

Scenarios (to be filled by Categories owner):
- Normal flow: Create category and assign todo using title.
- Alternate flow: Re-assign / rename category.
- Error flow: Assign an existing todo to an existing category using { "id": "<todoId>" } → 404 even though both exist. This is an undocumented / bug behavior observed during exploratory testing.
This captures discovered instability in category <-> todo linking.


────────────────────────
Interoperability (Marcello)
────────────────────────

File: interoperability/interoperability_stories.feature
Covers stories:
- I1. Link a todo to a project.
- I2. Link a todo to a category.
- I3. Move / re-link a todo.
- I4. View relationships between todos, projects, categories.
- I5. Delete a project or category and observe what happens to linked todos.

Scenarios (to be filled by Interop owner):
- Normal flow: Link todo to project and confirm it appears under that project.
- Alternate flow: Move todo between parents.
- Error flow: Delete parent project or category and confirm the todo still exists (orphan behavior). We observed that deletes do NOT cascade and the todo remains accessible. This will be asserted explicitly.
