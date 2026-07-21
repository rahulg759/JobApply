package com.naukri.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.naukri.automation.config.ConfigManager;

public class HomePage extends BasePage {

    private final Locator searchKeywordInput;
    private final Locator searchLocationInput;
    private final Locator searchButton;
    private final Locator userAvatar;
    private final Locator logoutLink;

    public HomePage(Page page) {
        super(page);
        this.searchKeywordInput = page.locator("input.suggestor-input:not([tabindex='-1'])").first();
        this.searchLocationInput = page.locator("input.suggestor-input:not([tabindex='-1'])").nth(1);
        this.searchButton = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search"));
        this.userAvatar = page.getByLabel("Open profile menu");
        this.logoutLink = locator("a:has-text('Logout')");
    }

    public boolean isLoggedIn() {
        boolean visible = userAvatar.isVisible();
        log.info("User logged in: {}", visible);
        return visible;
    }

    public HomePage awaitPageLoad() {
        wait.waitForPageLoad();
        sleep(2000);
        return this;
    }

    private void navigateToSearch(String keyword, String location) {
        String searchUrl = "https://www.naukri.com/" + keyword.toLowerCase().replace(" ", "-")
                + "-jobs-in-" + location.toLowerCase().replace(" ", "-");
        log.info("Navigating to search URL: {}", searchUrl);
        page.navigate(searchUrl);
        wait.waitForPageLoad();
        sleep(5000);
    }

    public HomePage enterSearchKeyword(String keyword) {
        log.info("Entering search keyword: {}", keyword);
        return this;
    }

    public HomePage enterSearchLocation(String location) {
        log.info("Entering search location: {}", location);
        return this;
    }

    public HomePage clickSearch() {
        log.info("Clicking search button");
        return this;
    }

    public JobSearchPage searchJobs(String keyword, String location) {
        navigateToSearch(keyword, location);
        return new JobSearchPage(page);
    }
}
