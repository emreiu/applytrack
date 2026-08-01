# Datenmodell — Fähigkeitenprofil und Skill-Taxonomie

Entspricht Epic E9. `Skill` ist bewusst **systemweit**, nicht
nutzergebunden — im Gegensatz zu praktisch jeder anderen Entität in
diesem Modell (siehe Ausnahme in Regel B.4-1).

```mermaid
erDiagram
    DOCUMENT_VERSION ||--o| CANDIDATE_PROFILE : erzeugt
    CANDIDATE_PROFILE ||--o{ PROFILE_SKILL : hat
    CANDIDATE_PROFILE ||--o{ PROFILE_EXPERIENCE : hat
    CANDIDATE_PROFILE ||--o{ PROFILE_EDUCATION : hat
    SKILL ||--o{ SKILL_ALIAS : hat
    SKILL ||--o{ PROFILE_SKILL : referenziert_durch
    USER ||--o{ SKILL : schlaegt_vor

    SKILL {
        uuid id PK
        string canonical_name "UQ"
        string category "LANGUAGE|FRAMEWORK|TOOL|PLATFORM|METHOD|DOMAIN|SOFT_SKILL|CERTIFICATION"
        string status "APPROVED|PROPOSED"
        uuid proposed_by_user_id FK "nullbar"
    }

    SKILL_ALIAS {
        uuid id PK
        uuid skill_id FK
        string alias "UQ"
    }

    CANDIDATE_PROFILE {
        uuid id PK
        uuid user_id FK
        uuid document_version_id FK "UQ, nullbar"
        string title
        text summary
        boolean is_active
        timestamp generated_at
        string source "MANUAL|EXTRACTED"
    }

    PROFILE_SKILL {
        uuid id PK
        uuid candidate_profile_id FK
        uuid skill_id FK "nullbar"
        string raw_label
        numeric years_experience
        string proficiency "BASIC|ADVANCED|EXPERT"
        text evidence_text
        boolean is_confirmed
    }

    PROFILE_EXPERIENCE {
        uuid id PK
        uuid candidate_profile_id FK
        string employer_name
        string title
        date start_date
        date end_date
        text description
        int sort_order
    }

    PROFILE_EDUCATION {
        uuid id PK
        uuid candidate_profile_id FK
        string institution
        string degree
        string field_of_study
        date start_date
        date end_date
        string grade
    }
```

**Zu beachten:**
- `CandidateProfile.document_version_id` ist eindeutig — höchstens ein
  bestätigtes Profil je Lebenslauf-Version (siehe E9-S04, Regel).
- `ProfileSkill.skill_id` ist nullbar: eine erkannte Fähigkeit kann
  zunächst unverknüpfter Freitext bleiben (`raw_label`), bevor sie einem
  kanonischen `Skill` zugeordnet wird (E9-S02).
- `Skill.proposed_by_user_id` verweist auf einen einzelnen Nutzer, obwohl
  die Entität selbst systemweit ist — das ist bewusst, da nur die
  *Urheberschaft* des Vorschlags nutzerbezogen ist, nicht der Skill selbst.
- `JobRequirement.skill_id` (siehe Diagramm 01) referenziert dieselbe
  `Skill`-Entität — daher taucht `Skill` in zwei Kontexten auf.
