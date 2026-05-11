package com.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener for logging test lifecycle events.
 * Referenced in testng-login.xml as a suite-level listener.
 */
public class TestNGListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestNGListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ TEST STARTED: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✅ TEST PASSED: {} ({}ms)", result.getName(),
                result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("❌ TEST FAILED: {}", result.getName());
        if (result.getThrowable() != null) {
            log.error("   Cause: {}", result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⚠ TEST SKIPPED: {}", result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("══════════════════════════════════════════");
        log.info("  SUITE: {}", context.getName());
        log.info("══════════════════════════════════════════");
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("══════════════════════════════════════════");
        log.info("  SUITE FINISHED: {}", context.getName());
        log.info("  Passed : {}", context.getPassedTests().size());
        log.info("  Failed : {}", context.getFailedTests().size());
        log.info("  Skipped: {}", context.getSkippedTests().size());
        log.info("══════════════════════════════════════════");
    }
}
