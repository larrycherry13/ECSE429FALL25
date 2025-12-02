package com.ecse429.todoapi;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryPerformanceTests {

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
                .when().get("/categories")
                .then().statusCode(anyOf(is(200), is(204)));

        System.out.println("\n========================================");
        System.out.println("Starting Category Performance Tests");
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
        PerformanceTestHelper.writeResultsToCSV("category_performance_results.csv", allResults);

        System.out.println("Category Performance Tests Complete");
    }

    @Test
    @Order(1)
    void testCreateCategoryPerformance() {
        System.out.println("\n--- Testing Category CREATE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "CategoryCreate", count, "CREATE");

            System.out.println("Creating " + count + " categories...");

            String warmupId = PerformanceTestHelper.createRandomCategory();
            PerformanceTestHelper.safeDeleteCategory(warmupId);

            List<String> createdIds = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                PerformanceTestHelper.OperationMetrics metrics = PerformanceTestHelper.measureOperationWithMetrics(() -> {
                    String id = PerformanceTestHelper.createRandomCategory();
                    createdIds.add(id);
                });
                result.addTiming(metrics.durationNanos, metrics.cpuPercent, metrics.memoryMB);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            for (String id : createdIds) {
                PerformanceTestHelper.safeDeleteCategory(id);
            }
        }
    }

    @Test
    @Order(2)
    void testUpdateCategoryPerformance() {
        System.out.println("\n--- Testing Category UPDATE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "CategoryUpdate", count, "UPDATE");

            System.out.println("Updating " + count + " categories...");

            // Pre-populate with categories
            List<String> categoryIds = PerformanceTestHelper.createRandomCategories(count);

            // Warmup
            given()
                    .contentType("application/json")
                    .body("{\"title\":\"Warmup\",\"description\":\"Warmup\"}")
                    .when().post("/categories/" + categoryIds.get(0))
                    .then().statusCode(200);

            // Measure update operations
            for (String id : categoryIds) {
                PerformanceTestHelper.OperationMetrics metrics = PerformanceTestHelper.measureOperationWithMetrics(() -> {
                    String newTitle = "Updated-" + PerformanceTestHelper.generateRandomString(10);
                    String newDesc = "UpdatedDesc-" + PerformanceTestHelper.generateRandomString(15);

                    given()
                            .contentType("application/json")
                            .body(String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                                    newTitle, newDesc))
                            .when().post("/categories/" + id)
                            .then().statusCode(200);
                });
                result.addTiming(metrics.durationNanos, metrics.cpuPercent, metrics.memoryMB);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            for (String id : categoryIds) {
                PerformanceTestHelper.safeDeleteCategory(id);
            }
        }
    }

    @Test
    @Order(3)
    void testDeleteCategoryPerformance() {
        System.out.println("\n--- Testing Category DELETE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "CategoryDelete", count, "DELETE");

            System.out.println("Deleting " + count + " categories...");

            // Pre-populate with categories
            List<String> categoryIds = PerformanceTestHelper.createRandomCategories(count);

            // Warmup
            String warmupId = PerformanceTestHelper.createRandomCategory();
            given().when().delete("/categories/" + warmupId).then().statusCode(200);

            // Measure delete operations
            for (String id : categoryIds) {
                PerformanceTestHelper.OperationMetrics metrics = PerformanceTestHelper.measureOperationWithMetrics(() -> {
                    given()
                            .when().delete("/categories/" + id)
                            .then().statusCode(200);
                });
                result.addTiming(metrics.durationNanos, metrics.cpuPercent, metrics.memoryMB);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);
        }
    }
}
