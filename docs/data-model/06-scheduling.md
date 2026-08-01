# Datenmodell — Termine, Aufgaben, Erinnerungen

Entspricht Epic E10.

```mermaid
erDiagram
    APPLICATION ||--o{ INTERVIEW_EVENT : hat
    USER ||--o{ TASK : hat
    APPLICATION ||--o{ TASK : bezieht_sich_auf
    TASK ||--o{ NOTIFICATION : loest_aus
    INTERVIEW_EVENT ||--o{ NOTIFICATION : loest_aus

    INTERVIEW_EVENT {
        uuid id PK
        uuid application_id FK
        int round_no "UQ mit application_id"
        string format "PHONE|VIDEO|ONSITE|ASSESSMENT|TRIAL_DAY"
        timestamp scheduled_at
        int duration_minutes
        string location_or_link
        text participants_text
        text preparation_notes
        text debrief_notes
        string outcome "PASSED|FAILED|PENDING|CANCELLED"
    }

    TASK {
        uuid id PK
        uuid user_id FK
        uuid application_id FK "nullbar"
        string title
        string type "FOLLOW_UP|PREPARE|DEADLINE|OTHER"
        timestamp due_at
        string status "OPEN|DONE|DISMISSED"
        timestamp completed_at
        boolean generated_by_system
    }

    NOTIFICATION {
        uuid id PK
        uuid user_id FK
        uuid task_id FK "nullbar"
        uuid interview_event_id FK "nullbar"
        string channel "EMAIL|IN_APP"
        string subject
        timestamp sent_at
        timestamp read_at
        int attempt_count
        string last_error
    }
```

**Zu beachten:**
- `Notification` hat zwei optionale Auslöser-Referenzen (`task_id`,
  `interview_event_id`) — praktisch ist meist genau eine gesetzt, aber
  anders als bei `ExtractionJob` ist das hier nicht strikt exklusiv
  erzwungen (eine allgemeine Systemnachricht könnte künftig ganz ohne
  Bezug stehen).
- Der eindeutige Schlüssel auf `Notification` über (`user_id`, `task_id`,
  `interview_event_id`, `channel`) verhindert doppelten Versand — im
  Diagramm nicht sichtbar, gehört als Constraint ins Skript.
- `Task.application_id` ist nullbar: nicht jede Aufgabe hängt an einer
  Bewerbung (z. B. rein persönliche Erinnerungen).
