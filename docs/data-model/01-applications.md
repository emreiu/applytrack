# Datenmodell — Bewerbungen, Stellenanzeigen, Stammdaten

Entspricht den Epics E4, E5, E6. Kernstück: `Application` mit
`ApplicationStatusEvent` als unveränderlicher Ereigniskette — der
aktuelle Status ist ein abgeleiteter Lesewert, keine primäre Wahrheit.

```mermaid
erDiagram
    USER ||--o{ COMPANY : besitzt
    USER ||--o{ CONTACT : besitzt
    USER ||--o{ JOB_POSTING : besitzt
    USER ||--o{ APPLICATION : besitzt

    COMPANY ||--o{ CONTACT : beschaeftigt
    COMPANY ||--o{ JOB_POSTING : schreibt_aus

    SOURCE_DOCUMENT ||--o| JOB_POSTING : ist_grundlage_fuer
    JOB_POSTING ||--o{ JOB_REQUIREMENT : fordert
    JOB_POSTING ||--o{ APPLICATION : wird_beworben_durch

    APPLICATION ||--o{ APPLICATION_STATUS_EVENT : durchlaeuft
    APPLICATION ||--o{ APPLICATION_NOTE : hat
    APPLICATION ||--o{ APPLICATION_CONTACT : verknuepft
    CONTACT ||--o{ APPLICATION_CONTACT : ist_beteiligt_an

    COMPANY {
        uuid id PK
        uuid user_id FK
        string name
        string normalized_name "UQ mit user_id"
        string website
        string industry
        string size_category "MICRO|SMALL|MEDIUM|LARGE|UNKNOWN"
        string city
        string country
        text notes
    }

    CONTACT {
        uuid id PK
        uuid user_id FK
        uuid company_id FK "nullbar"
        string first_name
        string last_name
        string role_title
        string email
        string phone
        string profile_url
        text notes
    }

    SOURCE_DOCUMENT {
        uuid id PK
        uuid user_id FK
        string kind "PASTED_TEXT|UPLOADED_PDF|FETCHED_URL"
        string original_filename
        string mime_type
        string storage_key
        string checksum
        string source_url
        timestamp captured_at
        text extracted_text
        string extraction_error
    }

    JOB_POSTING {
        uuid id PK
        uuid user_id FK
        uuid company_id FK "nullbar"
        uuid source_document_id FK "nullbar"
        string title
        string employment_type "FULL_TIME|PART_TIME|INTERNSHIP|CONTRACT"
        string work_model "ONSITE|HYBRID|REMOTE"
        string seniority_level "JUNIOR|REGULAR|SENIOR|LEAD|UNKNOWN"
        string location_city
        string location_country
        string source_channel
        string source_url
        date posted_at
        date application_deadline
        string language
        numeric salary_min
        numeric salary_max
        string salary_currency
        string salary_period "MONTH|YEAR"
        boolean salary_is_collective_minimum
        string contact_person_name
    }

    JOB_REQUIREMENT {
        uuid id PK
        uuid job_posting_id FK
        uuid skill_id FK "nullbar, siehe Diagramm 04"
        text raw_text
        string requirement_type "MUST|NICE"
        numeric years_required
        int sort_order
        string source "MANUAL|EXTRACTED"
        numeric confidence
    }

    APPLICATION {
        uuid id PK
        uuid user_id FK
        uuid job_posting_id FK
        date applied_at
        string channel
        string priority "LOW|MEDIUM|HIGH"
        string current_status "denormalisiert, siehe Regel B.4-2"
        timestamp current_status_since
        timestamp last_activity_at
        timestamp closed_at
        string outcome
        text notes
    }

    APPLICATION_STATUS_EVENT {
        uuid id PK
        uuid application_id FK
        string status "DRAFT..GHOSTED, siehe E6-S02"
        string previous_status
        timestamp occurred_at "fachlicher Zeitpunkt"
        timestamp recorded_at "technischer Zeitpunkt"
        string source "MANUAL|SYSTEM"
        text note
        boolean is_correction
    }

    APPLICATION_NOTE {
        uuid id PK
        uuid application_id FK
        text content
        timestamp noted_at
    }

    APPLICATION_CONTACT {
        uuid application_id PK, FK
        uuid contact_id PK, FK
        string role_in_process
    }
```

**Zu beachten beim Übertragen:**
- `JobPosting.company_id` ist nullbar — eine Anzeige kann ohne bekannte
  Firma existieren (Deduplizierung erfolgt später, siehe E4-S02).
- `Application` → `JobPosting` ist **1:n**, nicht 1:1: eine erneute
  Bewerbung auf dieselbe Anzeige ist ein zweiter `Application`-Datensatz.
- `ApplicationStatusEvent` ist **nur einfügbar**, nie änderbar — im
  ER-Diagramm nicht sichtbar, aber in `RESTRICTIONS.md`/ADR festzuhalten.
- `DocumentVersion` (siehe Diagramm 02) und `Skill` (siehe Diagramm 04)
  sind hier nur referenziert, nicht vollständig dargestellt.
