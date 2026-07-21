package com.naukri.automation.mail;

public class ExecutionReportMailBodyBuilder {

    public static String buildMailBody(
            int featurePassPercent,
            int featureFailPercent,
            int featureSkipPercent,
            int scenarioPassPercent,
            int scenarioFailPercent,
            int scenarioSkipPercent
    ) {

        String featureColor = featureFailPercent > 0 ? "red" : "green";
        String scenarioColor = scenarioFailPercent > 0 ? "red" : "green";

        return """
        <html>
        <body style="font-family:Arial;">
            <h2>Automation Execution Summary</h2>

            <h3 style="color:%s;">Feature Results</h3>
            <table border="1" cellpadding="6">
                <tr><th>Passed</th><th>Failed</th><th>Skipped</th></tr>
                <tr>
                    <td>%d%%</td>
                    <td>%d%%</td>
                    <td>%d%%</td>
                </tr>
            </table>

            <h3 style="color:%s;">Scenario Results</h3>
            <table border="1" cellpadding="6">
                <tr><th>Passed</th><th>Failed</th><th>Skipped</th></tr>
                <tr>
                    <td>%d%%</td>
                    <td>%d%%</td>
                    <td>%d%%</td>
                </tr>
            </table>

            <p>Detailed Cucumber report is attached.</p>
        </body>
        </html>
        """
        .formatted(
                featureColor,
                featurePassPercent,
                featureFailPercent,
                featureSkipPercent,
                scenarioColor,
                scenarioPassPercent,
                scenarioFailPercent,
                scenarioSkipPercent
        );
    }
}
