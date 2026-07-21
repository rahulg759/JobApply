
# MailTrigger.java – Interview-Ready Notes

## 1. Quick Revision (One-Look)
- Custom Java utility for Jenkins email reporting
- Parses `cucumber.json` using Jackson
- Aggregates Feature / Scenario / Step results
- Builds rich HTML email (tables + colors)
- Sends mail via Gmail SMTP (Jakarta Mail)
- Attaches Cucumber HTML report ZIP

---

## 2. What / Why / How

### What
`MailTrigger` converts Cucumber execution output into a **management-friendly HTML email report**.

### Why
- Jenkins default mails are plain text
- Stakeholders want **quick visibility** without opening Jenkins
- Need **custom logic** (feature-level status, percentages, duration)

### How
1. Read `cucumber.json`
2. Traverse Feature → Scenario → Steps
3. Calculate pass/fail/skip counts
4. Compute percentages & duration
5. Build HTML tables
6. Send email via SMTP

---

## 3. Execution Flow (Interview Explanation)

```
Cucumber Tests Run
        ↓
cucumber.json Generated
        ↓
MailTrigger.java Reads JSON
        ↓
Calculates Feature / Scenario / Step Stats
        ↓
Builds HTML Email
        ↓
Sends Email via SMTP (Jenkins)
```

---

## 4. Architecture Diagram

```
┌──────────────┐
│ Jenkins Job  │
│ (Maven Test) │
└──────┬───────┘
       │ generates
       ▼
┌──────────────────────┐
│ cucumber.json        │
└──────┬───────────────┘
       │ read by
       ▼
┌──────────────────────┐
│ MailTrigger.java     │
│  - JSON Parser       │
│  - Stats Calculator  │
│  - HTML Builder      │
│  - SMTP Sender       │
└──────┬───────────────┘
       │ sends
       ▼
┌──────────────────────┐
│ HTML Email + ZIP     │
│ (Managers / Team)    │
└──────────────────────┘
```

---

## 5. Core Logic Explained

### Scenario Status Logic
- If **any step fails** → Scenario FAILED
- Else if skipped → Scenario SKIPPED
- Else → Scenario PASSED

### Feature Status Logic
- If **any scenario fails** → Feature FAILED
- Else if skipped → Feature SKIPPED
- Else → Feature PASSED

### Duration Calculation
- Cucumber stores step duration in **nanoseconds**
- Converted to milliseconds and formatted as `HH:MM:SS`

---

## 6. Important Code Concepts Referenced

### JSON Parsing
- Uses `ObjectMapper` and `JsonNode`
- Traverses Feature → elements → steps → result.status

### Email Sending
- Jakarta Mail API
- SMTP: `smtp.gmail.com`, port `587`
- TLS + Authentication
- Multipart email (HTML body + ZIP attachment)

### Security
- SMTP username/password read from **environment variables**
- Jenkins credentials compatible

---

## 7. Common Interview Questions & Answers

**Q1: Why parse cucumber.json instead of using plugins?**  
A: Plugins are generic. Parsing JSON gives full control over logic, format, branding, and metrics.

**Q2: How do you decide scenario status?**  
A: Scenario status is derived from step-level execution. Any failed step marks the scenario failed.

**Q3: Why environment variables for SMTP credentials?**  
A: For security and easy integration with Jenkins credential management.

**Q4: How do you avoid background scenarios appearing in reports?**  
A: Background and empty scenario entries are explicitly filtered while building scenario rows.

**Q5: Can this handle parallel execution?**  
A: Yes. It reads the consolidated Cucumber JSON generated after parallel execution.

---

## 8. Refactoring to OOP Design (Advanced)

### Current Issue
- Single large class
- Multiple responsibilities

### Recommended Design

```
mail/
 ├── MailTrigger.java
 ├── parser/
 │    └── CucumberJsonParser.java
 ├── model/
 │    └── ExecutionStats.java
 ├── service/
 │    ├── ReportBuilder.java
 │    └── EmailService.java
```

### Benefits
- Clean separation of concerns
- Easy unit testing
- Better scalability
- Strong interview impression

---

## 9. Optimizations for Large Test Suites

- Use `StringBuilder` instead of string concatenation
- Avoid static counters; use model objects
- Stream JSON for very large files
- Run report generation as Jenkins post-build step

---

## 10. Enhancement Ideas

- Inline SVG pie charts in email
- Trend comparison across Jenkins builds
- Slack / Teams notifications
- Store execution metrics in database

---

## 11. One-Line Interview Summary (Must Remember)

> "MailTrigger is a custom Jenkins reporting utility that parses Cucumber JSON, aggregates execution metrics, generates rich HTML summaries, and sends automated email notifications with attachments."
