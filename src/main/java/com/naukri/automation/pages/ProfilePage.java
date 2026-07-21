package com.naukri.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

public class ProfilePage extends BasePage {

    private final Locator resumeSectionTab;
    private final Locator resumeUploadInput;
    private final Locator resumeUploadButton;
    private final Locator resumeFileName;
    private final Locator saveButton;
    private final Locator successMessage;
    private final Locator deleteResumeButton;
    private final Locator confirmDeleteButton;
    private final Locator logoutLink;
    private String lastUploadedFileName;

    public ProfilePage(Page page) {
        super(page);
        // Resume section within profile - Naukri has a "Resume" tab/section
        this.resumeSectionTab = locator("a:has-text('Resume'), span:has-text('Resume'), div:has-text('Resume')").first();
        this.resumeUploadInput = locator("input[type='file']").first();
        this.resumeUploadButton = locator("button:has-text('Upload'), button:has-text('Browse'), a:has-text('Upload'), a:has-text('Update')").first();
        this.resumeFileName = locator("span[class*='resume']").first();
        this.saveButton = locator("button:has-text('Save'), button[type='submit']").first();
        this.successMessage = locator("div:has-text('uploaded'), span:has-text('uploaded'), div[class*='success']").first();
        this.deleteResumeButton = locator("i[class*='delete'], span[class*='delete'], button[class*='delete'], a[aria-label*='Delete'], a:has-text('Delete')").first();
        this.confirmDeleteButton = locator("button:has-text('Yes'), button:has-text('Delete'), button:has-text('Confirm')").first();
        this.logoutLink = locator("a:has-text('Logout')").first();
    }

    public ProfilePage navigateToProfile() {
        String profileUrl = "https://www.naukri.com/mnjuser/profile";
        log.info("Navigating to profile page: {}", profileUrl);
        page.navigate(profileUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        wait.waitForPageLoad();
        sleep(3000);
        return this;
    }

    public ProfilePage goToResumeSection() {
        log.info("Navigating to Resume section");
        try {
            if (resumeSectionTab.isVisible()) {
                click(resumeSectionTab);
                sleep(2000);
            }
        } catch (Exception e) {
            log.info("Resume tab not found, might already be on resume section");
        }
        return this;
    }

    public ProfilePage uploadResume(String resumePath) {
        if (resumePath == null || resumePath.isEmpty()) {
            log.info("No resume path provided, skipping upload");
            return this;
        }

        log.info("Looking for resume upload area");

        // Store filename for later verification fallback
        try {
            lastUploadedFileName = java.nio.file.Paths.get(resumePath).getFileName().toString();
        } catch (Exception e) {
            lastUploadedFileName = null;
        }

        try {
            // Try clicking upload/update button first if visible
            if (resumeUploadButton.isVisible()) {
                log.info("Clicking upload/update resume button");
                click(resumeUploadButton);
                sleep(2000);
            }
        } catch (Exception e) {
            log.info("Upload button not found, trying direct file input");
        }

        try {
            if (resumeUploadInput.isVisible()) {
                log.info("Uploading resume via file input: {}", resumePath);
                resumeUploadInput.setInputFiles(java.nio.file.Paths.get(resumePath));
                sleep(3000);
            } else {
                log.warn("Resume file input not found on page");
            }
        } catch (Exception e) {
            log.warn("Failed to upload resume: {}", e.getMessage());
        }

        return this;
    }

    public ProfilePage clickSave() {
        try {
            if (saveButton.isVisible()) {
                log.info("Clicking Save button");
                click(saveButton);
                sleep(2000);
            }
        } catch (Exception e) {
            log.info("Save button not found or not needed");
        }
        return this;
    }

    public boolean isUploadSuccessful() {
        try {
            boolean success = successMessage.isVisible();
            log.info("Resume upload successful: {}", success);
            if (success) return true;
        } catch (Exception e) {
            log.debug("Success message not found");
        }

        // Alternatively check if a resume filename is displayed
        try {
            boolean hasResume = resumeFileName.isVisible();
            log.info("Resume file displayed: {}", hasResume);
            return hasResume;
        } catch (Exception e) {
            log.debug("Resume filename not found");
        }

        // Fallback: check page text for the uploaded file name
        if (lastUploadedFileName != null) {
            try {
                String bodyText = page.textContent("body");
                if (bodyText != null && bodyText.contains(lastUploadedFileName)) {
                    log.info("Found uploaded filename in page text: {}", lastUploadedFileName);
                    return true;
                }
            } catch (Exception e) {
                log.debug("Failed to read page body for filename check: {}", e.getMessage());
            }
        }

        return false;
    }

    public ProfilePage deleteResume() {
        log.info("Looking for delete resume option");
        try {
            if (deleteResumeButton.isVisible()) {
                log.info("Clicking delete resume button");
                click(deleteResumeButton);
                sleep(2000);
                if (confirmDeleteButton.isVisible()) {
                    log.info("Confirming delete");
                    click(confirmDeleteButton);
                    sleep(3000);
                }
            } else {
                log.info("No delete button found, resume may not exist");
            }
        } catch (Exception e) {
            log.info("Could not delete resume: {}", e.getMessage());
        }
        return this;
    }

    public ProfilePage refreshPage() {
        log.info("Refreshing profile page");
        page.reload();
        wait.waitForPageLoad();
        sleep(3000);
        return this;
    }

    public HomePage logout() {
        log.info("Logging out");
        try {
            page.navigate("https://www.naukri.com");
            wait.waitForPageLoad();
            sleep(2000);
            if (logoutLink.isVisible()) {
                click(logoutLink);
                sleep(2000);
            }
        } catch (Exception e) {
            log.warn("Logout failed: {}", e.getMessage());
        }
        return new HomePage(page);
    }
}
