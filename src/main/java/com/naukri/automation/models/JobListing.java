package com.naukri.automation.models;

import java.util.List;

public record JobListing(
        String title,
        String company,
        String location,
        String experience,
        String description,
        List<String> skills,
        boolean easyApply,
        int matchScore,
        List<String> matchedKeywords
) {
    public boolean isQualified(int minimumScore) {
        return matchScore >= minimumScore;
    }
}
