package com.naukri.automation.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.naukri.automation.config.ConfigManager;
import com.naukri.automation.exception.AutomationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class WaitHelper {

    private static final Logger log = LogManager.getLogger(WaitHelper.class);
    private final Page page;
    private final Duration defaultTimeout;

    public WaitHelper(Page page) {
        this.page = page;
        this.defaultTimeout = Duration.ofSeconds(ConfigManager.getInstance().getExplicitTimeout());
    }

    public Locator waitForVisible(Locator locator) {
        return waitForVisible(locator, defaultTimeout);
    }

    public Locator waitForVisible(Locator locator, Duration timeout) {
        log.debug("Waiting for element to be visible: {}", locator);
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeout.toMillis()));
            return locator;
        } catch (Exception e) {
            throw AutomationException.wrap("Element not visible after " + timeout.toSeconds() + "s: " + locator, e);
        }
    }

    public Locator waitForClickable(Locator locator) {
        return waitForClickable(locator, defaultTimeout);
    }

    public Locator waitForClickable(Locator locator, Duration timeout) {
        log.debug("Waiting for element to be clickable: {}", locator);
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeout.toMillis()));
            return locator;
        } catch (Exception e) {
            throw AutomationException.wrap("Element not clickable after " + timeout.toSeconds() + "s: " + locator, e);
        }
    }

    public boolean waitForElementState(Locator locator, WaitForSelectorState state, Duration timeout) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(state)
                    .setTimeout(timeout.toMillis()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForNetworkIdle() {
        waitForNetworkIdle(defaultTimeout);
    }

    public void waitForNetworkIdle(Duration timeout) {
        log.debug("Waiting for network idle...");
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(timeout.toMillis()));
        } catch (Exception e) {
            log.warn("Network did not reach idle state within {}s", timeout.toSeconds());
        }
    }

    public void waitForPageLoad() {
        log.debug("Waiting for page to load...");
        try {
            page.waitForLoadState(LoadState.LOAD);
            // Also wait for network to be idle for better stability
            page.waitForLoadState(LoadState.NETWORKIDLE, 
                    new Page.WaitForLoadStateOptions().setTimeout(10000)); // 10s timeout for network idle
        } catch (Exception e) {
            log.warn("Network idle timeout, but page load state completed");
        }
    }

    public void waitForUrl(String urlSubstring, Duration timeout) {
        try {
            page.waitForURL(urlSubstring,
                    new Page.WaitForURLOptions().setTimeout(timeout.toMillis()));
        } catch (Exception e) {
            throw AutomationException.wrap("URL did not match '" + urlSubstring + "' within " + timeout.toSeconds() + "s", e);
        }
    }
}
