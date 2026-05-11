package com.framework.core;

import com.framework.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;

import java.time.Duration;
// import java.util.List;

/**
 * Thread-safe WebDriver manager using ThreadLocal.
 * Supports Chrome, Firefox, Edge, Safari with optional headless mode.
 */
public final class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    private DriverManager() {}

    /* ─────────────── Initialise ─────────────── */

    public static void initDriver() {
        initDriver(ConfigManager.get().browser());
    }

    public static void initDriver(String browserName) {
        boolean headless = ConfigManager.get().headless();
        log.info("Initialising [{}] driver | headless={}", browserName, headless);

        WebDriver driver = switch (browserName.trim().toLowerCase()) {
            case "chrome"   -> createChromeDriver(headless);
            case "firefox"  -> createFirefoxDriver(headless);
            case "edge"     -> createEdgeDriver(headless);
            case "safari"   -> createSafariDriver();
            default         -> throw new IllegalArgumentException("Unsupported browser: " + browserName);
        };

        applyTimeouts(driver);

        if (ConfigManager.get().browserMaximize()) {
            driver.manage().window().maximize();
        }

        driverThreadLocal.set(driver);
        log.info("Driver initialised successfully. Thread: {}", Thread.currentThread().getName());
    }

    /* ─────────────── Accessor ─────────────── */

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialised for thread: " + Thread.currentThread().getName());
        }
        return driver;
    }

    /* ─────────────── Teardown ─────────────── */

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("Driver quit successfully.");
            } catch (Exception e) {
                log.warn("Error quitting driver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    /* ─────────────── Browser Factories ─────────────── */

    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-extensions",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--remote-allow-origins=*"
        );
        if (headless) options.addArguments("--headless=new");
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) options.addArguments("-headless");
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (headless) options.addArguments("--headless=new");
        return new EdgeDriver(options);
    }

    private static WebDriver createSafariDriver() {
        return new SafariDriver();
    }

    /* ─────────────── Helpers ─────────────── */

    private static void applyTimeouts(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.get().implicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.get().pageLoadTimeout()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
    }
}
