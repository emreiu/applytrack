# Datenmodell — Dokumente und Versionierung

Entspricht Epic E7. Kernpunkt: `ApplicationDocument` referenziert eine
konkrete `DocumentVersion`, nicht das `Document` selbst — eine spätere
Überarbeitung ändert nichts an bereits eingereichten Fassungen.

```mermaid
erDiagram
    USER ||--o{ DOCUMENT : besitzt
    DOCUMENT ||--o{ DOCUMENT_VERSION : hat
    APPLICATION ||--o{ APPLICATION_DOCUMENT : reicht_ein
    DOCUMENT_VERSION ||--o{ APPLICATION_DOCUMENT : wird_verwendet_in

    DOCUMENT {
        uuid id PK
        uuid user_id FK
        string kind "CV|COVER_LETTER|CERTIFICATE|PORTFOLIO|OTHER"
        string title
    }

    DOCUMENT_VERSION {
        uuid id PK
        uuid document_id FK
        int version_no "UQ mit document_id"
        string storage_key
        string original_filename
        string mime_type
        int file_size
        string checksum
        text extracted_text
        string change_note
        boolean is_current
    }

    APPLICATION_DOCUMENT {
        uuid id PK
        uuid application_id FK
        uuid document_version_id FK
        string role "CV|COVER_LETTER|ATTACHMENT"
        timestamp attached_at
    }

    APPLICATION {
        uuid id PK "extern, siehe Diagramm 01"
    }
```

**Zu beachten:**
- `ApplicationDocument` hat einen eindeutigen Schlüssel über
  (`application_id`, `document_version_id`, `role`) — dieselbe Version
  kann derselben Bewerbung nicht zweimal in derselben Rolle zugeordnet
  werden.
- `DocumentVersion.is_current`: genau ein Datensatz je `document_id` darf
  `true` sein — im ER-Diagramm nicht abbildbar, gehört als
  Check-Constraint bzw. Anwendungslogik ins Flyway-Skript.
- Der Pfeil `DOCUMENT_VERSION ||--o| CANDIDATE_PROFILE` (höchstens ein
  Profil je Lebenslauf-Version) ist Teil von Diagramm 04, nicht hier
  wiederholt.
