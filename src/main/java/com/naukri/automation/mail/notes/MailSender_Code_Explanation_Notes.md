
# MailSender.java – CODE EXPLANATION NOTES

## 1. Class Purpose

```java
public class MailSender {
```
Utility class responsible **only for sending emails**.

---

## 2. Entry Method

```java
public static void sendMail(String htmlBody, String buildStatus)
```
- Static for easy access
- Accepts:
  - HTML email body
  - Jenkins build status

---

## 3. Reading SMTP Credentials

```java
String from = System.getenv("SMTP_USER");
String appPassword = System.getenv("SMTP_PASS");
```

### Why?
- Secure secret handling
- Jenkins credential binding compatible

---

## 4. Fail-Fast Validation

```java
if (from == null || appPassword == null) {
    throw new RuntimeException(...);
}
```

### Why?
- Avoid silent email failures
- Immediate pipeline feedback

---

## 5. SMTP Properties Configuration

```java
props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");
props.put("mail.smtp.host", "smtp.gmail.com");
props.put("mail.smtp.port", "587");
```

---

## 6. Session Creation

```java
Session session = Session.getInstance(props, new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(from, appPassword);
    }
});
```

---

## 7. Multipart Email

```java
Multipart multipart = new MimeMultipart();
```

---

## 8. Attachment Handling

```java
File reportZip = new File("target/cucumber-reports/cucumber-html-reports.zip");
```

---

## 9. Sending Email

```java
Transport.send(message);
```

---

## 10. One-Line Code Explanation

> "MailSender.sendMail configures an authenticated SMTP session, builds a multipart HTML email with optional attachments, and sends it securely from Jenkins."
