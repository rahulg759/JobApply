
# MailSender.java – THEORY & INTERVIEW NOTES

## 1. Quick Revision
- Utility class for sending HTML emails from Jenkins
- Uses Jakarta Mail API
- Reads SMTP credentials from environment variables
- Supports HTML body + ZIP attachment
- Designed as reusable service class

---

## 2. What / Why / How

### What
`MailSender` is a **mail service utility** that sends execution reports via email after Jenkins builds.

### Why
- Decouples email logic from reporting logic
- Improves reusability and maintainability
- Follows Single Responsibility Principle
- Secure handling of SMTP credentials

### How
1. Jenkins injects SMTP credentials as environment variables
2. Method receives HTML body + build status
3. Configures SMTP session
4. Builds multipart email (HTML + attachment)
5. Sends email via Gmail SMTP

---

## 3. Execution Flow

```
Jenkins Build
     ↓
Cucumber / Automation Execution
     ↓
HTML Report Generated
     ↓
MailSender.sendMail()
     ↓
SMTP Server (Gmail)
     ↓
Email Delivered
```

---

## 4. Design Highlights (Interview Focus)

### SRP (Single Responsibility Principle)
- MailSender only sends emails
- No JSON parsing, no reporting logic

### Security
- No hardcoded username/password
- Uses Jenkins credential binding

### Reusability
- Can be reused for:
  - Failure notifications
  - Smoke reports
  - Nightly build reports

---

## 5. Why Environment Variables?

- Avoids hardcoding secrets
- Jenkins-friendly
- Secure for production pipelines

---

## 6. Interview Questions & Answers

**Q1: Why separate MailSender from MailTrigger?**  
A: To follow clean architecture and SRP. Reporting and email delivery should be independent.

**Q2: Why Gmail SMTP with TLS?**  
A: Secure, widely supported, easy Jenkins integration.

**Q3: What happens if SMTP creds are missing?**  
A: Runtime exception is thrown to fail fast and avoid silent issues.

**Q4: Can this be extended?**  
A: Yes, CC/BCC, multiple recipients, retry logic can be added.

---

## 7. Enhancements (If Asked)

- Multiple recipients from Jenkins param
- CC / BCC support
- Retry on SMTP failure
- Configurable attachment paths
- Support for non-Gmail SMTP

---

## 8. One-Line Interview Summary

> "MailSender is a reusable email service that securely sends HTML execution reports with attachments from Jenkins using Jakarta Mail."
