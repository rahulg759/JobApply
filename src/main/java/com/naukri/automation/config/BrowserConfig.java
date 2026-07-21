package com.naukri.automation.config;

public record BrowserConfig(
        BrowserType browserType,
        boolean headless,
        String channel,
        int slowMo,
        int viewportWidth,
        int viewportHeight
) {
}
