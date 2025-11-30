package com.ecse429.todoapi;

import io.restassured.response.Response;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class PerformanceTestHelper {

    private static final String RESULTS_DIR = "target/performance-results";
    private static final Random random = new Random();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static class PerformanceResult {
        public String testName;
        public int objectCount;
        public String operationType;
        public List<Long> timingsNanos;
        public long totalTimeNanos;

        public PerformanceResult(String testName, int objectCount, String operationType) {
            this.testName = testName;
            this.objectCount = objectCount;
            this.operationType = operationType;
            this.timingsNanos = new ArrayList<>();
            this.totalTimeNanos = 0;
        }

        public void addTiming(long nanos) {
            timingsNanos.add(nanos);
            totalTimeNanos += nanos;
        }

        public double getAverageMs() {
            if (timingsNanos.isEmpty())
                return 0;
            return totalTimeNanos / 1_000_000.0 / timingsNanos.size();
        }

        public double getMinMs() {
            if (timingsNanos.isEmpty())
                return 0;
            return Collections.min(timingsNanos) / 1_000_000.0;
        }

        public double getMaxMs() {
            if (timingsNanos.isEmpty())
                return 0;
            return Collections.max(timingsNanos) / 1_000_000.0;
        }

        public double getTotalMs() {
            return totalTimeNanos / 1_000_000.0;
        }

        public double getMedianMs() {
            if (timingsNanos.isEmpty())
                return 0;
            List<Long> sorted = new ArrayList<>(timingsNanos);
            Collections.sort(sorted);
            int middle = sorted.size() / 2;
            if (sorted.size() % 2 == 0) {
                return (sorted.get(middle - 1) + sorted.get(middle)) / 2_000_000.0;
            } else {
                return sorted.get(middle) / 1_000_000.0;
            }
        }
    }

    public static void cleanupAllData() {
        try {
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
            System.err.println("Warning: Error during cleanup: " + e.getMessage());
        }
    }

    public static void deleteIfExists(String path) {
        given().when().delete(path).then().statusCode(anyOf(is(200), is(404), is(400)));
    }

    public static void safeDeleteTodo(String id) {
        if (id == null)
            return;
        given().when().delete("/todos/" + id);
    }

    public static void safeDeleteCategory(String id) {
        if (id == null)
            return;
        given().when().delete("/categories/" + id);
    }

    public static void safeDeleteProject(String id) {
        if (id == null)
            return;
        given().when().delete("/projects/" + id);
    }

    public static String extractId(Response res, String collectionRoot) {
        String id = res.path("id");
        if (id == null && collectionRoot != null) {
            Object v = res.path(collectionRoot + "[0].id");
            if (v != null)
                id = String.valueOf(v);
        }
        return id;
    }

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
                if (v != null)
                    id = String.valueOf(v);
            }
        }
        return id;
    }

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
                        if (v != null)
                            return String.valueOf(v);
                    }
                }
            }
        }
        return id;
    }

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
                        if (v != null)
                            return String.valueOf(v);
                    }
                }
            }
        }
        return id;
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static boolean generateRandomBoolean() {
        return random.nextBoolean();
    }

    public static int generateRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static String createRandomTodo() {
        String title = "Todo-" + generateRandomString(10);
        String description = "Description-" + generateRandomString(20);
        boolean doneStatus = generateRandomBoolean();

        Response res = given()
                .contentType("application/json")
                .body(String.format("{\"title\":\"%s\",\"doneStatus\":%s,\"description\":\"%s\"}",
                        title, doneStatus, description))
                .when().post("/todos")
                .then().statusCode(anyOf(is(200), is(201)))
                .extract().response();

        return extractId(res, "todos");
    }

    public static String createRandomCategory() {
        String title = "Category-" + generateRandomString(10);
        String description = "Description-" + generateRandomString(20);

        Response res = given()
                .contentType("application/json")
                .body(String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                        title, description))
                .when().post("/categories")
                .then().statusCode(anyOf(is(200), is(201)))
                .extract().response();

        return extractId(res, "categories");
    }

    public static String createRandomProject() {
        String title = "Project-" + generateRandomString(10);
        String description = "Description-" + generateRandomString(20);

        Response res = given()
                .contentType("application/json")
                .body(String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                        title, description))
                .when().post("/projects")
                .then().statusCode(anyOf(is(200), is(201)))
                .extract().response();

        return extractId(res, "projects");
    }

    public static List<String> createRandomTodos(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(createRandomTodo());
        }
        return ids;
    }

    public static List<String> createRandomCategories(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(createRandomCategory());
        }
        return ids;
    }

    public static List<String> createRandomProjects(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(createRandomProject());
        }
        return ids;
    }

    public static void initializeResultsDirectory() {
        try {
            Path path = Paths.get(RESULTS_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to create results directory: " + e.getMessage());
        }
    }

    public static void writeResultsToCSV(String filename, List<PerformanceResult> results) {
        initializeResultsDirectory();

        String filepath = RESULTS_DIR + "/" + filename;
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.append(
                    "TestName,ObjectCount,OperationType,AverageTimeMs,MinTimeMs,MaxTimeMs,MedianTimeMs,TotalTimeMs,SampleCount,Timestamp\n");

            for (PerformanceResult result : results) {
                writer.append(String.format("%s,%d,%s,%.3f,%.3f,%.3f,%.3f,%.3f,%d,%s\n",
                        result.testName,
                        result.objectCount,
                        result.operationType,
                        result.getAverageMs(),
                        result.getMinMs(),
                        result.getMaxMs(),
                        result.getMedianMs(),
                        result.getTotalMs(),
                        result.timingsNanos.size(),
                        new Date().toString()));
            }

            System.out.println("Performance results written to: " + filepath);
        } catch (IOException e) {
            System.err.println("Failed to write results to CSV: " + e.getMessage());
        }
    }

    public static void printResults(PerformanceResult result) {
        System.out.println("\n=== Performance Results ===");
        System.out.println("Test: " + result.testName);
        System.out.println("Object Count: " + result.objectCount);
        System.out.println("Operation: " + result.operationType);
        System.out.println(String.format("Average Time: %.3f ms", result.getAverageMs()));
        System.out.println(String.format("Min Time: %.3f ms", result.getMinMs()));
        System.out.println(String.format("Max Time: %.3f ms", result.getMaxMs()));
        System.out.println(String.format("Median Time: %.3f ms", result.getMedianMs()));
        System.out.println(String.format("Total Time: %.3f ms", result.getTotalMs()));
        System.out.println("Sample Count: " + result.timingsNanos.size());
        System.out.println("==========================\n");
    }

    public static long measureOperation(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    public static void createTodoCategoryRelationship(String todoId, String categoryId) {
        given()
                .contentType("application/json")
                .body(String.format("{\"id\":\"%s\"}", categoryId))
                .when().post("/todos/" + todoId + "/categories")
                .then().statusCode(anyOf(is(200), is(201)));
    }

    public static void createTodoProjectRelationship(String todoId, String projectId) {
        given()
                .contentType("application/json")
                .body(String.format("{\"id\":\"%s\"}", projectId))
                .when().post("/todos/" + todoId + "/tasksof")
                .then().statusCode(anyOf(is(200), is(201)));
    }
}
