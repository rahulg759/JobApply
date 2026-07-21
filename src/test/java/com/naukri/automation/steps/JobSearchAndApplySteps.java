package com.naukri.automation.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naukri.automation.context.SharedContext;
import com.naukri.automation.pages.LoginPage;
import com.naukri.automation.pages.HomePage;
import com.naukri.automation.pages.JobSearchPage;
import com.naukri.automation.pages.JobApplyPage;
import com.naukri.automation.models.SearchFilters;
import com.naukri.automation.models.JobListing;
import com.naukri.automation.engine.JobScorer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JobSearchAndApplySteps {

    private static final Logger log = LogManager.getLogger(JobSearchAndApplySteps.class);
    private final SharedContext ctx;

    public JobSearchAndApplySteps(SharedContext ctx) {
        this.ctx = ctx;
    }

    @Given("I am logged into Naukri")
    public void iAmLoggedIntoNaukri() {
        Allure.step("Login to Naukri.com");
        var config = ctx.getConfig();
        var page = ctx.getPage();

        LoginPage loginPage = new LoginPage(page);
        HomePage homePage = loginPage.login(config.getEmail(), config.getPassword());
        assertThat(homePage.isLoggedIn())
                .as("Login should be successful")
                .isTrue();
        ctx.setHomePage(homePage);
        log.info("Login successful");
    }

    @When("I search for my configured jobs")
    public void iSearchForMyConfiguredJobs() {
        Allure.step("Search for jobs with keywords: " + ctx.getConfig().getSearchKeywords());
        HomePage homePage = ctx.getHomePage();
        var config = ctx.getConfig();

        JobSearchPage searchPage = homePage.searchJobs(
                config.getSearchKeywords(),
                config.getSearchLocation()
        );
        sleep(3000);
        ctx.setSearchPage(searchPage);
        log.info("Search completed for '{}' in '{}'", config.getSearchKeywords(), config.getSearchLocation());
    }

    @When("I apply job filters")
    public void iApplyJobFilters() {
        Allure.step("Apply job filters");
        JobSearchPage searchPage = ctx.getSearchPage();
        var config = ctx.getConfig();

        SearchFilters filters = new SearchFilters(
                config.getSearchKeywords(),
                config.getSearchLocation(),
                config.getExperienceLevels(),
                config.getDatePosted(),
                config.getCompany()
        );
        searchPage.applyFilters(filters);
        sleep(2000);
    }

    @When("I load and collect job listings")
    public void iLoadAndCollectJobListings() {
        Allure.step("Load job listings");
        JobSearchPage searchPage = ctx.getSearchPage();

        searchPage.scrollToLoadMore(3);
        List<JobListing> allListings = searchPage.getAllListings();
        Allure.addAttachment("total_listings_found", "text/plain",
                String.valueOf(allListings.size()));
        ctx.setAllListings(allListings);
        log.info("Total listings found: {}", allListings.size());
    }

    @Then("I should find at least one job listing")
    public void iShouldFindAtLeastOneJobListing() {
        assertThat(ctx.getAllListings())
                .as("Should find at least one job listing")
                .isNotEmpty();
    }

    @When("I score each job against my target keywords")
    public void iScoreEachJobAgainstMyTargetKeywords() {
        Allure.step("Score job descriptions against target keywords");
        JobSearchPage searchPage = ctx.getSearchPage();
        var config = ctx.getConfig();
        List<JobListing> allListings = ctx.getAllListings();
        List<String> matchingKeywords = config.getMatchingKeywords();
        int minimumScore = config.getMinimumScore();

        log.info("Matching keywords: {} (minimum score: {})", matchingKeywords, minimumScore);

        List<JobListing> qualifiedJobs = new ArrayList<>();
        List<JobListing> scoredListings = new ArrayList<>();

        for (int i = 0; i < allListings.size(); i++) {
            Allure.step("Evaluating job " + (i + 1) + ": " + allListings.get(i).title());

            searchPage.clickJobCard(i);
            sleep(2000);
            String description = searchPage.getJobDescription();
            log.info("Job #{} '{}': description length={} chars, preview='{}'",
                    i + 1, allListings.get(i).title(),
                    description.length(),
                    description.length() > 120 ? description.substring(0, 120).replaceAll("\\s+", " ") + "..." : description.replaceAll("\\s+", " "));

            JobListing scored = JobScorer.score(
                    allListings.get(i), description, matchingKeywords, minimumScore
            );
            scoredListings.add(scored);

            log.info("Job #{}: '{}' at '{}' - Score: {}/{}",
                    i + 1, scored.title(), scored.company(), scored.matchScore(), minimumScore);

            if (scored.isQualified(minimumScore)) {
                qualifiedJobs.add(scored);
            }
        }

        Allure.addAttachment("scored_jobs", "application/json",
                toJson(scoredListings));
        Allure.addAttachment("qualified_jobs_count", "text/plain",
                String.valueOf(qualifiedJobs.size()));
        ctx.setQualifiedJobs(qualifiedJobs);
        log.info("Qualified jobs: {}/{}", qualifiedJobs.size(), allListings.size());
    }

    @Then("I should have qualified jobs")
    public void iShouldHaveQualifiedJobs() {
        assertThat(ctx.getQualifiedJobs())
                .as("Should have at least one qualified job to apply to")
                .isNotEmpty();
    }

    @When("I apply to all qualified jobs")
    public void iApplyToAllQualifiedJobs() {
        Allure.step("Apply to qualified jobs (" + ctx.getQualifiedJobs().size() + " jobs)");
        JobSearchPage searchPage = ctx.getSearchPage();
        var config = ctx.getConfig();
        List<JobListing> qualifiedJobs = ctx.getQualifiedJobs();
        List<JobListing> allListings = ctx.getAllListings();
        List<String> appliedJobs = new ArrayList<>();

        for (JobListing job : qualifiedJobs) {
            Allure.step("Applying to: " + job.title() + " at " + job.company());

            int originalIndex = allListings.indexOf(job);
            if (originalIndex < 0) continue;

            searchPage.clickJobCard(originalIndex);
            sleep(2000);

            if (searchPage.hasApplyButton()) {
                log.info("Applying to '{}' at '{}'", job.title(), job.company());
                JobApplyPage applyPage = searchPage.clickApply();
                sleep(2000);

                applyPage.uploadResume(config.getResumePath());
                sleep(1000);
                applyPage.clickSubmit();
                sleep(3000);

                boolean success = applyPage.isApplicationSuccessful();
                log.info("Application result for '{}': {}", job.title(), success);

                if (success) {
                    appliedJobs.add(job.title() + " - " + job.company());
                }

                ctx.setSearchPage(applyPage.closeModal());
                sleep(2000);
            } else {
                log.info("No apply button for '{}' at '{}', skipping", job.title(), job.company());
            }
        }

        ctx.setAppliedJobs(appliedJobs);
    }

    @Then("the application summary should be reported")
    public void theApplicationSummaryShouldBeReported() {
        List<JobListing> allListings = ctx.getAllListings();
        List<?> qualifiedJobs = ctx.getQualifiedJobs();
        List<String> appliedJobs = ctx.getAppliedJobs();

        Allure.addAttachment("applied_jobs", "application/json", toJson(appliedJobs));
        Allure.addAttachment("application_summary", "text/plain", String.format(
                "Total listings: %d\nQualified: %d\nSuccessfully applied: %d",
                allListings.size(), qualifiedJobs.size(), appliedJobs.size()
        ));

        log.info("=== APPLICATION SUMMARY ===");
        log.info("Total listings found: {}", allListings.size());
        log.info("Jobs matching criteria: {}", qualifiedJobs.size());
        log.info("Successfully applied: {}", appliedJobs.size());
        log.info("===========================");
    }

    

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }
}
