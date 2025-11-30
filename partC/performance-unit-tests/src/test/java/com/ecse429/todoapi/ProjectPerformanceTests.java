package com.ecse429.todoapi;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectPerformanceTests {

    private static final String BASE = "http://localhost";
    private static final int PORT = 4567;
    private static final int[] OBJECT_COUNTS = { 10, 25, 50, 100, 250 };
    private static final List<PerformanceTestHelper.PerformanceResult> allResults = new ArrayList<>();

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE;
        RestAssured.port = PORT;

        // Verify service is running
        given()
                .when().get("/projects")
                .then().statusCode(anyOf(is(200), is(204)));

        System.out.println("\n========================================");
        System.out.println("Starting Project Performance Tests");
        System.out.println("========================================\n");
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        PerformanceTestHelper.cleanupAllData();
    }

    @AfterAll
    static void writeResults() {
        // Write all results to CSV
        PerformanceTestHelper.writeResultsToCSV("project_performance_results.csv", allResults);

        System.out.println("\n========================================");
        System.out.println("Project Performance Tests Complete");
        System.out.println("========================================\n");
    }

    @Test
    @Order(1)
    void testCreateProjectPerformance() {
        System.out.println("\n--- Testing Project CREATE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "ProjectCreate", count, "CREATE");

            System.out.println("Creating " + count + " projects...");

            // Warmup
            String warmupId = PerformanceTestHelper.createRandomProject();
            PerformanceTestHelper.safeDeleteProject(warmupId);

            // Measure individual create operations
            List<String> createdIds = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                long timing = PerformanceTestHelper.measureOperation(() -> {
                    String id = PerformanceTestHelper.createRandomProject();
                    createdIds.add(id);
                });
                result.addTiming(timing);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            for (String id : createdIds) {
                PerformanceTestHelper.safeDeleteProject(id);
            }
        }
    }

    @Test
    @Order(2)
    void testUpdateProjectPerformance() {
        System.out.println("\n--- Testing Project UPDATE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "ProjectUpdate", count, "UPDATE");

            System.out.println("Updating " + count + " projects...");

            // Pre-populate with projects
            List<String> projectIds = PerformanceTestHelper.createRandomProjects(count);

            // Warmup
            given()
                    .contentType("application/json")
                    .body("{\"title\":\"Warmup\",\"description\":\"Warmup\"}")
                    .when().post("/projects/" + projectIds.get(0))
                    .then().statusCode(200);

            // Measure update operations
            for (String id : projectIds) {
                long timing = PerformanceTestHelper.measureOperation(() -> {
                    String newTitle = "Updated-" + PerformanceTestHelper.generateRandomString(10);
                    String newDesc = "UpdatedDesc-" + PerformanceTestHelper.generateRandomString(15);

                    given()
                            .contentType("application/json")
                            .body(String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                                    newTitle, newDesc))
                            .when().post("/projects/" + id)
                            .then().statusCode(200);
                });
                result.addTiming(timing);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            for (String id : projectIds) {
                PerformanceTestHelper.safeDeleteProject(id);
            }
        }
    }

    @Test
    @Order(3)
    void testDeleteProjectPerformance() {
        System.out.println("\n--- Testing Project DELETE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "ProjectDelete", count, "DELETE");

            System.out.println("Deleting " + count + " projects...");

            // Pre-populate with projects
            List<String> projectIds = PerformanceTestHelper.createRandomProjects(count);

            // Warmup
            String warmupId = PerformanceTestHelper.createRandomProject();
            given().when().delete("/projects/" + warmupId).then().statusCode(200);

            // Measure delete operations
            for (String id : projectIds) {
                long timing = PerformanceTestHelper.measureOperation(() -> {
                    given()
                            .when().delete("/projects/" + id)
                            .then().statusCode(200);
                });
                result.addTiming(timing);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);
        }
    }
}
