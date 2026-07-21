
# ExecutionReportMailBodyBuilder.java – CODE EXPLANATION NOTES

## 1. Class Purpose

```java
public class ExecutionReportMailBodyBuilder {
```
Utility class dedicated to **HTML content creation** for execution emails.

---

## 2. Entry Method

```java
public static String buildMailBody(...)
```
- Static method for easy access
- Returns a ready-to-send HTML string

---

## 3. Method Parameters Explained

```java
int featurePassPercent,
int featureFailPercent,
int featureSkipPercent,
int scenarioPassPercent,
int scenarioFailPercent,
int scenarioSkipPercent
```

### Why percentages?
- Abstracts raw execution data
- Keeps UI logic independent of execution logic

---

## 4. Dynamic Color Logic

```java
String featureColor = featureFailPercent > 0 ? "red" : "green";
```

### Meaning
- Red → failures present
- Green → clean execution

---

## 5. Java Text Block Usage

```java
return """
<html>
<body>
...
""";
```

### Why?
- Clean multiline HTML
- No escaping of quotes
- Easy to modify layout

---

## 6. HTML Structure Breakdown

- `<h2>` → Main header
- `<h3>` → Section headers (Feature / Scenario)
- `<table>` → Execution metrics
- `<p>` → Report note

---

## 7. Formatting Injection

```java
.formatted(...)
```

### Purpose
- Injects dynamic values
- Keeps HTML template readable

---

## 8. Return Value

```java
return formattedHtml;
```

- Returned string is passed to `MailSender.sendMail()`

---

## 9. Error Handling (Design Choice)

- No try-catch needed
- Pure string builder
- Failures handled at caller level

---

## 10. One-Line Code Explanation

> "ExecutionReportMailBodyBuilder.buildMailBody constructs a dynamic, color-coded HTML email body using execution percentage inputs."
