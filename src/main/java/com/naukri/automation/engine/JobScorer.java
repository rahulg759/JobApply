package com.naukri.automation.engine;

import com.naukri.automation.models.JobListing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class JobScorer {

    private static final Logger log = LogManager.getLogger(JobScorer.class);
    private static final List<String> TITLE_BONUS_KEYWORDS = List.of(
            "qa", "quality assurance", "automation", "sdet", "test", "testing", "software development engineer in test"
    );

    private JobScorer() {
    }

    public static JobListing score(JobListing listing, String description, List<String> keywords, int minimumScore) {
        if (description == null || description.isEmpty()) {
            log.warn("Empty description for job: {} at {}", listing.title(), listing.company());
            return new JobListing(
                    listing.title(), listing.company(), listing.location(),
                    listing.experience(), listing.description(), listing.skills(),
                    listing.easyApply(), 0, List.of()
            );
        }

        KeywordMatchResult result = KeywordMatcher.match(description, keywords, minimumScore);

        int totalScore = result.score();

        // Bonus points if title contains relevant QA/testing keywords
        String titleLower = listing.title().toLowerCase();
        for (String bonusWord : TITLE_BONUS_KEYWORDS) {
            if (titleLower.contains(bonusWord)) {
                totalScore++;
                break; // max 1 bonus point
            }
        }

        List<String> allMatched = new ArrayList<>(result.matchedKeywords());
        if (totalScore > result.score()) {
            allMatched.add("(title_bonus)");
        }

        log.info("Job '{}' at '{}': matched {}/{} keywords, total score: {}",
                listing.title(), listing.company(), result.score(), keywords.size(), totalScore);

        return new JobListing(
                listing.title(), listing.company(), listing.location(),
                listing.experience(), listing.description(), listing.skills(),
                listing.easyApply(), totalScore, allMatched
        );
    }
}
