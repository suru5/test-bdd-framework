package com.framework.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads element locators (XPath, CSS, ID, etc.) from a JSON file.
 *
 * JSON format:
 * {
 *   "LoginPage": {
 *     "usernameField":  { "type": "xpath", "value": "//input[@id='username']" },
 *     "passwordField":  { "type": "id",    "value": "password" },
 *     "loginButton":    { "type": "css",   "value": ".btn-login" }
 *   }
 * }
 *
 * Usage: LocatorReader.get("LoginPage", "usernameField")
 */
public final class LocatorReader {

    private static final Logger log = LogManager.getLogger(LocatorReader.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode rootNode;

    static {
        loadLocators();
    }

    private LocatorReader() {}

    /* ─────────────── Load ─────────────── */

    private static void loadLocators() {
        String path = ConfigManager.get().locatorsPath();
        log.info("Loading locators from: {}", path);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Locators file not found on classpath: " + path);
            rootNode = mapper.readTree(is);
            log.info("Locators loaded successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load locators: " + e.getMessage(), e);
        }
    }

    /* ─────────────── Public API ─────────────── */

    /**
     * Returns a Selenium {@link By} object for the given page and element key.
     *
     * @param pageName    Top-level JSON key (e.g. "LoginPage")
     * @param elementKey  Element JSON key (e.g. "usernameField")
     */
    public static By get(String pageName, String elementKey) {
        JsonNode pageNode = rootNode.get(pageName);
        if (pageNode == null) throw new RuntimeException("Page not found in locators: " + pageName);

        JsonNode elementNode = pageNode.get(elementKey);
        if (elementNode == null) throw new RuntimeException("Element not found: " + pageName + "." + elementKey);

        String type  = elementNode.get("type").asText().toLowerCase().trim();
        String value = elementNode.get("value").asText();

        log.debug("Resolved locator [{}.{}] => type={}, value={}", pageName, elementKey, type, value);

        return switch (type) {
            case "xpath"        -> By.xpath(value);
            case "id"           -> By.id(value);
            case "css", "cssselector" -> By.cssSelector(value);
            case "name"         -> By.name(value);
            case "classname", "class" -> By.className(value);
            case "linktext"     -> By.linkText(value);
            case "partiallinktext" -> By.partialLinkText(value);
            case "tagname"      -> By.tagName(value);
            default -> throw new RuntimeException("Unsupported locator type: " + type);
        };
    }

    /**
     * Returns the raw string value of a locator (e.g. the xpath expression itself).
     */
    public static String getRaw(String pageName, String elementKey) {
        JsonNode pageNode = rootNode.get(pageName);
        if (pageNode == null) throw new RuntimeException("Page not found: " + pageName);
        JsonNode elementNode = pageNode.get(elementKey);
        if (elementNode == null) throw new RuntimeException("Element not found: " + pageName + "." + elementKey);
        return elementNode.get("value").asText();
    }

    /**
     * Returns all locators for a given page as a Map<elementKey, By>.
     */
    public static Map<String, By> getAllForPage(String pageName) {
        JsonNode pageNode = rootNode.get(pageName);
        if (pageNode == null) throw new RuntimeException("Page not found: " + pageName);

        Map<String, By> locators = new HashMap<>();
        pageNode.fieldNames().forEachRemaining(key -> locators.put(key, get(pageName, key)));
        return locators;
    }

    /** Reload locators at runtime (e.g. after test env switch) */
    public static void reload() {
        loadLocators();
    }
}
