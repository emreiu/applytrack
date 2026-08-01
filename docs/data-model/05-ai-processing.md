# Datenmodell — KI-gestützte Extraktion

Entspricht Epic E8. Leitprinzip: `ExtractionProposal` ist ein Vorschlag,
kein Bestand — erst nach Prüfung entstehen Datensätze in `JobPosting`
bzw. `CandidateProfile` (siehe Diagramme 01 und 04).

```mermaid
erDiagram
    USER ||--o{ EXTRACTION_JOB : startet
    SOURCE_DOCUMENT ||--o| EXTRACTION_JOB : eingabe_fuer
    DOCUMENT_VERSION ||--o| EXTRACTION_JOB : eingabe_fuer
    EXTRACTION_JOB ||--o| EXTRACTION_PROPOSAL : erzeugt
    EXTRACTION_JOB ||--o{ AI_USAGE_RECORD : verursacht
    USER ||--o{ AI_BUDGET : hat
    EXTRACTION_PROPOSAL |o--o| JOB_POSTING : erzeugt_bei_annahme
    EXTRACTION_PROPOSAL |o--o| CANDIDATE_PROFILE : erzeugt_bei_annahme

    EXTRACTION_JOB {
        uuid id PK
        uuid user_id FK
        string job_type "JOB_POSTING|CV"
        uuid source_document_id FK "nullbar, genau eine der beiden Quellen gesetzt"
        uuid document_version_id FK "nullbar"
        string status "QUEUED|RUNNING|NEEDS_REVIEW|COMPLETED|FAILED|CANCELLED"
        int attempt_count
        timestamp queued_at
        timestamp started_at
        timestamp finished_at
        string error_code
        string error_message
        string model_name
        string prompt_version
    }

    EXTRACTION_PROPOSAL {
        uuid id PK
        uuid extraction_job_id FK "UQ"
        jsonb payload
        numeric overall_confidence
        timestamp reviewed_at
        string review_action "ACCEPTED|EDITED|REJECTED"
        jsonb review_diff
        uuid resulting_job_posting_id FK "nullbar"
        uuid resulting_profile_id FK "nullbar"
    }

    AI_USAGE_RECORD {
        uuid id PK
        uuid user_id FK
        uuid extraction_job_id FK "nullbar"
        timestamp occurred_at
        string model_name
        int input_tokens
        int output_tokens
        int cost_micro_units
    }

    AI_BUDGET {
        uuid id PK
        uuid user_id FK "nullbar = systemweites Budget"
        date period_start
        date period_end
        int limit_micro_units
        int used_micro_units
    }
```

**Zu beachten:**
- `ExtractionJob` hat **zwei alternative** Quellreferenzen — genau eine
  ist gesetzt, je nach `job_type`. Im ER-Diagramm werden dafür zwei
  optionale Beziehungen gezeichnet; die Exklusivität selbst gehört als
  Check-Constraint ins Flyway-Skript, nicht ins Diagramm.
- `AiBudget.user_id` ist nullbar: `NULL` steht für das systemweite
  Gesamtbudget (E8-S04) und existiert als eigener Datensatz neben den
  nutzerbezogenen Budgets.
- Die Beziehungen von `ExtractionProposal` zu `JobPosting` und
  `CandidateProfile` sind **optional und einseitig gerichtet** (entstehen
  erst bei `review_action = ACCEPTED`/`EDITED`) — deshalb `|o--o|` statt
  einer verpflichtenden Kardinalität.
