package com.naukri.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naukri.automation.engine.JobScorer;
import com.naukri.automation.models.JobFilterCriteria;
import com.naukri.automation.models.JobListing;
import com.naukri.automation.pages.HomePage;
import com.naukri.automation.pages.JobApplyPage;
import com.naukri.automation.pages.JobSearchPage;
import com.naukri.automation.pages.LoginPage;
import com.naukri.automation.utils.ExcelReader;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobSearchAndApplyTest extends BaseTest {

    @Test
    @Tag("regression")
    @DisplayName("Search QA Automation jobs, filter, score, and apply to matching listings")
    @Description("Complete flow: Login -> Search -> Filter -> Score -> Apply for matching jobs")
    void searchAndApplyToJobs() {
        // 1. Login
        Allure.step("Login to Naukri.com");
        LoginPage loginPage = new LoginPage(page);
        HomePage homePage = loginPage.login(
                config.getEmail(),
                config.getPassword()
        );
        assertThat(homePage.isLoggedIn())
                .as("Login should be successful")
                .isTrue();
        log.info("Login successful");

        // 2. Search jobs
        Allure.step("Search for jobs with keywords: " + config.getSearchKeywords());
        JobSearchPage searchPage = homePage.searchJobs(
                config.getSearchKeywords(),
                config.getSearchLocation()
        );
        sleepBetweenActions(3000);
        log.info("Search completed for '{}' in '{}'", config.getSearchKeywords(), config.getSearchLocation());

        // 3. Apply filters from Excel
        Allure.step("Apply job filters from Excel");
        List<JobFilterCriteria> filters = ExcelReader.readFilters();
        if (!filters.isEmpty()) {
            searchPage.applyFilters(filters);
        } else {
            log.info("No filters found in Excel, skipping filter application");
        }
        sleepBetweenActions(2000);

        // 4. Scroll to load more listings
        Allure.step("Load job listings");
        searchPage.scrollToLoadMore(3);
        List<JobListing> allListings = searchPage.getAllListings();
        Allure.addAttachment("total_listings_found", "text/plain",
                String.valueOf(allListings.size()));
        log.info("Total listings found: {}", allListings.size());

        assertThat(allListings)
                .as("Should find at least one job listing")
                .isNotEmpty();

        // 5. Score each job by matching keywords in description
        Allure.step("Score job descriptions against target keywords");
        List<String> matchingKeywords = config.getMatchingKeywords();
        int minimumScore = config.getMinimumScore();
        log.info("Matching keywords: {} (minimum score: {})", matchingKeywords, minimumScore);

        List<JobListing> qualifiedJobs = new ArrayList<>();
        List<JobListing> scoredListings = new ArrayList<>();

        for (int i = 0; i < allListings.size(); i++) {
            Allure.step("Evaluating job " + (i + 1) + ": " + allListings.get(i).title());

            searchPage.clickJobCard(i);
            sleepBetweenActions(2000);
            String description = searchPage.getJobDescription();

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
        log.info("Qualified jobs: {}/{}", qualifiedJobs.size(), allListings.size());

        // 6. Apply to qualified jobs
        Allure.step("Apply to qualified jobs (" + qualifiedJobs.size() + " jobs)");
        List<String> appliedJobs = new ArrayList<>();

        for (int i = 0; i < qualifiedJobs.size(); i++) {
            JobListing job = qualifiedJobs.get(i);
            Allure.step("Applying to: " + job.title() + " at " + job.company());

            int originalIndex = allListings.indexOf(job);
            if (originalIndex < 0) continue;

            searchPage.clickJobCard(originalIndex);
            sleepBetweenActions(2000);

            if (searchPage.hasApplyButton()) {
                log.info("Applying to '{}' at '{}'", job.title(), job.company());
                JobApplyPage applyPage = searchPage.clickApply();
                sleepBetweenActions(2000);

                applyPage.uploadResume(config.getResumePath());
                sleepBetweenActions(1000);
                applyPage.clickSubmit();
                sleepBetweenActions(3000);

                boolean success = applyPage.isApplicationSuccessful();
                log.info("Application result for '{}': {}", job.title(), success);

                if (success) {
                    appliedJobs.add(job.title() + " - " + job.company());
                }

                searchPage = applyPage.closeModal();
                sleepBetweenActions(2000);
            } else {
                log.info("No apply button for '{}' at '{}', skipping", job.title(), job.company());
            }
        }

        // 7. Report results
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

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }
}
