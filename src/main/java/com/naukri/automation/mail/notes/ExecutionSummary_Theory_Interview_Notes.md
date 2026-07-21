
# ExecutionSummary.java – THEORY & INTERVIEW NOTES

## 1. Quick Revision
- Plain Java model (POJO)
- Holds aggregated execution results
- Represents Feature & Scenario summary
- Used for data transfer between layers
- No business logic inside

---

## 2. What / Why / How

### What
`ExecutionSummary` is a **data holder class** that stores aggregated execution counts for features and scenarios.

### Why
- Avoids using multiple static variables
- Makes reporting logic cleaner and testable
- Follows Object-Oriented Design principles
- Improves readability and maintainability

### How
1. Execution results are calculated elsewhere
2. Values are set using setters
3. Reporting / mail builder reads values via getters

---

## 3. Execution Flow

```
Cucumber JSON Parsing
        ↓
Counts Calculated
        ↓
ExecutionSummary Object
        ↓
Report Builder / Mail Body Builder
        ↓
Email Sent
```

---

## 4. Design Principles (Interview Focus)

### POJO (Plain Old Java Object)
- No framework dependency
- No annotations
- Simple getters & setters

### Encapsulation
- Fields are private
- Access controlled via getters/setters

### Separation of Concerns
- Holds data only
- No calculation or formatting logic

---

## 5. Why Use a Model Class Instead of Static Counters?

- Cleaner architecture
- Thread-safe design (future parallel use)
- Easy to extend (add steps, duration, env)
- Unit-test friendly

---

## 6. Interview Questions & Answers

**Q1: Why not use static variables for summary?**  
A: Static variables tightly couple logic; model objects are cleaner and scalable.

**Q2: Where is this class typically used?**  
A: Between JSON parser and report/mail builder layers.

**Q3: Can this class be immutable?**  
A: Yes, by using constructor-only fields (future improvement).

**Q4: Is this an entity or DTO?**  
A: DTO (Data Transfer Object).

---

## 7. Enhancements (If Asked)

- Add step-level counts
- Add execution duration
- Make immutable with constructor
- Override `toString()`
- Add builder pattern

---

## 8. One-Line Interview Summary

> "ExecutionSummary is a POJO that encapsulates feature and scenario execution counts and transfers them across reporting layers."
