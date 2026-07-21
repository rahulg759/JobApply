package com.naukri.automation.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class KeywordMatcher {

    private KeywordMatcher() {
    }

    public static KeywordMatchResult match(String text, List<String> keywords, int minimumScore) {
        if (text == null || text.isEmpty()) {
            return new KeywordMatchResult(0, List.of(), keywords, false);
        }

        String lowerText = text.toLowerCase();
        List<String> matched = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        for (String keyword : keywords) {
            String trimmedKeyword = keyword.trim();
            if (trimmedKeyword.isEmpty()) continue;

            Pattern pattern = Pattern.compile(
                    Pattern.quote(trimmedKeyword.toLowerCase()),
                    Pattern.CASE_INSENSITIVE
            );

            if (pattern.matcher(lowerText).find()) {
                matched.add(trimmedKeyword);
            } else {
                unmatched.add(trimmedKeyword);
            }
        }

        return new KeywordMatchResult(
                matched.size(),
                List.copyOf(matched),
                List.copyOf(unmatched),
                matched.size() >= minimumScore
        );
    }
}
