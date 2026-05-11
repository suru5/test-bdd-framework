package com.framework.pages;

import com.framework.config.ConfigManager;
import com.framework.core.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for all Page Objects.
 * Provides fluent, waited Selenium actions with full Log4j logging.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(this.getClass());

    /* ─────────────── Driver/Wait ─────────────── */

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }

    protected WebDriverWait getWait() {
        return new WebDriverWait(driver(), Duration.ofSeconds(ConfigManager.get().explicitWait()));
    }

    protected WebDriverWait getWait(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    /* ─────────────── Navigation ─────────────── */

    public void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        driver().get(url);
    }

    public String getCurrentUrl() {
        return driver().getCurrentUrl();
    }

    public String getTitle() {
        return driver().getTitle();
    }

    /* ─────────────── Find Elements ─────────────── */

    protected WebElement find(By locator) {
        log.debug("Finding element: {}", locator);
        return getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement findVisible(By locator) {
        log.debug("Finding visible element: {}", locator);
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement findClickable(By locator) {
        log.debug("Finding clickable element: {}", locator);
        return getWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> findAll(By locator) {
        log.debug("Finding all elements: {}", locator);
        getWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        return driver().findElements(locator);
    }

    /* ─────────────── Interactions ─────────────── */

    public void click(By locator) {
        log.info("Clicking element: {}", locator);
        try {
            findClickable(locator).click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Normal click intercepted, trying JS click: {}", locator);
            jsClick(locator);
        }
    }

    public void jsClick(By locator) {
        log.info("JS click on: {}", locator);
        WebElement element = find(locator);
        ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", element);
    }

    public void type(By locator, String text) {
        log.info("Typing '{}' into: {}", text, locator);
        WebElement element = findVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    public void clearAndType(By locator, String text) {
        log.info("Clearing and typing '{}' into: {}", text, locator);
        WebElement element = findVisible(locator);
        element.sendKeys(Keys.CONTROL + "a");
        element.sendKeys(Keys.DELETE);
        element.sendKeys(text);
    }

    public void pressKey(By locator, Keys key) {
        log.info("Pressing key [{}] on: {}", key.name(), locator);
        findVisible(locator).sendKeys(key);
    }

    /* ─────────────── Dropdown (Select) ─────────────── */

    public void selectByVisibleText(By locator, String text) {
        log.info("Selecting '{}' by visible text in: {}", text, locator);
        new Select(findVisible(locator)).selectByVisibleText(text);
    }

    public void selectByValue(By locator, String value) {
        log.info("Selecting by value '{}' in: {}", value, locator);
        new Select(findVisible(locator)).selectByValue(value);
    }

    public void selectByIndex(By locator, int index) {
        log.info("Selecting by index [{}] in: {}", index, locator);
        new Select(findVisible(locator)).selectByIndex(index);
    }

    public List<String> getDropdownOptions(By locator) {
        Select select = new Select(findVisible(locator));
        return select.getOptions().stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public String getSelectedOption(By locator) {
        return new Select(findVisible(locator)).getFirstSelectedOption().getText();
    }

    /* ─────────────── Text / Attribute ─────────────── */

    public String getText(By locator) {
        String text = findVisible(locator).getText();
        log.debug("Got text '{}' from: {}", text, locator);
        return text;
    }

    public String getAttribute(By locator, String attribute) {
        return findVisible(locator).getAttribute(attribute);
    }

    public String getValue(By locator) {
        return getAttribute(locator, "value");
    }

    /* ─────────────── State Checks ─────────────── */

    public boolean isDisplayed(By locator) {
        try {
            return findVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        return find(locator).isEnabled();
    }

    public boolean isSelected(By locator) {
        return find(locator).isSelected();
    }

    public boolean isPresent(By locator) {
        try {
            driver().findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /* ─────────────── Wait Conditions ─────────────── */

    public void waitForVisible(By locator) {
        getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitForInvisible(By locator) {
        getWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void waitForText(By locator, String text) {
        getWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public void waitForUrl(String urlFragment) {
        getWait().until(ExpectedConditions.urlContains(urlFragment));
    }

    /* ─────────────── Scroll ─────────────── */

    public void scrollToElement(By locator) {
        WebElement element = find(locator);
        ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void scrollToTop() {
        ((JavascriptExecutor) driver()).executeScript("window.scrollTo(0, 0);");
    }

    public void scrollToBottom() {
        ((JavascriptExecutor) driver()).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    /* ─────────────── Actions (Hover, Drag-Drop) ─────────────── */

    public void hover(By locator) {
        log.info("Hovering over: {}", locator);
        new Actions(driver()).moveToElement(find(locator)).perform();
    }

    public void dragAndDrop(By source, By target) {
        log.info("Drag from {} to {}", source, target);
        new Actions(driver()).dragAndDrop(find(source), find(target)).perform();
    }

    public void doubleClick(By locator) {
        log.info("Double-clicking: {}", locator);
        new Actions(driver()).doubleClick(findClickable(locator)).perform();
    }

    public void rightClick(By locator) {
        log.info("Right-clicking: {}", locator);
        new Actions(driver()).contextClick(find(locator)).perform();
    }

    /* ─────────────── JavaScript ─────────────── */

    public void jsSetValue(By locator, String value) {
        WebElement el = find(locator);
        ((JavascriptExecutor) driver()).executeScript("arguments[0].value=arguments[1];", el, value);
    }

    public Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver()).executeScript(script, args);
    }

    /* ─────────────── Alerts ─────────────── */

    public String getAlertText() {
        getWait().until(ExpectedConditions.alertIsPresent());
        return driver().switchTo().alert().getText();
    }

    public void acceptAlert() {
        getWait().until(ExpectedConditions.alertIsPresent());
        driver().switchTo().alert().accept();
    }

    public void dismissAlert() {
        getWait().until(ExpectedConditions.alertIsPresent());
        driver().switchTo().alert().dismiss();
    }

    /* ─────────────── Frames & Windows ─────────────── */

    public void switchToFrame(By locator) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    public void switchToDefaultContent() {
        driver().switchTo().defaultContent();
    }

    public void switchToNewTab() {
        String originalWindow = driver().getWindowHandle();
        getWait().until(ExpectedConditions.numberOfWindowsToBe(2));
        driver().getWindowHandles().stream()
                .filter(w -> !w.equals(originalWindow))
                .findFirst()
                .ifPresent(w -> driver().switchTo().window(w));
    }
}
