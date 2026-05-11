package com.framework.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Main Cucumber TestNG Runner for saucedemo Login Tests.
 *
 * Run with Maven:
 *   mvn test                            (runs all @smoke and @regression scenarios)
 *   mvn test -Dcucumber.filter.tags="@smoke"
 *   mvn test -Dcucumber.filter.tags="@regression"
 *   mvn test -P smoke
 *   mvn test -P regression
 */
@CucumberOptions(
        features  = "src/test/resources/features/Login.feature",
        glue      = {
                "com.framework.hooks",
                "com.framework.steps"
        },
        tags      = "@smoke",
        plugin    = {
                "pretty",
                "html:reports/cucumber/cucumber-report.html",
                "json:reports/cucumber/cucumber-report.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true,
        publish    = false,
        dryRun     = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
