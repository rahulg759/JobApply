
# MailTrigger.java – Code Explanation Notes (Interview + Practical)

## 1. Purpose of This File
This document explains the **actual code of `MailTrigger.java` line by line**, mapping logic to real-world Jenkins + Cucumber usage.

Use this when:
- Interviewer opens your code
- You need to explain *why this line exists*
- You want confidence while discussing framework internals

---

## 2. Package & Imports Explanation

```java
package com.test.robust.mail;
```

### Why?
- Logical separation of **mail/reporting layer**
- Improves maintainability in large automation frameworks

---

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
```

### Why?
- Jackson library is used to **parse cucumber.json**
- `JsonNode` allows tree-style traversal (Feature → Scenario → Step)

---

```java
import jakarta.mail.*;
import jakarta.mail.internet.*;
```

### Why?
- Jakarta Mail API is used for **sending emails**
- Supports HTML content, attachments, authentication

---

## 3. Global Counters (Execution Statistics)

```java
static int featurePassed = 0;
static int scenarioFailed = 0;
static int stepsSkipped = 0;
```

### Why static?
- Single execution lifecycle
- Shared across methods
- Easy aggregation for summary & overview

### What is tracked?
- Feature-level status
- Scenario-level status
- Step-level status
- Duration

---

## 4. main() Method – Execution Entry Point

```java
public static void main(String[] args) {
```

### Why main?
- Executed as **standalone Java program**
- Triggered from Jenkins post-build step

---

### Jenkins Parameters

```java
String buildStatus = System.getProperty("build.status", "UNKNOWN");
String buildUrl = System.getProperty("build.url", "#");
```

### Why?
- Jenkins injects runtime build details
- Used to create clickable build links in email

---

### SMTP Credentials

```java
final String username = System.getenv("SMTP_USER");
final String password = System.getenv("SMTP_PASS");
```

### Why?
- Security best practice
- Jenkins credential binding compatible

---

## 5. Reading Cucumber JSON

```java
File jsonFile = new File("target/cucumber-parallel/cucumber.json");
```

### Why?
- This is where Cucumber writes execution results
- Parallel-safe consolidated report

---

```java
JsonNode root = mapper.readTree(jsonFile);
```

### What happens?
- Entire JSON converted into tree structure
- Enables nested traversal

---

## 6. Feature → Scenario → Step Traversal

```java
for (JsonNode feature : root) {
```

### Feature Loop
- Each element = one `.feature` file

---

```java
for (JsonNode scenario : feature.get("elements")) {
```

### Scenario Loop
- `elements` represent scenarios & backgrounds

---

```java
for (JsonNode step : scenario.get("steps")) {
```

### Step Loop
- Real execution happens here
- Step status drives scenario status

---

## 7. Step Status Evaluation

```java
String status = step.get("result").get("status").asText();
```

Possible values:
- passed
- failed
- skipped

---

```java
case "failed":
    stepFailed++;
    scenarioFailedFlag = true;
```

### Why scenarioFailedFlag?
- Even one failed step fails entire scenario

---

## 8. Scenario Status Decision

```java
if (scenarioFailedFlag) {
    scenarioFailed++;
} else if (scenarioSkippedFlag) {
    scenarioSkipped++;
} else {
    scenarioPassed++;
}
```

### Interview Rule:
> Scenario result is derived from **step-level execution**

---

## 9. Feature Status Decision

```java
if (fFailed > 0) {
    featureFailed++;
}
```

### Why?
- One failed scenario = failed feature
- Industry-standard reporting logic

---

## 10. HTML Report Building

```java
String htmlBody = "<html><body> ... </body></html>";
```

### Why HTML?
- Rich formatting
- Tables
- Color-coded statuses
- Manager friendly

---

## 11. Overview Section Code

```java
buildOverviewSection()
```

### Purpose
- Percentage-based health view
- Fast decision-making for management

---

## 12. Summary Section Code

```java
buildSummarySection()
```

### Purpose
- Absolute counts
- Execution duration
- Feature vs Scenario vs Step clarity

---

## 13. Duration Calculation Logic

```java
result.get("duration").asLong();
```

### Important
- Cucumber stores duration in **nanoseconds**
- Converted to milliseconds

---

## 14. Scenario Filtering (Critical Fix)

```java
if (scenario == null || scenario.trim().isEmpty()) {
    return "";
}
```

### Why?
- Prevents background / outline parent rows
- Avoids duplicate entries

---

## 15. Email Configuration

```java
props.put("mail.smtp.host", "smtp.gmail.com");
props.put("mail.smtp.port", "587");
```

### Why?
- TLS enabled secure communication
- Industry standard Gmail SMTP

---

## 16. Multipart Email

```java
MimeMultipart multipart = new MimeMultipart();
```

### Supports:
- HTML body
- ZIP attachment

---

## 17. Attachment Handling

```java
File zip = new File("target/cucumber-reports/cucumber-html-reports.zip");
```

### Why?
- Allows deep-dive if needed
- Optional attachment logic

---

## 18. Jenkins Integration Command

```bash
java -cp target/classes com.test.robust.mail.MailTrigger  -Dbuild.status=SUCCESS  -Dbuild.url=$BUILD_URL
```

---

## 19. Common Interview Questions (Code Focus)

**Q: Why static counters instead of objects?**  
A: Simplicity for single-run execution; can be refactored to models.

**Q: How do you avoid duplicate scenarios?**  
A: Background and empty scenarios are filtered explicitly.

**Q: How do you calculate execution time?**  
A: By aggregating step-level duration from cucumber.json.

---

## 20. One-Line Code Explanation (Power Line)

> "MailTrigger.java reads Cucumber JSON results, evaluates execution status at step, scenario, and feature level, builds an HTML report, and sends it via SMTP as part of Jenkins automation."
