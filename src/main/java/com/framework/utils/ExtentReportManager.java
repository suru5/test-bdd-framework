package com.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages Extent Reports lifecycle.
 * Uses ThreadLocal to support parallel test execution safely.
 */
public final class ExtentReportManager {

    private static final Logger log = LogManager.getLogger(ExtentReportManager.class);
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();

    private ExtentReportManager() {}

    /**
     * Initialises and returns the singleton ExtentReports instance.
     */
    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String reportPath = ConfigManager.get().extentReportPath();
            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("BDD Test Report – saucedemo Login");
            spark.config().setReportName("saucedemo Login Automation Report");
            spark.config().setEncoding("UTF-8");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Application", "saucedemo");
            extent.setSystemInfo("Browser", ConfigManager.get().browser());
            extent.setSystemInfo("Environment", ConfigManager.get().environment());
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));

            log.info("Extent Report initialised at: {}", reportPath);
        }
        return extent;
    }

    /**
     * Creates a new test node and binds it to the current thread.
     */
    public static void createTest(String testName, String description) {
        ExtentTest test = getInstance().createTest(testName, description);
        testThreadLocal.set(test);
    }

    /**
     * Returns the ExtentTest for the current thread, or null if not set.
     */
    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }

    /**
     * Removes the test binding from the current thread.
     */
    public static void removeTest() {
        testThreadLocal.remove();
    }

    /**
     * Flushes all reports to disk. Call once at suite teardown.
     */
    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
            log.info("Extent Report flushed.");
        }
    }
}
