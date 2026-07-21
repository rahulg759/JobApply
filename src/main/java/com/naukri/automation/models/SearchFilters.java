package com.naukri.automation.models;

import java.util.List;

public record SearchFilters(
        String keywords,
        String location,
        List<String> experienceLevels,
        String datePosted,
        String company
) {
}
