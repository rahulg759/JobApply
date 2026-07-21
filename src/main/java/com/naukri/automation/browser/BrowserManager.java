package com.naukri.automation.browser;

import com.microsoft.playwright.*;
import com.naukri.automation.config.BrowserConfig;
import com.naukri.automation.config.ConfigManager;
import com.naukri.automation.exception.AutomationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class BrowserManager {

    private static final Logger log = LogManager.getLogger(BrowserManager.class);
    private static BrowserManager instance;
    private Playwright playwright;
    private Browser browser;

    private BrowserManager() {
    }

    public static synchronized BrowserManager getInstance() {
        if (instance == null) {
            instance = new BrowserManager();
        }
        return instance;
    }

    public void initialize() {
        if (playwright != null) {
            log.warn("BrowserManager already initialized. Closing previous instance first.");
          //  close();
        }

        log.info("Initializing Playwright...");
        playwright = Playwright.create();
        BrowserConfig config = ConfigManager.getInstance().getBrowserConfig();
        browser = launchBrowser(config);
        log.info("Browser '{}' launched successfully (headless: {})", config.browserType(), headless);
    }

    private Browser launchBrowser(BrowserConfig config) {
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", String.valueOf(config.headless()))
        );

        var options = new com.microsoft.playwright.BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(config.slowMo());

        if (config.channel() != null && !config.channel().isEmpty()) {
            options.setChannel(config.channel());
        }

        return switch (config.browserType()) {
            case CHROME, EDGE -> playwright.chromium().launch(options);
            case FIREFOX -> playwright.firefox().launch(options);
        };
    }

    public BrowserContext createContext() {
        return createContext(null, null);
    }

    public BrowserContext createContext(Double latitude, Double longitude) {
        if (browser == null) {
            throw new AutomationException("Browser not initialized. Call initialize() first.");
        }
        ConfigManager config = ConfigManager.getInstance();
        var options = new Browser.NewContextOptions()
                .setViewportSize(config.getViewportWidth(), config.getViewportHeight())
                .setLocale("en-IN")
                .setPermissions(List.of("geolocation"));

        // Default to New Delhi if no coordinates provided
        if (latitude == null || longitude == null) {
            options.setGeolocation(28.6139, 77.2090);
        } else {
            options.setGeolocation(latitude, longitude);
        }

        return browser.newContext(options);
    }

    public Page newPage(BrowserContext context) {
        return context.newPage();
    }

    public Page newPage() {
        return createContext().newPage();
    }

   /* public void close() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
        log.info("BrowserManager closed successfully");
    }*/
}
