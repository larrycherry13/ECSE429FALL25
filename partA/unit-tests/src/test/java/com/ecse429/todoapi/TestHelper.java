package com.ecse429.todoapi;

import io.restassured.response.Response;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Shared test utilities for Todo Manager API Unit Tests
 * 
 * This class provides common helper methods used across all test classes,
 * including database cleanup, object creation, and deletion utilities.
 */
public class TestHelper {

    /**
     * Comprehensive cleanup method that removes all objects from the database.
     */
    public static void cleanupAllData() {
        try {
            // Get and delete all todos
            Response todosResponse = given().when().get("/todos");
            if (todosResponse.getStatusCode() == 200) {
                List<Map<String, Object>> todos = todosResponse.path("todos");
                if (todos != null) {
                    for (Map<String, Object> todo : todos) {
                        String id = String.valueOf(todo.get("id"));
                        deleteIfExists("/todos/" + id);
                    }
                }
            }

            // Get and delete all categories
            Response categoriesResponse = given().when().get("/categories");
            if (categoriesResponse.getStatusCode() == 200) {
                List<Map<String, Object>> categories = categoriesResponse.path("categories");
                if (categories != null) {
                    for (Map<String, Object> category : categories) {
                        String id = String.valueOf(category.get("id"));
                        deleteIfExists("/categories/" + id);
                    }
                }
            }

            // Get and delete all projects
            Response projectsResponse = given().when().get("/projects");
            if (projectsResponse.getStatusCode() == 200) {
                List<Map<String, Object>> projects = projectsResponse.path("projects");
                if (projects != null) {
                    for (Map<String, Object> project : projects) {
                        String id = String.valueOf(project.get("id"));
                        deleteIfExists("/projects/" + id);
                    }
                }
            }
        } catch (Exception e) {
            // Log the exception but don't fail the test
            System.err.println("Warning: Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Delete that accepts any success or expected failure status codes.
     * 
     * @param path The REST path to delete (e.g., "/todos/123")
     */
    public static void deleteIfExists(String path) {
        given().when().delete(path).then().statusCode(anyOf(is(200), is(404), is(400)));
    }

    /**
     * Delete that accepts any success or expected failure status codes.
     * 
     * @param id The todo ID to delete
     */
    public static void safeDeleteTodo(String id) {
        if (id == null) return;
        given().when().delete("/todos/" + id);
    }

    /**
     * Delete that accepts any success or expected failure status codes.
     * 
     * @param id The category ID to delete
     */
    public static void safeDeleteCategory(String id) {
        if (id == null) return;
        given().when().delete("/categories/" + id);
    }

    /**
     * Delete that accepts any success or expected failure status codes.
     * 
     * @param id The project ID to delete
     */
    public static void safeDeleteProject(String id) {
        if (id == null) return;
        given().when().delete("/projects/" + id);
    }

    /**
     * Extract ID from either flat object response or array-wrapped response.
     * This handles the API's inconsistent response format where sometimes objects
     * are returned directly and sometimes wrapped in arrays.
     * 
     * @param res The response from the API call
     * @param collectionRoot The root name of the collection (e.g., "todos", "categories")
     * @return The extracted ID as a string, or null if not found
     */
    public static String extractId(Response res, String collectionRoot) {
        String id = res.path("id");
        if (id == null && collectionRoot != null) {
            Object v = res.path(collectionRoot + "[0].id");
            if (v != null) id = String.valueOf(v);
        }
        return id;
    }

    /**
     * Create a todo using JSON payload.
     * 
     * @param title The todo title
     * @param done Whether the todo is completed
     * @param description The todo description
     * @return The ID of the created todo
     */
    public static String createTodo(String title, boolean done, String description) {
        Response res = given()
            .contentType("application/json")
            .body(String.format("{\"title\":\"%s\",\"doneStatus\":%s,\"description\":\"%s\"}",
                title, done, description == null ? "" : description))
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = extractId(res, "todos");
        if (id == null) {
            Response r = given().queryParam("title", title)
                .when().get("/todos").then().statusCode(200).extract().response();
            List<Map<String, Object>> list = r.path("todos");
            if (list != null && !list.isEmpty()) {
                Object v = list.get(0).get("id");
                if (v != null) id = String.valueOf(v);
            }
        }
        return id;
    }

    /**
     * Create a category using JSON payload.
     * 
     * @param title The category title
     * @param description The category description
     * @return The ID of the created category
     */
    public static String createCategory(String title, String description) {
        Response res = given()
            .contentType("application/json")
            .body(String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                title, description == null ? "" : description))
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
                        Object v = c.get("id");
                        if (v != null) return String.valueOf(v);
                    }
                }
            }
        }
        return id;
    }

    /**
     * Create a project using JSON payload.
     * 
     * @param title The project title
     * @param description The project description
     * @return The ID of the created project
     */
    public static String createProject(String title, String description) {
        Response res = given()
            .contentType("application/json")
            .body(String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                title, description == null ? "" : description))
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
                        Object v = p.get("id");
                        if (v != null) return String.valueOf(v);
                    }
                }
            }
        }
        return id;
    }
}