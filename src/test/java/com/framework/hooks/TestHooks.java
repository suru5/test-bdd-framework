package com.framework.hooks;

import com.framework.config.ConfigManager;
import com.framework.core.DriverManager;
import com.framework.utils.ExtentReportManager;
import com.framework.utils.ScreenshotUtil;
import io.cucumber.java.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Cucumber lifecycle hooks.
 *
 * Order of execution:
 *   @BeforeAll → @Before → [@BeforeStep → step → @AfterStep] → @After → @AfterAll
 */
public class TestHooks {

    private static final Logger log = LogManager.getLogger(TestHooks.class);

    /* ─────────────── Suite Level ─────────────── */

    @BeforeAll
    public static void suiteSetup() {
        log.info("========================================");
        log.info("  TEST SUITE STARTING");
        log.info("  Environment : {}", ConfigManager.get().environment());
        log.info("  Browser     : {}", ConfigManager.get().browser());
        log.info("  Base URL    : {}", ConfigManager.get().baseUrl());
        log.info("========================================");
        ExtentReportManager.getInstance(); // initialise once
    }

    @AfterAll
    public static void suiteTeardown() {
        log.info("========================================");
        log.info("  TEST SUITE COMPLETED");
        log.info("========================================");
        ExtentReportManager.flush();
    }

    /* ─────────────── Scenario Level ─────────────── */

    @Before(order = 1)
    public void scenarioSetup(Scenario scenario) {
        log.info("──────────────────────────────────────────");
        log.info("▶ SCENARIO: {}", scenario.getName());
        log.info("  Tags: {}", scenario.getSourceTagNames());
        log.info("──────────────────────────────────────────");

        DriverManager.initDriver();
        // NOTE: Navigation to the login page is handled by the step definition
        // "Given the user navigates to the saucedemo login page"

        ExtentReportManager.createTest(scenario.getName(),
                "Tags: " + scenario.getSourceTagNames());
    }

    @After(order = 1)
    public void scenarioTeardown(Scenario scenario) {
        log.info("◀ SCENARIO [{}]: {}", scenario.getStatus(), scenario.getName());

        try {
            if (scenario.isFailed()) {
                handleFailure(scenario);
                if (ExtentReportManager.getTest() != null) {
                    ExtentReportManager.getTest().fail("Scenario FAILED: " + scenario.getName());
                }
            } else {
                captureAndAttach(scenario, "Final state - PASSED");
                if (ExtentReportManager.getTest() != null) {
                    ExtentReportManager.getTest().pass("Scenario PASSED: " + scenario.getName());
                }
            }
        } catch (Exception e) {
            log.warn("Error in teardown hook: {}", e.getMessage());
        } finally {
            // ── Only quit if keep.browser.open=false ──
            if (!ConfigManager.get().keepBrowserOpen()) {
                DriverManager.quitDriver();
                log.info("Browser closed after scenario.");
            } else {
                log.info("Browser kept open (keep.browser.open=true).");
            }
            ExtentReportManager.removeTest();
        }
    }

    /* ─────────────── Step Level ─────────────── */

    @AfterStep
    public void afterStep(Scenario scenario) {
        try {
            DriverManager.getDriver();
        } catch (IllegalStateException e) {
            return;
        }
        try {
            String stepLabel = "Step screenshot - " + scenario.getName();

            byte[] bytes = ScreenshotUtil.captureAsBytes();
            if (bytes.length > 0) {
                scenario.attach(bytes, "image/png", stepLabel);
            }

            if (ExtentReportManager.getTest() != null) {
                String base64 = ScreenshotUtil.captureAsBase64();
                if (!base64.isEmpty()) {
                    ExtentReportManager.getTest()
                            .info("Step completed",
                                    com.aventstack.extentreports.MediaEntityBuilder
                                            .createScreenCaptureFromBase64String(base64)
                                            .build());
                }
            }

            ScreenshotUtil.attachToAllure(stepLabel);

        } catch (Exception e) {
            log.warn("AfterStep screenshot failed: {}", e.getMessage());
        }
    }

    /* ─────────────── Private Helpers ─────────────── */

    private void handleFailure(Scenario scenario) {
        log.error("SCENARIO FAILED: {}", scenario.getName());
        captureAndAttach(scenario, "FAILURE - " + sanitize(scenario.getName()));

        String path = ScreenshotUtil.captureOnFailure(sanitize(scenario.getName()));
        if (!path.isEmpty()) {
            log.info("Failure screenshot saved: {}", path);
        }
    }

    private void captureAndAttach(Scenario scenario, String label) {
        byte[] screenshot = ScreenshotUtil.captureAsBytes();
        if (screenshot.length > 0) {
            scenario.attach(screenshot, "image/png", label);
        }
        ScreenshotUtil.attachToAllure(label);
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
