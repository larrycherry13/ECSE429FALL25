Bug Summary – Part B (ECSE-429)

Format:
• Executive summary (≤ 80 chars)  
• Description  
• Potential impact on system operation  
• Steps to reproduce  
• Related story

──────────────────────────────
BUG #1 – Malformed JSON Fails Silently
──────────────────────────────
Description:  
POST /todos with malformed JSON (`{title:"no quote}`) returns 500 Internal Server Error instead of 400 Bad Request.

Impact:  
Developers cannot reliably detect invalid payloads; clients receive generic errors without meaningful feedback.

Steps to reproduce:  
1. Send `POST /todos` with `{title:"no quote}`  
2. Observe 500 status.  
3. Expected: 400 Bad Request with error message.

Related story: T1 (Create Todo – Error flow)

──────────────────────────────
BUG #2 – Category Link by ID Returns 404
──────────────────────────────
Description:  
POST `/categories/{cid}/todos` with body `{"id":"<todoId>"}` fails with 404 even when both resources exist.  
Using `{"title":"todo name"}` works but creates a new todo instead of linking the existing one.

Impact:  
Breaks documented behavior and causes duplicate todos during category assignment.

Steps to reproduce:  
1. Create a todo and category.  
2. Attempt to link via ID.  
3. Observe 404 response.  
4. Attempt to link via title → 201 and duplicate todo appears.

Related story: C3 (Assign Todo to Category – Error flow)

──────────────────────────────
BUG #3 – DELETE Response Body Is Inconsistent
──────────────────────────────
Description:  
DELETE /todos/{id} returns status 200 with empty body `{}`.  
Expected behavior is 204 No Content or structured confirmation.

Impact:  
Inconsistent responses make API less predictable for clients.

Steps to reproduce:  
1. Create todo.  
2. DELETE it.  
3. Inspect response body → `{}`.

Related story: T5 (Delete Todo – Normal flow)

──────────────────────────────
BUG #4 – Deleting Parent Does Not Cascade
──────────────────────────────
Description:  
When a todo is linked to a project or category, deleting the project or category does not delete the todo or unlink it.  
Todo remains accessible and references broken links.

Impact:  
Data integrity issues and stale references may accumulate.

Steps to reproduce:  
1. Create project and todo.  
2. Link todo to project.  
3. DELETE project.  
4. GET /todos → todo still exists.

Related story: I5 (Delete Parent Object Behavior – Error flow)

──────────────────────────────
BUG #5 – GET Todos After Server Reset Shows Cached Data
──────────────────────────────
Description:  
Occasionally after multiple runs, GET /todos returns previous session’s data even after restarting the jar.

Impact:  
Indicates possible in-memory cache not reset properly; may affect repeatability of tests.

Steps to reproduce:  
1. Create todos.  
2. Stop and re-launch server.  
3. GET /todos → old items still appear.

Related story: Hooks / Test Background (clean initial state)
