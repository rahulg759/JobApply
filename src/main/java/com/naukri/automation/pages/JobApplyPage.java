package com.naukri.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class JobApplyPage extends BasePage {

    private final Locator resumeUploadInput;
    private final Locator submitButton;
    private final Locator nextButton;
    private final Locator successMessage;
    private final Locator closeModalButton;
    private final Locator phoneInput;

    public JobApplyPage(Page page) {
        super(page);
        this.resumeUploadInput = locator("input[type='file'][accept*='pdf'], input[type='file'][accept*='doc'], input[name='file']").first();
        this.submitButton = locator("button:has-text('Submit'), button:has-text('Apply Now'), button[type='submit']").first();
        this.nextButton = locator("button:has-text('Next')").first();
        this.successMessage = locator("div:has-text('application submitted'), span:has-text('applied'), div[class*='success'], div[class*='thank']").first();
        this.closeModalButton = locator("button[class*='close'], span[class*='close'], button[aria-label*='Close']").first();
        this.phoneInput = locator("input[type='tel'], input[placeholder*='phone'], input[placeholder*='Phone']").first();
    }

    public JobApplyPage uploadResume(String resumePath) {
        if (resumePath == null || resumePath.isEmpty()) {
            log.info("No resume path provided, skipping upload");
            return this;
        }

        if (resumeUploadInput.isVisible()) {
            log.info("Uploading resume: {}", resumePath);
            try {
                resumeUploadInput.setInputFiles(java.nio.file.Paths.get(resumePath));
                sleep(2000);
            } catch (Exception e) {
                log.warn("Failed to upload resume: {}", e.getMessage());
            }
        } else {
            log.info("Resume upload not required or already uploaded");
        }
        return this;
    }

    public JobApplyPage fillPhone(String phone) {
        if (phone != null && !phone.isEmpty() && phoneInput.isVisible()) {
            log.info("Filling phone number");
            fill(phoneInput, phone);
        }
        return this;
    }

    public JobApplyPage clickNext() {
        if (nextButton.isVisible()) {
            log.info("Clicking Next button");
            click(nextButton);
            sleep(2000);
        }
        return this;
    }

    public JobApplyPage clickSubmit() {
        log.info("Clicking Submit/Apply button");
        try {
            click(submitButton);
            sleep(3000);
        } catch (Exception e) {
            log.warn("Submit button not found or click failed: {}", e.getMessage());
        }
        return this;
    }

    public boolean isApplicationSuccessful() {
        boolean success = successMessage.isVisible();
        log.info("Application successful: {}", success);
        return success;
    }

    public JobSearchPage closeModal() {
        if (closeModalButton.isVisible()) {
            log.info("Closing application modal");
            click(closeModalButton);
            sleep(1000);
        }
        return new JobSearchPage(page);
    }

    public boolean isApplyButtonVisible() {
        return submitButton.isVisible();
    }
}
