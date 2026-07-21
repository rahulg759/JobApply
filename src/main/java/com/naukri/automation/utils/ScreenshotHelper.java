package com.naukri.automation.utils;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotHelper {

    private static final Logger log = LogManager.getLogger(ScreenshotHelper.class);
    private static final Path SCREENSHOT_DIR = Paths.get("logs", "screenshots");

    static {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
        } catch (IOException e) {
            log.error("Failed to create screenshot directory: {}", SCREENSHOT_DIR, e);
        }
    }

    private ScreenshotHelper() {
    }

    public static byte[] capture(Page page, String testName) {
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true)
                    .setType(ScreenshotType.PNG));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path filePath = SCREENSHOT_DIR.resolve(String.format("%s_%s.png", testName, timestamp));
            Files.write(filePath, screenshot);

            log.info("Screenshot saved: {}", filePath);
            return screenshot;
        } catch (Exception e) {
            log.error("Failed to capture screenshot for test: {}", testName, e);
            return new byte[0];
        }
    }
}
