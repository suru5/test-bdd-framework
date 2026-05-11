package com.framework.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads test data (field values, dropdown options, expected values) from a JSON file.
 *
 * JSON format:
 * {
 *   "Login": {
 *     "validUser": {
 *       "username": "admin@example.com",
 *       "password": "Admin@123",
 *       "role": "Admin"
 *     },
 *     "invalidUser": {
 *       "username": "wrong@user.com",
 *       "password": "wrongPass"
 *     }
 *   },
 *   "Registration": {
 *     "newUser": {
 *       "firstName": "John",
 *       "country": "India",
 *       "gender": "Male"
 *     }
 *   }
 * }
 *
 * Usage:
 *   TestDataReader.get("Login", "validUser", "username")    => "admin@example.com"
 *   TestDataReader.getMap("Login", "validUser")             => { username -> ..., password -> ... }
 *   TestDataReader.getList("Registration", "countryList")   => [ "India", "US", ... ]
 */
public final class TestDataReader {

    private static final Logger log = LogManager.getLogger(TestDataReader.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode rootNode;

    static {
        loadTestData();
    }

    private TestDataReader() {}

    /* ─────────────── Load ─────────────── */

    private static void loadTestData() {
        String path = ConfigManager.get().testdataPath();
        log.info("Loading test data from: {}", path);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Test data file not found on classpath: " + path);
            rootNode = mapper.readTree(is);
            log.info("Test data loaded successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data: " + e.getMessage(), e);
        }
    }

    /* ─────────────── Public API ─────────────── */

    /**
     * Gets a single string value: section → dataset → field.
     * e.g. get("Login", "validUser", "username") => "admin@example.com"
     */
    public static String get(String section, String dataset, String field) {
        JsonNode node = navigate(section, dataset);
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null) throw new RuntimeException(
                "Field not found in testdata: " + section + "." + dataset + "." + field);
        String value = fieldNode.asText();
        log.debug("TestData [{}.{}.{}] => {}", section, dataset, field, value);
        return value;
    }

    /**
     * Gets a flat string value directly under section: section → field.
     * e.g. get("Urls", "loginPage") => "/login"
     */
    public static String get(String section, String field) {
        JsonNode sectionNode = rootNode.get(section);
        if (sectionNode == null) throw new RuntimeException("Section not found: " + section);
        JsonNode fieldNode = sectionNode.get(field);
        if (fieldNode == null) throw new RuntimeException("Field not found: " + section + "." + field);
        return fieldNode.asText();
    }

    /**
     * Gets all key-value pairs for a dataset as a Map<String, String>.
     * e.g. getMap("Login", "validUser") => { "username": "...", "password": "..." }
     */
    public static Map<String, String> getMap(String section, String dataset) {
        JsonNode node = navigate(section, dataset);
        Map<String, String> result = new HashMap<>();
        node.fieldNames().forEachRemaining(key -> result.put(key, node.get(key).asText()));
        return result;
    }

    /**
     * Gets a JSON array as a List<String>.
     * e.g. getList("Registration", "countryOptions") => ["India", "US", "UK"]
     */
    public static List<String> getList(String section, String field) {
        JsonNode sectionNode = rootNode.get(section);
        if (sectionNode == null) throw new RuntimeException("Section not found: " + section);
        JsonNode arrayNode = sectionNode.get(field);
        if (arrayNode == null || !arrayNode.isArray())
            throw new RuntimeException("Array field not found: " + section + "." + field);
        List<String> list = new ArrayList<>();
        arrayNode.forEach(n -> list.add(n.asText()));
        return list;
    }

    /**
     * Checks whether a dataset exists in a section (useful for conditional steps).
     */
    public static boolean exists(String section, String dataset) {
        JsonNode sectionNode = rootNode.get(section);
        return sectionNode != null && sectionNode.has(dataset);
    }

    /** Reload test data at runtime */
    public static void reload() {
        loadTestData();
    }

    /* ─────────────── Private Helpers ─────────────── */

    private static JsonNode navigate(String section, String dataset) {
        JsonNode sectionNode = rootNode.get(section);
        if (sectionNode == null) throw new RuntimeException("Section not found in testdata: " + section);
        JsonNode datasetNode = sectionNode.get(dataset);
        if (datasetNode == null) throw new RuntimeException("Dataset not found: " + section + "." + dataset);
        return datasetNode;
    }
}
