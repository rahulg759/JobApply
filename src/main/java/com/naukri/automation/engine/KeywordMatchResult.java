package com.naukri.automation.engine;

import java.util.List;

public record KeywordMatchResult(
        int score,
        List<String> matchedKeywords,
        List<String> unmatchedKeywords,
        boolean isQualified
) {
}
