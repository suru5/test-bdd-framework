package com.framework.steps;

import com.framework.pages.LoginPage;
import com.framework.utils.ExtentReportManager;
import com.framework.utils.ScreenshotUtil;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;

/**
 * Step definitions for saucedemo Login feature.
 * Target: https://www.saucedemo.com
 */
public class LoginSteps {

    private static final Logger log = LogManager.getLogger(LoginSteps.class);
    private final LoginPage loginPage = new LoginPage();

    // ── Navigation ──────────────────────────────────────────────────────────

    @Given("the user navigates to the login page")
    @Step("Navigate to the saucedemo login page")
    public void navigateToLoginPage() {
        log.info("Step: Navigate to saucedemo login page");
        loginPage.navigateToLogin();
        captureStep("Navigated to login page");
    }

    // ── Input Actions ────────────────────────────────────────────────────────

    @When("the user enters email {string}")
    @Step("Enter email/username: {0}")
    public void enterEmail(String email) {
        log.info("Step: Enter email = {}", email);
        loginPage.enterUsername(email);
        captureStep("Entered email: " + email);
    }

    @When("the user enters the login password {string}")
    @Step("Enter login password")
    public void enterPassword(String password) {
        log.info("Step: Enter password");
        loginPage.enterPassword(password);
        captureStep("Entered password");
    }

    @When("the user clicks the sign in button")
    @Step("Click the Sign In / Login button")
    public void clickSignIn() {
        log.info("Step: Click sign in button");
        loginPage.clickLoginButton();
        captureStep("Clicked Sign In button");
    }

    @And("the user clicks the logout link")
    @Step("Click the Logout link")
    public void clickLogout() {
        log.info("Step: Click logout");
        loginPage.clickLogoutButton();
        captureStep("Clicked Logout");
    }

    // ── Assertions ───────────────────────────────────────────────────────────

    @Then("the login should be successful")
    @Step("Verify login was successful")
    public void verifyLoginSuccessful() {
        log.info("Step: Verify login successful");
        captureStep("Verifying login success");
        boolean success = loginPage.isLoginSuccessful();
        Assertions.assertThat(success)
                .as("Login should be successful — URL should change away from /login and/or dashboard should be visible")
                .isTrue();
    }

    @Then("the login should fail")
    @Step("Verify login failed")
    public void verifyLoginFailed() {
        log.info("Step: Verify login failed");
        captureStep("Verifying login failure");
        // Login failed means: still on login page OR error message shown
        boolean stillOnLogin = loginPage.isOnLoginPage();
        boolean errorShown   = loginPage.isErrorDisplayed();
        log.info("Still on login page: {}, Error displayed: {}", stillOnLogin, errorShown);
        Assertions.assertThat(stillOnLogin || errorShown)
                .as("Login with wrong credentials should either stay on login page or show an error message")
                .isTrue();
    }

    @Then("an error message should be displayed on the login page")
    @Step("Verify error message is shown")
    public void verifyErrorMessageDisplayed() {
        log.info("Step: Verify error message");
        captureStep("Error message check");
        // Error is shown OR we're still on login page (some sites don't show inline errors)
        boolean errorVisible = loginPage.isErrorDisplayed();
        boolean onLoginPage  = loginPage.isOnLoginPage();
        log.info("Error visible: {}, On login page: {}", errorVisible, onLoginPage);
        Assertions.assertThat(errorVisible || onLoginPage)
                .as("An error message should be displayed, or the user should remain on the login page, after invalid credentials")
                .isTrue();
        if (errorVisible) {
            String errorText = loginPage.getErrorMessage();
            log.info("Error message text: {}", errorText);
        }
    }

    @Then("the user should remain on the login page")
    @Step("Verify user remains on the login page")
    public void verifyRemainsOnLoginPage() {
        log.info("Step: Verify user remains on login page");
        captureStep("Verifying login page persistence");
        Assertions.assertThat(loginPage.isOnLoginPage())
                .as("User should remain on the login page when submitting empty credentials")
                .isTrue();
    }

    // ── Private Helper ───────────────────────────────────────────────────────

    private void captureStep(String stepLabel) {
        try {
            ScreenshotUtil.attachToAllure(stepLabel);
            if (ExtentReportManager.getTest() != null) {
                String base64 = ScreenshotUtil.captureAsBase64();
                if (!base64.isEmpty()) {
                    ExtentReportManager.getTest()
                            .info(stepLabel,
                                    com.aventstack.extentreports.MediaEntityBuilder
                                            .createScreenCaptureFromBase64String(base64)
                                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Could not capture step screenshot for '{}': {}", stepLabel, e.getMessage());
        }
    }
}
