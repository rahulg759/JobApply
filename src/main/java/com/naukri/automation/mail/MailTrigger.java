package com.naukri.automation.mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.Properties;

import jakarta.mail.*;

public class MailTrigger {

    // ===== Feature level counters (for overview %) =====
    static int featurePassed = 0;
    static int featureFailed = 0;
    static int featureSkipped = 0;
    static int featurePending = 0;
    static int featureUndefined = 0;

    // ===== Counters =====
    static int featureCount = 0;

    static int scenarioPassed = 0;
    static int scenarioFailed = 0;
    static int scenarioSkipped = 0;
    static int scenarioPending = 0;
    static int scenarioUndefined = 0;
    static long featureDurationMs;
    static long scenarioDurationMs;

    static int stepsPassed = 0;
    static int stepsFailed = 0;
    static int stepsSkipped = 0;
    static int stepsPending = 0;
    static int stepsUndefined = 0;
    static int totalSteps = 0;
    static long stepDurationMs = 0;

    static long featureStartTime;
    public static long stepStartTime;

    static long featurePassedDurationMs = 0;
    static long featureFailedDurationMs = 0;
    static long featureSkippedDurationMs = 0;
    static long featurePendingDurationMs = 0;
    static long featureUndefinedDurationMs = 0;

    static long scenarioPassedDurationMs = 0;
    static long scenarioFailedDurationMs = 0;
    static long scenarioSkippedDurationMs = 0;


    public static void main(String[] args) {

        String buildStatus = System.getProperty("build.status", "UNKNOWN");
        String buildUrl = System.getProperty("build.url", "#");
        String headerColor = System.getProperty("header.color", "#2ecc71");

        final String username = System.getenv("SMTP_USER");
        final String password = System.getenv("SMTP_PASS");

        String featureRows = "";
        String scenarioRows = "";

        try {
            // ================= READ CUCUMBER JSON =================
            ObjectMapper mapper = new ObjectMapper();
            File jsonFile = new File("target/cucumber-reports/cucumber.json");

            if (!jsonFile.exists()) {
                System.out.println("❌ cucumber.json not found");
                return;
            }

            JsonNode root = mapper.readTree(jsonFile);

            for (JsonNode feature : root) {
                featureCount++;
                String featureName = feature.get("name").asText();

                int fPassed = 0, fFailed = 0, fSkipped = 0;

                for (JsonNode scenario : feature.get("elements")) {

                    // 🔥 FIX 1: Ignore Scenario Outline parent
                    if (scenario.has("type")
                            && "scenario_outline".equalsIgnoreCase(scenario.get("type").asText())) {
                        continue;
                    }

                    // 🔥 FIX 2: Ignore Background
                    if (scenario.has("keyword")
                            && "Background".equalsIgnoreCase(scenario.get("keyword").asText())) {
                        continue;
                    }

                    boolean scenarioFailedFlag = false;
                    boolean scenarioSkippedFlag = false;

                    for (JsonNode step : scenario.get("steps")) {
                        String status = step.get("result").get("status").asText();

                        switch (status) {
                            case "passed":
                                stepsPassed++;
                                break;
                            case "failed":
                                stepsFailed++;
                                scenarioFailedFlag = true;
                                break;
                            case "skipped":
                                stepsSkipped++;
                                scenarioSkippedFlag = true;
                                break;
                        }
                    }

                    // ✅ Scenario count (NOW CORRECT)
                    if (scenarioFailedFlag) {
                        fFailed++;
                        scenarioFailed++;
                    } else if (scenarioSkippedFlag) {
                        fSkipped++;
                        scenarioSkipped++;
                    } else {
                        fPassed++;
                        scenarioPassed++;
                    }

                    scenarioRows += buildScenarioRow(
                            featureName,
                            scenario.get("name").asText(),
                            scenarioFailedFlag ? "FAILED" : "PASSED"
                    );
                }

                // ✅ Feature count logic (unchanged)
                if (fFailed > 0) {
                    featureFailed++;
                } else if (fSkipped > 0) {
                    featureSkipped++;
                } else {
                    featurePassed++;
                }

                featureRows += buildFeatureRow(featureName, fPassed, fFailed, fSkipped);
            }


            // ================= MAIL CONFIG =================
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse("automationtechiesindia@gmail.com")
            );

            message.setSubject("Jenkins | Automation Execution | " + buildStatus);

            // ================= HTML BODY =================
            String htmlBody =
                    "<html><body style='font-family:Arial,sans-serif;'>" +

                            "<h2 style='background:" + headerColor + ";color:white;padding:12px;'>" +
                            "Automation Execution Summary - " + buildStatus +
                            "</h2>" +

                            buildOverviewSection() +
                            buildSummarySection() +

                            "<h3 style='background:#d6befe;padding:8px;'>Feature Status</h3>" +

                            "<table border='1' cellpadding='0' cellspacing='0' " +
                            "style='border-collapse:collapse;table-layout:fixed;width:auto;'>" +

                            "<tr style='background:#1f3a5f;color:white;'>" +
                            "<th style='padding:6px 10px;'>Feature</th>" +
                            "<th style='padding:6px 10px;'>Passed</th>" +
                            "<th style='padding:6px 10px;'>Failed</th>" +
                            "<th style='padding:6px 10px;'>Skipped</th>" +
                            "<th style='padding:6px 10px;'>Status</th>" +
                            "</tr>" +

                            featureRows +

                            "</table><br/>" +


                            "<h3 style='background:#d6befe;padding:8px;'>Scenario Status</h3>" +

                            "<table border='1' cellpadding='0' cellspacing='0' " +
                            "style='border-collapse:collapse;table-layout:fixed;width:auto;'>" +

                            "<tr style='background:#1f3a5f;color:white;'>" +
                            "<th style='padding:6px 10px;'>Feature</th>" +
                            "<th style='padding:6px 10px;'>Scenario</th>" +
                            "<th style='padding:6px 10px;'>Status</th>" +
                            "</tr>" +

                            scenarioRows +

                            "</table><br/>" +

                            "<p>🔗 <a href='" + buildUrl + "'>View Jenkins Build</a></p>" +
                            "<p style='font-size:12px;color:gray;'>This is an automated Jenkins email.</p>" +
                            "</body></html>";

            // ================= MULTIPART =================
            Multipart multipart = new MimeMultipart();

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html");
            multipart.addBodyPart(htmlPart);

            File zip = new File("target/cucumber-reports/cucumber-html-reports.zip");
            if (zip.exists()) {
                MimeBodyPart attachment = new MimeBodyPart();
                attachment.attachFile(zip);
                multipart.addBodyPart(attachment);
            }

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("✅ Email sent successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= HTML SECTIONS =================

    private static String buildOverviewSection() {

        // ===== TOTAL COUNTS =====
        int totalFeatures = featurePassed + featureFailed + featureSkipped;
        int totalSteps = stepsPassed + stepsFailed + stepsSkipped;

        // ===== FEATURE PERCENTAGE =====
        int featurePassPct = totalFeatures == 0 ? 0 : (featurePassed * 100 / totalFeatures);
        int featureFailPct = totalFeatures == 0 ? 0 : (featureFailed * 100 / totalFeatures);
        int featureSkipPct = totalFeatures == 0 ? 0 : (featureSkipped * 100 / totalFeatures);

        // ===== STEP PERCENTAGE =====
        int stepPassPct = totalSteps == 0 ? 0 : (stepsPassed * 100 / totalSteps);
        int stepFailPct = totalSteps == 0 ? 0 : (stepsFailed * 100 / totalSteps);
        int stepSkipPct = totalSteps == 0 ? 0 : (stepsSkipped * 100 / totalSteps);

        int passPct = totalSteps == 0 ? 0 : (stepsPassed * 100 / totalSteps);
        int failPct = totalSteps == 0 ? 0 : (stepsFailed * 100 / totalSteps);
        int skipPct = totalSteps == 0 ? 0 : (stepsSkipped * 100 / totalSteps);

        return "<h3 style='background:#d6befe;padding:8px;'>Overview Chart</h3>" +
                "<table border='1' cellpadding='8' cellspacing='0'>" +
                "<tr style='background:#1f3a5f;color:white;'>" +
                "<th>Type</th>" +
                "<th>Passed %</th>" +
                "<th>Failed %</th>" +
                "<th>Skipped %</th>" +
                "</tr>" +

                "<tr align='center'><td><b>Features</b></td>" +
                "<td style='background:#26e626;color:black;'>" + featurePassPct + "%</td>" +
                "<td style='background:#fa222e;color:black;'>" + featureFailPct + "%</td>" +
                "<td style='background:#f6d61f;color:black;'>" + featureSkipPct + "%</td></tr>" +

                "<tr align='center'><td><b>Scenario</b></td>" +
                "<td style='background:#26e626;color:black;'>" + stepPassPct + "%</td>" +
                "<td style='background:#fa222e;color:black;'>" + stepFailPct + "%</td>" +
                "<td style='background:#f6d61f;color:black;'>" + stepSkipPct + "%</td></tr>" +
                "</table><br/>";
    }

    private static String buildSummarySection() {

        int totalScenarios =
                scenarioPassed + scenarioFailed + scenarioSkipped + scenarioPending + scenarioUndefined;

        int totalFeatures =
                featurePassed + featureFailed + featureSkipped + featurePending + featureUndefined;

        int totalSteps =
                stepsPassed + stepsFailed + stepsSkipped + stepsPending + stepsUndefined;

       // long totalExecutionMs = featurePassedDurationMs + featureFailedDurationMs + featureSkippedDurationMs + featurePendingDurationMs + featureUndefinedDurationMs;

        long totalExecutionMs = readDurationFromCucumberJson();

        return "<h3 style='background:#d6befe;padding:8px;'>Summary</h3>" +

                "<table border='1' cellpadding='8' cellspacing='0'>" +

                "<tr style='background:#1f3a5f;color:white;'>" +
                "<th>Type</th>" +
                "<th>Passed</th>" +
                "<th>Failed</th>" +
                "<th>Skipped</th>" +
                "<th>Pending</th>" +
                "<th>Undefined</th>" +
                "<th>Total</th>" +
                /*"<th>Duration</th>" +*/
                "</tr>" +

                // -------- Features Row --------
                "<tr align='center'><td><b>Features</b></td>" +
                "<td style='background:#26e626;color:black;'>" + featurePassed + "</td>" +
                "<td style='background:#fa222e;color:black;'>" + featureFailed + "</td>" +
                "<td style='background:#f6d61f;color:black;'>" + featureSkipped + "</td>" +
                "<td style='background:#ff9800;color:black;'>" + featurePending + "</td>" +
                "<td style='background:#eae8dd;color:black;'>" + featureUndefined + "</td>" +
                "<td><b>" + totalFeatures + "</b></td>" +
                /*"<td><b>" + formatDuration(totalFeatures) + "</b></td>" +*/
                "</tr>" +

                // -------- Scenarios Row --------
                "<tr align='center'><td><b>Scenarios</b></td>" +
                "<td style='background:#26e626;color:black;'>" + scenarioPassed + "</td>" +
                "<td style='background:#fa222e;color:black;'>" + scenarioFailed + "</td>" +
                "<td style='background:#f6d61f;color:black;'>" + scenarioSkipped + "</td>" +
                "<td style='background:#ff9800;color:black;'>" + scenarioPending + "</td>" +
                "<td style='background:#eae8dd;color:black;'>" + scenarioUndefined + "</td>" +
                "<td><b>" + totalScenarios + "</b></td>" +
                /*"<td><b>" + formatDuration(totalScenarios) + "</b></td></tr>" +*/

                // -------- Steps Row --------
                "<tr align='center'><td><b>Steps</b></td>" +
                "<td style='background:#26e626;color:black;'>" + stepsPassed + "</td>" +
                "<td style='background:#fa222e;color:black;'>" + stepsFailed + "</td>" +
                "<td style='background:#f6d61f;color:black;'>" + stepsSkipped + "</td>" +
                "<td style='background:#ff9800;color:black;'>" + stepsPending + "</td>" +
                "<td style='background:#eae8dd;color:black;'>" + stepsUndefined + "</td>" +
                "<td><b>" + totalSteps + "</b></td>" +
                /* "<td><b>" + formatDuration(totalSteps) + "</b></td></tr>" +*/

                // -------- Duration Breakup --------
                "<tr align='center'><td><b>Duration Breakup</b></td>" +
                "<td style='background:#26e626;color:black;'>" + formatDuration(featurePassedDurationMs) + "</td>" +
                "<td style='background:#fa222e;color:black;'>" + formatDuration(featureFailedDurationMs) + "</td>" +
                "<td style='background:#f6d61f;color:black;'>" + formatDuration(featureSkippedDurationMs) + "</td>" +
                "<td style='background:#ff9800;color:black;'>" + formatDuration(featurePendingDurationMs) + "</td>" +
                "<td style='background:#eae8dd;color:black;'>" + formatDuration(featureUndefinedDurationMs) + "</td>" +
                "<td><b>" + formatDuration(totalExecutionMs) + "</b></td>" +
                "</tr>" +

                "</table><br/>";
    }


    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long hh = seconds / 3600;
        long mm = (seconds % 3600) / 60;
        long ss = seconds % 60;

        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    /*public static long readDurationFromCucumberJson() {

        long totalDurationNs = 0;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File("target/cucumber.json"));

            for (JsonNode feature : root) {
                JsonNode elements = feature.get("elements");
                if (elements == null) continue;

                for (JsonNode scenario : elements) {
                    JsonNode steps = scenario.get("steps");
                    if (steps == null) continue;

                    for (JsonNode step : steps) {
                        JsonNode result = step.get("result");
                        if (result != null && result.has("duration")) {
                            totalDurationNs += result.get("duration").asLong();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert nanoseconds → milliseconds
        return totalDurationNs / 1_000_000;
    }*/

    /*public static long readDurationFromCucumberJson() {

        long totalDurationNs = 0;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File("target/cucumber.json"));

            for (JsonNode feature : root) {

                long featureDurationNs = 0;
                boolean featureFailed = false;
                boolean featureSkipped = false;

                JsonNode elements = feature.get("elements");
                if (elements == null) continue;

                for (JsonNode scenario : elements) {

                    boolean scenarioFailed = false;
                    boolean scenarioSkipped = false;
                    long scenarioDurationNs = 0;

                    JsonNode steps = scenario.get("steps");
                    if (steps == null) continue;

                    for (JsonNode step : steps) {
                        JsonNode result = step.get("result");
                        if (result == null) continue;

                        String status = result.get("status").asText();
                        long durationNs = result.has("duration")
                                ? result.get("duration").asLong()
                                : 0;

                        scenarioDurationNs += durationNs;
                        totalDurationNs += durationNs;

                        if ("failed".equals(status)) {
                            scenarioFailed = true;
                        } else if ("skipped".equals(status)) {
                            scenarioSkipped = true;
                        }
                    }

                    featureDurationNs += scenarioDurationNs;

                    // SCENARIO → FEATURE decision
                    if (scenarioFailed) {
                        featureFailed = true;
                    } else if (scenarioSkipped) {
                        featureSkipped = true;
                    }
                }

                // FEATURE DURATION BREAKUP (⭐ MAIN FIX ⭐)
                if (featureFailed) {
                    featureFailedDurationMs += featureDurationNs / 1_000_000;
                } else if (featureSkipped) {
                    featureSkippedDurationMs += featureDurationNs / 1_000_000;
                } else {
                    featurePassedDurationMs += featureDurationNs / 1_000_000;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // total execution time (used already by you)
        return totalDurationNs / 1_000_000;
    }*/

   /* public static long readDurationFromCucumberJson() {

        // 🔁 RESET (VERY IMPORTANT)
        featurePassedDurationMs = 0;
        featureFailedDurationMs = 0;
        featureSkippedDurationMs = 0;
        featurePendingDurationMs = 0;
        featureUndefinedDurationMs = 0;

        scenarioPassedDurationMs = 0;
        scenarioFailedDurationMs = 0;
        scenarioSkippedDurationMs = 0;

        long totalDurationNs = 0;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File("target/cucumber.json"));

            for (JsonNode feature : root) {

                JsonNode elements = feature.get("elements");
                if (elements == null) continue;

                for (JsonNode scenario : elements) {

                    boolean scenarioFailed = false;
                    boolean scenarioSkipped = false;
                    long scenarioDurationNs = 0;

                    JsonNode steps = scenario.get("steps");
                    if (steps == null) continue;

                    for (JsonNode step : steps) {
                        JsonNode result = step.get("result");
                        if (result == null) continue;

                        String status = result.get("status").asText();
                        long durationNs = result.has("duration")
                                ? result.get("duration").asLong()
                                : 0;

                        scenarioDurationNs += durationNs;
                        totalDurationNs += durationNs;

                        if ("failed".equals(status)) {
                            scenarioFailed = true;
                        } else if ("skipped".equals(status)) {
                            scenarioSkipped = true;
                        }
                    }

                    long scenarioDurationMs = scenarioDurationNs / 1_000_000;

                    // ✅ SCENARIO-LEVEL BREAKUP (KEY FIX)
                    if (scenarioFailed) {
                        scenarioFailedDurationMs += scenarioDurationMs;
                        featureFailedDurationMs += scenarioDurationMs;
                    } else if (scenarioSkipped) {
                        scenarioSkippedDurationMs += scenarioDurationMs;
                        featureSkippedDurationMs += scenarioDurationMs;
                    } else {
                        scenarioPassedDurationMs += scenarioDurationMs;
                        featurePassedDurationMs += scenarioDurationMs;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalDurationNs / 1_000_000;
    }*/

    public static long readDurationFromCucumberJson() {

        // 🔁 RESET
        featurePassedDurationMs = 0;
        featureFailedDurationMs = 0;
        featureSkippedDurationMs = 0;

        long totalDurationNs = 0;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(
                    new File("target/cucumber-reports/cucumber.json"));

            for (JsonNode feature : root) {

                JsonNode elements = feature.get("elements");
                if (elements == null) continue;

                for (JsonNode scenario : elements) {

                    // Same filters as count logic
                    if (scenario.has("keyword")
                            && "Background".equalsIgnoreCase(scenario.get("keyword").asText())) {
                        continue;
                    }

                    boolean scenarioFailed = false;
                    boolean scenarioSkipped = false;
                    long scenarioDurationNs = 0;

                    JsonNode steps = scenario.get("steps");
                    if (steps == null) continue;

                    for (JsonNode step : steps) {
                        JsonNode result = step.get("result");
                        if (result == null) continue;

                        String status = result.get("status").asText();
                        long durationNs = result.has("duration")
                                ? result.get("duration").asLong()
                                : 0;

                        scenarioDurationNs += durationNs;
                        totalDurationNs += durationNs;

                        if ("failed".equals(status)) {
                            scenarioFailed = true;
                        } else if ("skipped".equals(status)) {
                            scenarioSkipped = true;
                        }
                        /*String status = result.get("status").asText();

                        long durationNs = 0;
                        if (result.has("duration")) {
                            durationNs = result.get("duration").asLong();
                        } else if (step.has("duration")) {
                            durationNs = step.get("duration").asLong();
                        }

                        scenarioDurationNs += durationNs;
                        totalDurationNs += durationNs;

                        if ("failed".equals(status)) {
                            scenarioFailed = true;
                        } else if ("skipped".equals(status)) {
                            scenarioSkipped = true;
                        }*/
                    }

                    long scenarioDurationMs = scenarioDurationNs / 1_000_000;

                    // 🔥 SCENARIO-LEVEL BUCKETING (KEY FIX)
                    if (scenarioFailed) {
                        featureFailedDurationMs += scenarioDurationMs;
                    } else if (scenarioSkipped) {
                        featureSkippedDurationMs += scenarioDurationMs;
                    } else {
                        featurePassedDurationMs += scenarioDurationMs;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalDurationNs / 1_000_000;
    }





    private static String buildFeatureRow(String name, int p, int f, int s) {

        String status = f > 0 ? "FAILED" : "PASSED";
        String statusColor = "FAILED".equals(status) ? "red" : "green";

        return "<tr>" +

                // Feature name (normal text)
                "<td style='padding:6px 10px;'>" + name + "</td>" +

                // Passed
                "<td style='background:#26e626;color:black;" +
                "text-align:center;padding:6px 10px;'>" + p + "</td>" +

                // Failed
                "<td style='background:#fa222e;color:black;" +
                "text-align:center;padding:6px 10px;'>" + f + "</td>" +

                // Skipped
                "<td style='background:#f6d61f;color:black;" +
                "text-align:center;padding:6px 10px;'>" + s + "</td>" +

                // Status
                "<td style='color:" + statusColor + ";" +
                "font-weight:bold;text-align:center;padding:6px 10px;'>" +
                status + "</td>" +

                "</tr>";
    }

    private static String buildScenarioRow(String feature, String scenario, String status) {

        // 🔥 MOST IMPORTANT FIX
        // Skip Background / empty / outline-parent rows
        if (scenario == null || scenario.trim().isEmpty()) {
            return "";
        }

        String lowerScenario = scenario.toLowerCase();

        // Optional: skip background explicitly (extra safety)
        if (lowerScenario.startsWith("background")) {
            return "";
        }

        String statusColor;
        if ("FAILED".equalsIgnoreCase(status)) {
            statusColor = "red";
        } else if ("SKIPPED".equalsIgnoreCase(status)) {
            statusColor = "#f6d61f"; // yellow
        } else {
            statusColor = "green";
        }

        return "<tr>" +

                // Feature
                "<td style='padding:6px 10px;'>" + feature + "</td>" +

                // Scenario (REAL scenario only)
                "<td style='padding:6px 10px;'>" + scenario + "</td>" +

                // Status
                "<td style='color:" + statusColor + ";" +
                "font-weight:bold;text-align:center;padding:6px 10px;'>" +
                status +
                "</td>" +

                "</tr>";
    }

}