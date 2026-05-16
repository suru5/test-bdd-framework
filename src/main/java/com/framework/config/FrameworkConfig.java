package com.framework.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

/**
 * Framework configuration interface.
 * Values are loaded from framework.properties, with env variable + system property overrides.
 */
@LoadPolicy(LoadType.MERGE)
@Sources({
        "system:properties",
        "system:env",
        "classpath:framework.properties"
})
public interface FrameworkConfig extends Config {

    /* ─────────────── Browser ─────────────── */
    @Key("browser")
    @DefaultValue("chrome")
    String browser();

    @Key("headless")
    @DefaultValue("false")
    boolean headless();

    @Key("browser.maximize")
    @DefaultValue("true")
    boolean browserMaximize();

    @Key("implicit.wait")
    @DefaultValue("10")
    int implicitWait();

    @Key("explicit.wait")
    @DefaultValue("20")
    int explicitWait();

    @Key("page.load.timeout")
    @DefaultValue("30")
    int pageLoadTimeout();

    @Key("keep.browser.open")
    @DefaultValue("false")
    boolean keepBrowserOpen();

    /* ─────────────── Application ─────────────── */
    @Key("base.url")
    @DefaultValue("https://www.saucedemo.com/inventory.html")
    String baseUrl();

    @Key("app.username")
    String appUsername();

    @Key("app.password")
    String appPassword();

    /* ─────────────── Environment ─────────────── */
    @Key("environment")
    @DefaultValue("qa")
    String environment();

    /* ─────────────── Reporting ─────────────── */
    @Key("screenshot.on.failure")
    @DefaultValue("true")
    boolean screenshotOnFailure();

    @Key("screenshot.on.pass")
    @DefaultValue("false")
    boolean screenshotOnPass();

    @Key("extent.report.path")
    @DefaultValue("reports/extent/ExtentReport.html")
    String extentReportPath();

    /* ─────────────── Retry ─────────────── */
    @Key("retry.count")
    @DefaultValue("1")
    int retryCount();

    /* ─────────────── Paths ─────────────── */
    @Key("locators.path")
    @DefaultValue("locators/locators.json")
    String locatorsPath();

    @Key("testdata.path")
    @DefaultValue("testdata/testdata.json")
    String testdataPath();
}
