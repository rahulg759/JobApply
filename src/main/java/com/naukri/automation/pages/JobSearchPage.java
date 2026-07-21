package com.naukri.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.naukri.automation.models.JobFilterCriteria;
import com.naukri.automation.models.JobListing;
import com.naukri.automation.models.SearchFilters;

import java.util.ArrayList;
import java.util.List;

public class JobSearchPage extends BasePage {

    // Job card locators
    private final Locator jobCards;
    private final Locator jobTitle;
    private final Locator jobCompany;
    private final Locator jobLocation;
    private final Locator jobExperience;
    private final Locator jobSkills;
    private final Locator jobDescriptionContainer;
    private final Locator showMoreDescription;
    private final Locator applyButton;
    private final Locator noJobsMessage;

    // Filter locators
    private final Locator experienceFilter;
    private final Locator datePostedFilter;
    private final Locator companyFilter;
    private final Locator salaryFilter;
    private final Locator filterApplyButton;

    public JobSearchPage(Page page) {
        super(page);
        this.jobCards = locator("article.jobTuple, section[class*='jobTuple'], div[class*='jobCard'], div[class*='list'] > article");
        this.jobTitle = locator("a[class*='title'], a[class*='jobTitle'], span[class*='title']");
        this.jobCompany = locator("a[class*='company'], span[class*='company'], div[class*='company']");
        this.jobLocation = locator("span[class*='location'], li[class*='location'], span[class*='loc']");
        this.jobExperience = locator("span[class*='experience'], li[class*='experience'], span[class*='exp']");
        this.jobSkills = locator("div[class*='skill'] span, span[class*='skill']");
        this.jobDescriptionContainer = locator("div[class*='description'], section[class*='description'], div[class*='job-desc'], .jdDesc, [class*='descText'], [class*='jobDescription']").first();
        this.showMoreDescription = locator("span:has-text('show more'), a:has-text('more'), span[class*='more']").first();
        this.applyButton = locator("button:has-text('Apply'), a:has-text('Apply'), div[class*='apply'] button").first();
        this.noJobsMessage = locator("div:has-text('No jobs found')");

        this.experienceFilter = page.getByText("Experience", new Page.GetByTextOptions().setExact(true));
        this.datePostedFilter = page.getByText("Posted", new Page.GetByTextOptions().setExact(true));
        this.companyFilter = page.getByText("Company", new Page.GetByTextOptions().setExact(true));
        this.salaryFilter = page.getByText("Salary", new Page.GetByTextOptions().setExact(true));
        this.filterApplyButton = page.getByText("Apply").first();
    }

    public JobSearchPage applyFilters(SearchFilters filters) {
        log.info("Applying search filters");
        wait.waitForPageLoad();
        sleep(2000);

        if (filters.experienceLevels() != null && !filters.experienceLevels().isEmpty()) {
            applyExperienceFilter(filters.experienceLevels());
        }
        if (filters.datePosted() != null && !filters.datePosted().isEmpty()) {
            applyDatePostedFilter(filters.datePosted());
        }
        if (filters.company() != null && !filters.company().isEmpty()) {
            applyCompanyFilter(filters.company());
        }

        sleep(2000);
        return this;
    }

    private void applyExperienceFilter(List<String> levels) {
        log.info("Applying experience filter: {}", levels);
        if (experienceFilter.isVisible()) {
            click(experienceFilter);
            sleep(1000);
            for (String level : levels) {
                Locator checkbox = locator("span:has-text('" + level.replace("_", " ") + "')").first();
                if (checkbox.isVisible()) {
                    click(checkbox);
                }
            }
        }
    }

    private void applyDatePostedFilter(String datePosted) {
        log.info("Applying date posted filter: {}", datePosted);
        if (datePostedFilter.isVisible()) {
            click(datePostedFilter);
            sleep(1000);
            String displayText = switch (datePosted.toUpperCase()) {
                case "PAST_24_HOURS" -> "24 hours";
                case "PAST_WEEK" -> "week";
                case "PAST_MONTH" -> "month";
                default -> datePosted;
            };
            Locator option = locator("span:has-text('" + displayText + "'), label:has-text('" + displayText + "')").first();
            if (option.isVisible()) {
                click(option);
            }
        }
    }

    private void applyCompanyFilter(String company) {
        log.info("Applying company filter: {}", company);
        if (companyFilter.isVisible()) {
            click(companyFilter);
            sleep(1000);
            Locator input = locator("input[placeholder*='company'], input[placeholder*='Company']").first();
            if (input.isVisible()) {
                fill(input, company);
                sleep(500);
                pressEnter(input);
            }
        }
    }

    public List<JobListing> getAllListings() {
        List<JobListing> listings = new ArrayList<>();
        wait.waitForPageLoad();
        sleep(2000);

        // Debug: find what elements exist for job cards
        int allElements = page.evaluate("document.querySelectorAll('*[class*=\"job\"], *[class*=\"tupe\"], *[class*=\"listing\"], *[class*=\"card\"]').length").hashCode();
        String sampleClasses = page.evaluate("Array.from(document.querySelectorAll('*[class*=\"job\"], *[class*=\"tuple\"]')).slice(0,5).map(e => e.tagName + '.' + (e.className || '')).join(' | ') || 'none found'").toString();
        log.info("Matching elements count: {}, sample: {}", allElements, sampleClasses);

        int cardCount = jobCards.count();
        log.info("Found {} job cards on page", cardCount);

        for (int i = 0; i < cardCount; i++) {
            try {
                Locator card = jobCards.nth(i);
                String title = card.locator(jobTitle).first().textContent();
                String company = card.locator(jobCompany).first().textContent();
                String location = card.locator(jobLocation).first().textContent();
                String experience = card.locator(jobExperience).first().textContent();

                List<String> skills = new ArrayList<>();
                int skillCount = card.locator(jobSkills).count();
                for (int j = 0; j < skillCount; j++) {
                    skills.add(card.locator(jobSkills).nth(j).textContent());
                }

                boolean hasApply = card.locator(applyButton).isVisible();

                listings.add(new JobListing(
                        title != null ? title.trim() : "",
                        company != null ? company.trim() : "",
                        location != null ? location.trim() : "",
                        experience != null ? experience.trim() : "",
                        "", // description fetched separately
                        skills,
                        hasApply,
                        0,
                        List.of()
                ));

                log.debug("Found job: {} at {}", title != null ? title.trim() : "N/A", company != null ? company.trim() : "N/A");
            } catch (Exception e) {
                log.warn("Failed to parse job card at index {}: {}", i, e.getMessage());
            }
        }

        return listings;
    }

    public JobSearchPage scrollToLoadMore(int scrollCount) {
        log.info("Scrolling to load more results ({} times)", scrollCount);
        for (int i = 0; i < scrollCount; i++) {
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
            sleep(2000);
            int currentCount = jobCards.count();
            log.debug("Scroll {}: {} cards visible", i + 1, currentCount);
        }
        return this;
    }

    public JobSearchPage clickJobCard(int index) {
        log.info("Clicking job card at index {}", index);
        try {
            Locator card = jobCards.nth(index);
            scrollIntoView(card);
            click(card);
            sleep(2000);
            wait.waitForPageLoad();
        } catch (Exception e) {
            log.warn("Failed to click job card at index {}: {}", index, e.getMessage());
        }
        return this;
    }

    public String getJobDescription() {
        try {
            if (showMoreDescription.isVisible()) {
                click(showMoreDescription);
                sleep(1000);
            }

            // Attach page source snippet to help diagnose selector mismatch
            String bodyPreview = page.evaluate(
                "(() => { const el = document.querySelector('div[class*=\"desc\"], section[class*=\"desc\"], div[class*=\"job-desc\"], .jdDesc, .jobDescription, .description'); " +
                "return el ? el.tagName + '.' + (el.className || '') + ' → ' + el.textContent.trim().substring(0, 200).replace(/\\s+/g, ' ') : 'NO_MATCH'; })()"
            ).toString();
            log.info("Description container preview: {}", bodyPreview);

            if (jobDescriptionContainer.isVisible()) {
                String desc = jobDescriptionContainer.textContent();
                log.info("Job description length: {} characters", desc != null ? desc.length() : 0);
                return desc != null ? desc.trim() : "";
            } else {
                log.warn("jobDescriptionContainer not visible. Try selectors: {}", bodyPreview);
                // Fallback: try the dynamically found element
                String fallback = page.evaluate(
                    "(() => { const el = document.querySelector('div[class*=\"desc\"], section[class*=\"desc\"], .jdDesc, .description'); " +
                    "return el ? el.textContent.trim() : ''; })()"
                ).toString();
                if (!fallback.isEmpty()) {
                    log.info("Fallback description fetched ({} chars)", fallback.length());
                    return fallback;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch job description: {}", e.getMessage());
        }
        return "";
    }

    public boolean hasApplyButton() {
        return applyButton.isVisible();
    }

    public JobApplyPage clickApply() {
        log.info("Clicking apply button");
        try {
            click(applyButton);
            sleep(3000);
        } catch (Exception e) {
            log.warn("Apply button click failed: {}", e.getMessage());
        }
        return new JobApplyPage(page);
    }

    public JobSearchPage applyFilters(List<JobFilterCriteria> filters) {
        log.info("Applying filters from Excel ({} rows)", filters.size());
        wait.waitForPageLoad();
        sleep(2000);

        for (int i = 0; i < filters.size(); i++) {
            JobFilterCriteria criteria = filters.get(i);
            log.info("Applying filter row {}: salary='{}', location='{}', experience='{}'",
                    i + 1, criteria.salary(), criteria.location(), criteria.experience());

            if (criteria.salary() != null && !criteria.salary().isEmpty()) {
                applySalaryFilter(criteria.salary());
                sleep(1500);
            }
            if (criteria.location() != null && !criteria.location().isEmpty()) {
                applyLocationFilter(criteria.location());
                sleep(1500);
            }
            if (criteria.experience() != null && !criteria.experience().isEmpty()) {
                applyExperienceFilter(List.of(criteria.experience()));
                sleep(1500);
            }
        }

        sleep(2000);
        return this;
    }

    private void applySalaryFilter(String salaryRange) {
        log.info("Applying salary filter: {}", salaryRange);
        if (salaryFilter.isVisible()) {
            click(salaryFilter);
            sleep(1000);

            // Normalise "5-10" → "5-10 Lakhs" or "5-10 LPA" for Naukri labels
            String label = salaryRange.trim();
            if (!label.contains("Lakh") && !label.contains("LPA")) {
                label = label + " Lakhs";
            }
            Locator option = locator("span:has-text('" + label + "'), label:has-text('" + label + "')").first();
            if (option.isVisible()) {
                click(option);
                log.info("Selected salary option: {}", label);
            } else {
                log.warn("Salary option '{}' not visible on page", label);
            }
        } else {
            log.warn("Salary filter section not visible on page");
        }
    }

    private void applyLocationFilter(String location) {
        log.info("Applying location filter: {}", location);
        // Try the existing location input on the results page, if present
        Locator locationInput = locator("input[placeholder*='location'], input[placeholder*='Location'], input[placeholder*='city']").first();
        if (locationInput.isVisible()) {
            fill(locationInput, location);
            sleep(500);
            // Pick the first suggestion
            Locator suggestion = locator("div[class*='suggestion']:has-text('" + location + "'), li:has-text('" + location + "')").first();
            if (suggestion.isVisible()) {
                click(suggestion);
                log.info("Selected location: {}", location);
            } else {
                pressEnter(locationInput);
            }
        } else {
            // Some Naukri layouts have a checkbox-based location filter
            Locator locationCheckbox = page.getByText("Location", new Page.GetByTextOptions().setExact(true));
            if (locationCheckbox.isVisible()) {
                click(locationCheckbox);
                sleep(1000);
                Locator option = locator("span:has-text('" + location + "')").first();
                if (option.isVisible()) {
                    click(option);
                }
            } else {
                log.warn("No location filter input found on page");
            }
        }
    }

    public int getTotalResultCount() {
        return jobCards.count();
    }
}
