package com.naukri.automation;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.naukri.automation.browser.BrowserManager;
import com.naukri.automation.config.ConfigManager;
import com.naukri.automation.listeners.AllureLifecycleListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ExtendWith(AllureLifecycleListener.class)
public abstract class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);
    protected static ConfigManager config;
    protected static BrowserManager browserManager;
    protected Page page;
    protected BrowserContext context;

    @BeforeAll
    static void setUpAll() {
        config = ConfigManager.getInstance();
        browserManager = BrowserManager.getInstance();
        browserManager.initialize();
        log.info("Test suite initialized");
    }

    @BeforeEach
    void setUp() {
        context = browserManager.createContext();
        page = browserManager.newPage(context);
        AllureLifecycleListener.setPage(page);
        log.info("Test setup complete - new browser context created");
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
            log.info("Browser context closed");
        }
        AllureLifecycleListener.removePage();
    }

    @AfterAll
    static void tearDownAll() {
        if (browserManager != null) {
          //  browserManager.close();
            log.info("Browser manager closed");
        }
    }

    protected ConfigManager getConfig() {
        return config;
    }

    protected Page getPage() {
        return page;
    }

    protected void sleepBetweenActions(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
