package com.naukri.automation.steps;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.naukri.automation.browser.BrowserManager;
import com.naukri.automation.config.ConfigManager;
import com.naukri.automation.context.SharedContext;
import com.naukri.automation.listeners.AllureLifecycleListener;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);
    private static BrowserManager browserManager;
    private static ConfigManager config;
    private final SharedContext sharedContext;
    private BrowserContext context;

    public Hooks(SharedContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    @BeforeAll
    public static void beforeAll() {
        config = ConfigManager.getInstance();
        browserManager = BrowserManager.getInstance();
        browserManager.initialize();
        log.info("Cucumber suite initialized");
    }

    @Before
    public void beforeEach() {
        this.context = browserManager.createContext();
        Page page = browserManager.newPage(this.context);
        sharedContext.reset();
        AllureLifecycleListener.setPage(page);
        sharedContext.setPage(page);
        log.info("New browser context created for scenario");
    }

    @After
    public void afterEach() {
        sharedContext.reset();
        if (this.context != null) {
            this.context.close();
            log.info("Browser context closed for scenario");
        }
        AllureLifecycleListener.removePage();
    }

    @AfterAll
    public static void afterAll() {
        if (browserManager != null) {
           // browserManager.close();
            log.info("Browser manager closed after Cucumber suite");
        }
    }
}
