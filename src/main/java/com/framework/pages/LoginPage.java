package com.framework.pages;

import com.framework.config.ConfigManager;
import com.framework.core.LocatorReader;

/**
 * Login Page Object for https://www.saucedemo.com
 * All locators are sourced from locators/locators.json → "LoginPage".
 */
public class LoginPage extends BasePage {

    private static final String PAGE = "LoginPage";

    /* ─────────────── Navigation ─────────────── */

    public void navigateToLogin() {
        String url = ConfigManager.get().baseUrl();
        log.info("Navigating to login page: {}", url);
        navigateTo(url);
    }

    /* ─────────────── Actions ─────────────── */

    public void enterUsername(String username) {
        log.info("Entering username: {}", username);
        type(LocatorReader.get(PAGE, "usernameField"), username);
    }

    public void enterPassword(String password) {
        log.info("Entering password");
        type(LocatorReader.get(PAGE, "passwordField"), password);
    }

    public void clickLoginButton() {
        log.info("Clicking Login button");
        click(LocatorReader.get(PAGE, "loginButton"));
    }

    public void clickLogoutButton() {
        log.info("Clicking Logout link");
        try {
            click(LocatorReader.get(PAGE, "logoutLink"));
        } catch (Exception e) {
            log.warn("Logout link not found via locator, attempting JS click: {}", e.getMessage());
            jsClick(LocatorReader.get(PAGE, "logoutLink"));
        }
    }

    /* ─────────────── State / Assertions ─────────────── */

    public boolean isLoginSuccessful() {
        try {
            // Give the page time to load after login
            Thread.sleep(2000);
            String currentUrl = getCurrentUrl();
            log.info("Current URL after login attempt: {}", currentUrl);

            // Check URL changed away from login page
            boolean urlChanged = !currentUrl.contains("/login") && !currentUrl.equals(ConfigManager.get().baseUrl() + "/login");

            log.info("URL changed from login: {}", urlChanged);
            return urlChanged;
        } catch (Exception e) {
            log.warn("Error checking login success: {}", e.getMessage());
            return false;
        }
    }

    public boolean isOnLoginPage() {
        try {
            Thread.sleep(1000);
            String currentUrl = getCurrentUrl();
            log.info("Checking if on login page. Current URL: {}", currentUrl);
            return currentUrl.contains("/login") || currentUrl.equals(ConfigManager.get().baseUrl())
                    || currentUrl.equals(ConfigManager.get().baseUrl() + "/");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(LocatorReader.get(PAGE, "errorMessage"));
    }

    public String getErrorMessage() {
        try {
            return getText(LocatorReader.get(PAGE, "errorMessage"));
        } catch (Exception e) {
            log.warn("Could not get error message text: {}", e.getMessage());
            return "";
        }
    }

    public boolean isLogoutLinkVisible() {
        return isDisplayed(LocatorReader.get(PAGE, "logoutLink"));
    }
}
