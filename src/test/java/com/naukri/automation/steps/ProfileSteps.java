package com.naukri.automation.steps;

import com.naukri.automation.context.SharedContext;
import com.naukri.automation.pages.ProfilePage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileSteps {

    private static final Logger log = LogManager.getLogger(ProfileSteps.class);
    private final SharedContext ctx;

    public ProfileSteps(SharedContext ctx) {
        this.ctx = ctx;
    }

    @When("I navigate to my profile")
    public void iNavigateToMyProfile() {
        Allure.step("Navigate to profile page");
        ProfilePage profilePage = new ProfilePage(ctx.getPage());
        profilePage.navigateToProfile();
        ctx.setProfilePage(profilePage);
        log.info("Navigated to profile");
    }

    @When("I go to the resume section")
    public void iGoToTheResumeSection() {
        Allure.step("Go to resume section in profile");
        ProfilePage profilePage = ctx.getProfilePage();
        profilePage.goToResumeSection();
        log.info("Navigated to resume section");
    }

    @When("I delete the old resume")
    public void iDeleteTheOldResume() {
        Allure.step("Delete old resume");
        ProfilePage profilePage = ctx.getProfilePage();
        profilePage.deleteResume();
        log.info("Old resume deleted");
    }

    @When("I upload my resume")
    public void iUploadMyResume() {
        Allure.step("Upload resume through profile");
        ProfilePage profilePage = ctx.getProfilePage();
        var config = ctx.getConfig();

        profilePage.uploadResume(config.getResumePath());
        sleep(2000);

        log.info("Resume upload completed");
    }

    @When("I save the resume")
    public void iSaveTheResume() {
        Allure.step("Save resume");
        ProfilePage profilePage = ctx.getProfilePage();
        profilePage.clickSave();
        sleep(2000);
        log.info("Resume saved");
    }

    @When("I refresh the page")
    public void iRefreshThePage() {
        Allure.step("Refresh profile page");
        ProfilePage profilePage = ctx.getProfilePage();
        profilePage.refreshPage();
        log.info("Page refreshed");
    }

    @Then("the resume upload should be successful")
    public void theResumeUploadShouldBeSuccessful() {
        ProfilePage profilePage = ctx.getProfilePage();
        boolean success = profilePage.isUploadSuccessful();
        assertThat(success)
                .as("Resume upload through profile should be successful")
                .isTrue();
        Allure.addAttachment("resume_upload_result", "text/plain", "Success");
        log.info("Resume upload was successful");
    }

    @When("I logout of Naukri")
    public void iLogoutOfNaukri() {
        Allure.step("Logout from Naukri");
        ProfilePage profilePage = ctx.getProfilePage();
        profilePage.logout();
        log.info("Logged out successfully");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
