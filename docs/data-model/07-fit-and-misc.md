# Datenmodell — Passungsanalyse und Sonstiges

Entspricht dem optionalen Epic E13 sowie den Restfunktionen aus E12.

```mermaid
erDiagram
    JOB_POSTING ||--o{ FIT_ASSESSMENT : wird_bewertet
    CANDIDATE_PROFILE ||--o{ FIT_ASSESSMENT : bewertet_gegen
    FIT_ASSESSMENT ||--o{ FIT_FACTOR : besteht_aus
    USER ||--o{ SAVED_FILTER : besitzt
    USER ||--o{ IMPORT_JOB : startet

    FIT_ASSESSMENT {
        uuid id PK
        uuid user_id FK
        uuid job_posting_id FK
        uuid candidate_profile_id FK
        timestamp computed_at
        numeric score
        int considered_factor_count
        string algorithm_version "UQ mit job_posting_id, candidate_profile_id"
    }

    FIT_FACTOR {
        uuid id PK
        uuid fit_assessment_id FK
        string factor_type
        numeric raw_value
        numeric weight
        numeric contribution
        text explanation
    }

    SAVED_FILTER {
        uuid id PK
        uuid user_id FK
        string name "UQ mit user_id"
        string target_view
        jsonb criteria
    }

    IMPORT_JOB {
        uuid id PK
        uuid user_id FK
        string status
        string file_name
        int row_count
        int imported_count
        int failed_count
        jsonb report
        timestamp started_at
        timestamp finished_at
    }
```

**Zu beachten:**
- `FitAssessment` ist je Kombination aus Anzeige, Profil **und**
  Algorithmusversion eindeutig — ändert sich das Bewertungsverfahren,
  entsteht ein neuer Datensatz statt einer Überschreibung. Das erhält die
  Nachvollziehbarkeit, falls sich die Gewichtung später ändert.
- `FitFactor.raw_value` kann fehlen (nullbar in der Praxis, hier aus
  Platzgründen nicht extra vermerkt) — ein nicht bewertbarer Faktor wird
  ausgeschlossen, nicht mit 0 bewertet (siehe E13-S01).
