package com.naukri.automation.config;

import com.naukri.automation.exception.AutomationException;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private final Properties properties;
    private final Dotenv dotenv;

    private ConfigManager() {
        properties = new Properties();
        dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();
        loadPropertiesFile();
        log.info("Configuration loaded successfully");
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadPropertiesFile() {
        Path configPath = Paths.get("config", "config.properties");
        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            properties.load(fis);
        } catch (IOException e) {
            throw AutomationException.wrap("Failed to load config.properties from " + configPath.toAbsolutePath(), e);
        }
    }

    private String resolve(String key) {
        String systemProp = System.getProperty(key);
        if (systemProp != null && !systemProp.isEmpty()) {
            return systemProp;
        }
        String envValue = readDotEnv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return properties.getProperty(key);
    }

    private String readDotEnv(String key) {
        java.nio.file.Path envPath = java.nio.file.Paths.get(".env");
        if (!envPath.toFile().exists()) return null;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String k = line.substring(0, eqIdx).trim();
                    String v = line.substring(eqIdx + 1).trim();
                    if (k.equals(key)) return v;
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read .env file: {}", e.getMessage());
        }
        return null;
    }

    public BrowserConfig getBrowserConfig() {
        return new BrowserConfig(
                getBrowserType(),
                isHeadless(),
                resolve("browser.channel"),
                getSlowMo(),
                getViewportWidth(),
                getViewportHeight()
        );
    }

    public BrowserType getBrowserType() {
        String browser = resolve("browser");
        if (browser == null || browser.isEmpty()) {
            return BrowserType.CHROME;
        }
        return switch (browser.trim().toLowerCase()) {
            case "edge" -> BrowserType.EDGE;
            case "firefox" -> BrowserType.FIREFOX;
            default -> BrowserType.CHROME;
        };
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(resolve("browser.headless"));
    }

    public int getSlowMo() {
        return parseInt("browser.slowMo", 500);
    }

    public int getViewportWidth() {
        return parseInt("browser.viewport.width", 1920);
    }

    public int getViewportHeight() {
        return parseInt("browser.viewport.height", 1080);
    }

    public String getSite() {
        String site = resolve("site");
        return (site != null && !site.isEmpty()) ? site.trim().toLowerCase() : "naukri";
    }

    public String getNaukriUrl() {

        String url = resolve("NAUKRI_URL");
        System.out.println("DEBUG URL = " + url);
        return url;
        //return resolve("NAUKRI_URL");
    }

    public String getEmail() {
        return resolve("NAUKRI_EMAIL");
    }

    public String getPassword() {
        return resolve("NAUKRI_PASSWORD");
    }

    public String getResumePath() {
        String path = resolve("RESUME_PATH");
        if (path == null || path.isEmpty()) return path;
        java.nio.file.Path resolved = java.nio.file.Paths.get(path);
        if (!resolved.isAbsolute()) {
            resolved = java.nio.file.Paths.get(System.getProperty("user.dir")).resolve(path).normalize();
        }
        return resolved.toString();
    }

    public String getSearchKeywords() {
        return resolve("search.keywords");
    }

    public String getSearchLocation() {
        return resolve("search.location");
    }

    public List<String> getExperienceLevels() {
        String value = resolve("search.experience");
        if (value == null || value.isEmpty()) return List.of();
        return Arrays.asList(value.split(","));
    }

    public String getDatePosted() {
        return resolve("search.datePosted");
    }

    public String getCompany() {
        return resolve("search.company");
    }

    public List<String> getMatchingKeywords() {
        String value = resolve("matching.keywords");
        if (value == null || value.isEmpty()) return List.of();
        return Arrays.asList(value.split(","));
    }

    public int getMinimumScore() {
        return parseInt("matching.minimumScore", 3);
    }

    public int getExplicitTimeout() {
        return parseInt("timeout.explicit", 30);
    }

    public int getPollingInterval() {
        return parseInt("timeout.polling.interval", 500);
    }

    private int parseInt(String key, int defaultValue) {
        String value = resolve(key);
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value for '{}': '{}'. Using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
}
