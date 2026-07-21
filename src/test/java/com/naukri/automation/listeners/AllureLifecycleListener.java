package com.naukri.automation.listeners;

import com.microsoft.playwright.Page;
import com.naukri.automation.utils.ScreenshotHelper;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.ByteArrayInputStream;

public class AllureLifecycleListener implements AfterTestExecutionCallback {

    private static final Logger log = LogManager.getLogger(AllureLifecycleListener.class);
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    public static void setPage(Page page) {
        pageThreadLocal.set(page);
    }

    public static void removePage() {
        pageThreadLocal.remove();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean testFailed = context.getExecutionException().isPresent();
        if (testFailed) {
            Page page = pageThreadLocal.get();
            if (page != null) {
                try {
                    String testName = context.getDisplayName();
                    byte[] screenshot = ScreenshotHelper.capture(page, testName);
                    Allure.getLifecycle().addAttachment(
                            "Screenshot_" + testName,
                            "image/png",
                            "png",
                            new ByteArrayInputStream(screenshot)
                    );
                    log.info("Screenshot attached to Allure report for failed test: {}", testName);
                } catch (Exception e) {
                    log.error("Failed to attach screenshot to Allure report", e);
                }
            }
        }
    }
}
