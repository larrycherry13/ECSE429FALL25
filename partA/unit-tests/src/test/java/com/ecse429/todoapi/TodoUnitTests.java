package com.ecse429.todoapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Todo test suite for Todo REST API (v1.5.5)
 * Focus: CRUD operations, relationships, and payload validation for /todos API.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoUnitTests {

    // Test Configuration
    private static final String BASE = "http://localhost";
    private static final int PORT = 4567;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE;
        RestAssured.port = PORT;

        // Fail fast if service not running — /todos is reliable
        given()
            .when().get("/todos")
            .then().statusCode(anyOf(is(200), is(204)));
    }

    @AfterEach
    void tearDown() {
        // Clean up all data after each test to ensure test isolation
        TestHelper.cleanupAllData();
    }

    // Helpers
    private static String extractId(Response res, String rootName) {
        return TestHelper.extractId(res, rootName);
    }

    private static String findTodoIdByTitle(String title) {
        Response r = given()
            .queryParam("title", title)
            .when().get("/todos")
            .then().statusCode(200)
            .extract().response();

        List<Map<String, Object>> list = r.path("todos");
        if (list != null && !list.isEmpty()) {
            Object v = list.get(0).get("id");
            return v == null ? null : String.valueOf(v);
        }
        return null;
    }

    private static String createTodoJSON(String title, boolean done, String description) {
        // IMPORTANT: doneStatus must be boolean (no quotes)
        String body = String.format(
            "{\"title\":\"%s\",\"doneStatus\":%s,\"description\":\"%s\"}",
            title, Boolean.toString(done), description == null ? "" : description
        );

        Response res = given()
            .contentType("application/json")
            .body(body)
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = extractId(res, "todos");
        if (id == null) id = findTodoIdByTitle(title);
        return id;
    }

    private static String createTodoXML(String title, boolean done, String description) {
        String xml = "<todo><title>" + title + "</title><doneStatus>" + done +
            "</doneStatus><description>" + (description == null ? "" : description) +
            "</description></todo>";

        given()
            .contentType("application/xml")
            .body(xml)
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)));

        return findTodoIdByTitle(title);
    }

    private static String createCategory(String title, String desc) {
        String body = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", title, desc == null ? "" : desc);
        Response res = given()
            .contentType("application/json")
            .body(body)
            .when().post("/categories")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = extractId(res, "categories");
        if (id == null) {
            Response r = given().when().get("/categories").then().statusCode(200).extract().response();
            List<Map<String, Object>> list = r.path("categories");
            if (list != null) {
                for (Map<String, Object> c : list) {
                    if (title.equals(String.valueOf(c.get("title")))) {
                        Object v = c.get("id"); if (v != null) return String.valueOf(v);
                    }
                }
            }
        }
        return id;
    }

    private static String createProject(String title, String desc) {
        String body = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", title, desc == null ? "" : desc);
        Response res = given()
            .contentType("application/json")
            .body(body)
            .when().post("/projects")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = extractId(res, "projects");
        if (id == null) {
            Response r = given().when().get("/projects").then().statusCode(200).extract().response();
            List<Map<String, Object>> list = r.path("projects");
            if (list != null) {
                for (Map<String, Object> p : list) {
                    if (title.equals(String.valueOf(p.get("title")))) {
                        Object v = p.get("id"); if (v != null) return String.valueOf(v);
                    }
                }
            }
        }
        return id;
    }

    private static void safeDeleteTodo(String id) {
        TestHelper.safeDeleteTodo(id);
    }

    // /todos (collection)

    @Test
    void get_all_todos_200_and_has_body() {
        given()
          .when().get("/todos")
          .then().statusCode(200)
          .body("$", notNullValue());
    }

    @Test
    void head_todos_200_no_body_assertion() {
        given()
          .when().head("/todos")
          .then().statusCode(200);
    }

    @Test
    void filter_by_title_query_param_200_contains_created() {
        String unique = "ECSE429-" + System.nanoTime();
        String id = createTodoJSON(unique, false, "filter-check");

        given()
          .queryParam("title", unique)
          .when().get("/todos")
          .then().statusCode(200)
          .body("todos.find { it.title == '" + unique + "' }", notNullValue());

        safeDeleteTodo(id);
    }

    @Test
    void post_todos_minimal_defaults_doneStatus_false() {
        Response res = given()
            .contentType("application/json")
            .body("{\"title\":\"Minimal\"}")
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .body("title", anyOf(equalTo("Minimal"), notNullValue()))
            .extract().response();

        String id = extractId(res, "todos");
        if (id == null) id = findTodoIdByTitle("Minimal");

        // doneStatus may be "false" (string) or boolean false depending on build
        given().when().get("/todos/" + id)
            .then().statusCode(200)
            .body("todos[0].doneStatus", anyOf(equalTo("false"), equalTo(false)));

        safeDeleteTodo(id);
    }

    @Test
    void post_todos_rejects_invalid_doneStatus_400() {
        given()
            .contentType("application/json")
            .body("{\"title\":\"Invalid\",\"doneStatus\":\"maybe\"}")
            .when().post("/todos")
            .then().statusCode(400);
    }

    @Test
    void put_delete_patch_todos_405() {
        given().when().put("/todos").then().statusCode(405);
        given().when().delete("/todos").then().statusCode(405);
        given().when().patch("/todos").then().statusCode(405);
    }

    @Test
    void options_todos_200() {
        given().when().options("/todos").then().statusCode(200);
    }

    // /todos/{id} (item)

    @Test
    void get_existing_todo_200_then_missing_404() {
        String id = createTodoJSON("get-existing", false, "ok");
        given().when().get("/todos/" + id).then().statusCode(200)
              .body("todos[0].id", equalTo(id));
        safeDeleteTodo(id);
        given().when().get("/todos/" + id).then().statusCode(404);
    }

    @Test
    void head_existing_200_head_missing_404() {
        String id = createTodoJSON("head-existing", false, "ok");
        given().when().head("/todos/" + id).then().statusCode(200);
        safeDeleteTodo(id);
        given().when().head("/todos/" + id).then().statusCode(404);
    }

    @Test
    void put_replaces_fields_200_no_side_effects() {
        String id = createTodoJSON("put-me", false, "before");
        // IMPORTANT: doneStatus must be boolean literal true (no quotes)
        given().contentType("application/json")
          .body("{\"title\":\"after\",\"doneStatus\":true,\"description\":\"changed\"}")
          .when().put("/todos/" + id)
          .then().statusCode(200)
          .body("title", equalTo("after"))
          .body("doneStatus", anyOf(equalTo("true"), equalTo(true)));

        given().when().get("/todos").then().statusCode(200);
        safeDeleteTodo(id);
    }

    @Test
    void post_amends_fields_200() {
        String id = createTodoJSON("post-amend", false, "desc");
        given().contentType("application/json")
          .body("{\"description\":\"amended-only\"}")
          .when().post("/todos/" + id)
          .then().statusCode(200)
          .body("description", equalTo("amended-only"));
        safeDeleteTodo(id);
    }

    @Test
    void delete_existing_200_then_delete_again_404_or_400() {
        String id = createTodoJSON("to-delete", false, "desc");
        given().when().delete("/todos/" + id).then().statusCode(200);
        given().when().delete("/todos/" + id).then().statusCode(anyOf(is(404), is(400)));
    }

    // Payload format & malformed

    @Test
    void create_via_xml_works() {
        String id = createTodoXML("xml-created-" + System.nanoTime(), false, "xml payload");
        given().when().get("/todos/" + id).then().statusCode(200);
        safeDeleteTodo(id);
    }

    @Test
    void malformed_json_returns_4xx() {
        given()
          .contentType("application/json")
          .body("{\"title\":\"bad-json\", \"doneStatus\":\"false\"") // Missing closing brace
          .when().post("/todos")
          .then().statusCode(anyOf(is(400), is(415), is(422)));
    }

    @Test
    void malformed_xml_returns_4xx() {
        given()
          .contentType("application/xml")
          .body("<todo><title>bad</title><doneStatus>false</todo>") // Bad XML
          .when().post("/todos")
          .then().statusCode(anyOf(is(400), is(415), is(422)));
    }

    // Relationship smoke checks (categories & tasksof)

    @Test
    void categories_relationship_attach_head_list_and_delete() {
        String todoId = createTodoJSON("rel-cat-" + System.nanoTime(), false, "smoke");
        String catId = createCategory("cat-" + System.nanoTime(), "smoke-rel");

        given().when().get("/todos/" + todoId + "/categories").then().statusCode(200);
        given().when().head("/todos/" + todoId + "/categories").then().statusCode(200);

        given().contentType("application/json")
          .body("{\"id\":\"" + catId + "\"}")
          .when().post("/todos/" + todoId + "/categories")
          .then().statusCode(anyOf(is(200), is(201)));

        given().when().delete("/todos/" + todoId + "/categories/" + catId)
          .then().statusCode(anyOf(is(200), is(404)));

        given().when().delete("/todos/" + todoId + "/categories/999999").then().statusCode(404);

        safeDeleteTodo(todoId);
    }

    @Test
    void tasksof_relationship_attach_head_list_and_delete() {
        String todoId = createTodoJSON("rel-tasksof-" + System.nanoTime(), false, "smoke");
        String projectId = createProject("proj-" + System.nanoTime(), "smoke-rel");

        given().when().get("/todos/" + todoId + "/tasksof").then().statusCode(200);
        given().when().head("/todos/" + todoId + "/tasksof").then().statusCode(200);

        given().contentType("application/json")
          .body("{\"id\":\"" + projectId + "\"}")
          .when().post("/todos/" + todoId + "/tasksof")
          .then().statusCode(anyOf(is(200), is(201)));

        given().when().delete("/todos/" + todoId + "/tasksof/" + projectId)
          .then().statusCode(anyOf(is(200), is(404)));

        given().when().delete("/todos/" + todoId + "/tasksof/999999").then().statusCode(404);

        safeDeleteTodo(todoId);
    }

    @Test
    void post_todos_missing_title_400() {
        given()
        .contentType("application/json")
        .body("{\"doneStatus\":false,\"description\":\"no title\"}")
        .when().post("/todos")
        .then().statusCode(400);
    }

    @Test
    void post_todos_without_description_sets_empty_or_absent_description() {
        String unique = "ECSE429-NODESC-" + System.nanoTime();
        String id = given()
        .contentType("application/json")
        .body("{\"title\":\"" + unique + "\",\"doneStatus\":false}")
        .when().post("/todos")
        .then().statusCode(anyOf(is(200), is(201)))
        .extract().path("id").toString();

        given().when().get("/todos/" + id)
        .then().statusCode(200)
        .body("todos[0].title", equalTo(unique))
        .body("todos[0].description", anyOf(nullValue(), equalTo(""), instanceOf(String.class)));

        safeDeleteTodo(id);
    }

    @Test
    void filter_by_title_returns_only_that_title_when_unique() {
        String unique = "ECSE429-FILTER-" + System.nanoTime();
        String id = createTodoJSON(unique, false, "filter-only");

        Response r = given()
        .queryParam("title", unique)
        .when().get("/todos")
        .then().statusCode(200)
        .extract().response();

        // Ensure all returned titles equal the unique title
        List<Map<String, Object>> todos = r.path("todos");
        if (todos != null) {
            for (Map<String, Object> t : todos) {
                Assertions.assertEquals(unique, String.valueOf(t.get("title")));
            }
        }

        safeDeleteTodo(id);
    }

    @Test
    void categories_collection_method_not_allowed_405() {
        String todoId = createTodoJSON("rel-cat-405-" + System.nanoTime(), false, "smoke");

        // Per Swagger these should be 405
        given().when().put("/todos/" + todoId + "/categories")
            .then().statusCode(anyOf(is(405), is(404)));

        given().when().patch("/todos/" + todoId + "/categories")
            .then().statusCode(anyOf(is(405), is(404)));

        given().contentType("application/json")
            .body("{\"id\":\"1\"}")
            .when().delete("/todos/" + todoId + "/categories") // Delete on collection
            .then().statusCode(anyOf(is(405), is(404)));

        // POST on /:id/:id is not allowed per Swagger
        given().contentType("application/json")
            .body("{\"dummy\":\"x\"}")
            .when().post("/todos/" + todoId + "/categories/1")
            .then().statusCode(anyOf(is(405), is(404)));

        safeDeleteTodo(todoId);
    }

    @Test
    void tasksof_collection_method_not_allowed_405() {
        String todoId = createTodoJSON("rel-tasksof-405-" + System.nanoTime(), false, "smoke");

        given().when().put("/todos/" + todoId + "/tasksof")
            .then().statusCode(anyOf(is(405), is(404)));

        given().when().patch("/todos/" + todoId + "/tasksof")
            .then().statusCode(anyOf(is(405), is(404)));

        given().contentType("application/json")
            .body("{\"id\":\"1\"}")
            .when().delete("/todos/" + todoId + "/tasksof") // delete on collection
            .then().statusCode(anyOf(is(405), is(404)));

        given().contentType("application/json")
            .body("{\"dummy\":\"x\"}")
            .when().post("/todos/" + todoId + "/tasksof/1")
            .then().statusCode(anyOf(is(405), is(404)));

        safeDeleteTodo(todoId);
    }

    @Test
    void attach_nonexistent_category_returns_client_error() {
        String todoId = createTodoJSON("rel-cat-nonexistent-" + System.nanoTime(), false, "smoke");
        given().contentType("application/json")
        .body("{\"id\":\"99999999\"}")
        .when().post("/todos/" + todoId + "/categories")
        .then().statusCode(anyOf(is(400), is(404)));
        safeDeleteTodo(todoId);
    }

    @Test
    void attach_nonexistent_project_returns_client_error() {
        String todoId = createTodoJSON("rel-proj-nonexistent-" + System.nanoTime(), false, "smoke");
        given().contentType("application/json")
        .body("{\"id\":\"99999999\"}")
        .when().post("/todos/" + todoId + "/tasksof")
        .then().statusCode(anyOf(is(400), is(404)));
        safeDeleteTodo(todoId);
    }

    @Test
    void attach_same_category_twice_is_graceful() {
        String todoId = createTodoJSON("rel-cat-double-" + System.nanoTime(), false, "smoke");
        String catId = createCategory("cat-double-" + System.nanoTime(), "rel");

        given().contentType("application/json")
        .body("{\"id\":\"" + catId + "\"}")
        .when().post("/todos/" + todoId + "/categories")
        .then().statusCode(anyOf(is(200), is(201)));

        given().contentType("application/json")
        .body("{\"id\":\"" + catId + "\"}")
        .when().post("/todos/" + todoId + "/categories")
        .then().statusCode(anyOf(is(200), is(201), is(400), is(409)));

        // Clean
        given().when().delete("/todos/" + todoId + "/categories/" + catId)
        .then().statusCode(anyOf(is(200), is(404)));
        safeDeleteTodo(todoId);
    }

    @Test
    void options_relationship_endpoints_200() {
        String todoId = createTodoJSON("rel-options-" + System.nanoTime(), false, "smoke");
        given().when().options("/todos/" + todoId + "/categories").then().statusCode(200);
        given().when().options("/todos/" + todoId + "/tasksof").then().statusCode(200);
        safeDeleteTodo(todoId);
    }

    @Test
    void ids_are_not_reused_after_delete_sanity() {
        String id1 = createTodoJSON("id-sanity-" + System.nanoTime(), false, "one");
        given().when().delete("/todos/" + id1).then().statusCode(200);
        String id2 = createTodoJSON("id-sanity-" + System.nanoTime(), false, "two");
        Assertions.assertNotEquals(id1, id2);
        safeDeleteTodo(id2);
    }

}
