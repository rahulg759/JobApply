
# ExecutionReportMailBodyBuilder.java – THEORY & INTERVIEW NOTES

## 1. Quick Revision
- Utility class to build HTML email body
- Focuses only on **email content generation**
- Uses Java Text Blocks (`"""`) for clean HTML
- Accepts percentage-based execution metrics
- Designed to be reused by mail-sending layer

---

## 2. What / Why / How

### What
`ExecutionReportMailBodyBuilder` is responsible for **creating the HTML body** of the automation execution email.

### Why
- Separates **content generation** from **email sending**
- Improves readability and maintainability
- Makes HTML changes independent of SMTP logic
- Follows Single Responsibility Principle (SRP)

### How
1. Accepts feature & scenario execution percentages
2. Decides color coding based on failure presence
3. Builds HTML using Java text blocks
4. Returns formatted HTML string

---

## 3. Execution Flow

```
Cucumber Execution
      ↓
Stats Calculated
      ↓
ExecutionReportMailBodyBuilder.buildMailBody()
      ↓
HTML Body Returned
      ↓
MailSender.sendMail()
```

---

## 4. Design Principles (Interview Focus)

### Single Responsibility Principle (SRP)
- This class only builds **mail body**
- No SMTP, no JSON parsing

### Reusability
- Can be reused for:
  - Daily regression reports
  - Smoke test reports
  - Nightly execution mails

### Maintainability
- HTML layout changes do not affect email logic

---

## 5. Why Use Percentages Instead of Counts?

- Management-friendly
- Quick health indicator
- Easy comparison across builds

---

## 6. Interview Questions & Answers

**Q1: Why separate mail body builder from mail sender?**  
A: To keep content generation and delivery logic decoupled and clean.

**Q2: Why use Java text blocks here?**  
A: Improves readability of HTML and avoids string concatenation mess.

**Q3: How do you highlight failures visually?**  
A: By dynamically applying red or green color based on failure percentage.

**Q4: Can this support more sections?**  
A: Yes, duration, environment, build URL, charts can be added easily.

---

## 7. Enhancements (If Asked)

- Add Jenkins build URL
- Add execution duration section
- Inline CSS for better styling
- Add SVG pie charts
- Support feature-wise breakdown

---

## 8. One-Line Interview Summary

> "ExecutionReportMailBodyBuilder generates a clean, color-coded HTML email body for automation execution reports using percentage-based metrics."
