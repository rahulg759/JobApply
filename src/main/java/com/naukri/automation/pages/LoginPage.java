package com.naukri.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.naukri.automation.config.ConfigManager;
import com.naukri.automation.utils.ScreenshotHelper;

public class LoginPage extends BasePage {

    private final Locator loginHeaderButton;
    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator loginSubmitButton;
    private final Locator emailTab;

    public LoginPage(Page page) {
        super(page);
        // Try multiple selectors for login button - website might have changed
        this.loginHeaderButton = locator("a[title='Login']");
        this.emailInput = locator("input[placeholder*='Email']");
        this.passwordInput = locator("input[type='password']");
        this.loginSubmitButton = locator("button[type='submit']:has-text('Login')");
        this.emailTab = locator("button:has-text('Email')");
    }

    public LoginPage navigateToLogin() {
        String url = ConfigManager.getInstance().getNaukriUrl();
        log.info("Navigating to Naukri: {}", url);
        page.navigate(url);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        wait.waitForPageLoad();
        sleep(2000); // Extra wait for dynamic content
        return this;
    }

    public LoginPage clickLoginButton() {
        log.info("Clicking login button in header");
        // Try multiple selectors - Naukri changes login button selector frequently
        boolean clicked = false;
        for (String selector : new String[]{"a[title='Login']", "a:has-text('Login')", "//a[contains(@title, 'Login')]"}) {
            try {
                Locator btn = locator(selector).first();
                if (btn.isVisible()) {
                    click(btn);
                    clicked = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        if (!clicked) {
            log.error("Could not find login button with any selector");
            ScreenshotHelper.capture(page, "login_button_not_found");
            throw new RuntimeException("Login button not found");
        }
        sleep(1500);
        return this;
    }

    public LoginPage switchToEmailTab() {
        if (emailTab.isVisible()) {
            log.info("Switching to email login tab");
            click(emailTab);
            sleep(500);
        }
        return this;
    }

    public LoginPage enterEmail(String email) {
        log.info("Entering email");
        fill(emailInput, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        log.info("Entering password");
        fill(passwordInput, password);
        return this;
    }

    public LoginPage clickSubmit() {
        log.info("Clicking login submit button");
        click(loginSubmitButton);
        wait.waitForPageLoad();
        return this;
    }

    public HomePage login(String email, String password) {
        navigateToLogin()
                .clickLoginButton()
                .switchToEmailTab()
                .enterEmail(email)
                .enterPassword(password)
                .clickSubmit();
        sleep(3000);
        return new HomePage(page);
    }
}
