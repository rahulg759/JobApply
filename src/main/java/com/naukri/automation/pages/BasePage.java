package com.naukri.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.naukri.automation.utils.ScreenshotHelper;
import com.naukri.automation.utils.WaitHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final Page page;
    protected final WaitHelper wait;

    protected BasePage(Page page) {
        this.page = page;
        this.wait = new WaitHelper(page);
    }

    protected Locator locator(String selector) {
        return page.locator(selector);
    }

    protected void click(Locator locator) {
        wait.waitForClickable(locator);
        locator.click();
        log.debug("Clicked: {}", locator);
    }

    protected void click(String selector) {
        click(locator(selector));
    }

    protected void fill(Locator locator, String text) {
        wait.waitForVisible(locator);
        locator.clear();
        locator.fill(text);
        log.debug("Filled '{}' into: {}", maskPassword(text), locator);
    }

    protected void fill(String selector, String text) {
        fill(locator(selector), text);
    }

    protected String getText(Locator locator) {
        wait.waitForVisible(locator);
        return locator.textContent();
    }

    protected String getText(String selector) {
        return getText(locator(selector));
    }

    protected boolean isVisible(Locator locator) {
        return locator.isVisible();
    }

    protected boolean isVisible(String selector) {
        return isVisible(locator(selector));
    }

    protected void scrollIntoView(Locator locator) {
        locator.scrollIntoViewIfNeeded();
        log.debug("Scrolled into view: {}", locator);
    }

    protected void pressEnter(Locator locator) {
        wait.waitForVisible(locator);
        locator.press("Enter");
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public byte[] takeScreenshot(String testName) {
        return ScreenshotHelper.capture(page, testName);
    }

    private String maskPassword(String text) {
        return text; // masked at caller level if needed
    }
}
