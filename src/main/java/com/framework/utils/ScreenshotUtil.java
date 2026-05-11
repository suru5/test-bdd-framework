package com.framework.utils;

import com.framework.core.DriverManager;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Utility class for capturing and attaching screenshots to reports.
 */
public final class ScreenshotUtil {

    private static final Logger log = LogManager.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_DIR = "reports/screenshots/";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtil() {}

    /**
     * Captures screenshot as a byte array (for embedding in Cucumber reports).
     */
    public static byte[] captureAsBytes() {
        try {
            WebDriver driver = DriverManager.getDriver();
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.warn("Could not capture screenshot as bytes: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Captures screenshot as a Base64 string (for Extent reports).
     */
    public static String captureAsBase64() {
        try {
            WebDriver driver = DriverManager.getDriver();
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            log.warn("Could not capture screenshot as base64: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Attaches a screenshot to the Allure report with the given label.
     */
    public static void attachToAllure(String label) {
        try {
            byte[] bytes = captureAsBytes();
            if (bytes.length > 0) {
                Allure.addAttachment(label, new ByteArrayInputStream(bytes));
            }
        } catch (Exception e) {
            log.warn("Could not attach screenshot to Allure for '{}': {}", label, e.getMessage());
        }
    }

    /**
     * Captures and saves screenshot to disk on failure.
     * Returns the saved file path, or empty string on error.
     */
    public static String captureOnFailure(String scenarioName) {
        try {
            WebDriver driver = DriverManager.getDriver();
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            String timestamp = LocalDateTime.now().format(FORMATTER);
            String fileName = scenarioName + "_" + timestamp + ".png";
            Path destDir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(destDir);

            Path destPath = destDir.resolve(fileName);
            Files.copy(src.toPath(), destPath);

            log.info("Screenshot saved: {}", destPath.toAbsolutePath());
            return destPath.toAbsolutePath().toString();
        } catch (IOException e) {
            log.warn("Could not save failure screenshot: {}", e.getMessage());
            return "";
        } catch (Exception e) {
            log.warn("Screenshot capture error: {}", e.getMessage());
            return "";
        }
    }
}
