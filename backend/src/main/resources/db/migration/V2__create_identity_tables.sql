CREATE TABLE app_user
(
    id                      UUID PRIMARY KEY,
    email                   VARCHAR(255) NOT NULL,
    password_hash           VARCHAR(255) NOT NULL,
    display_name            VARCHAR(255) NOT NULL,
    status                  VARCHAR(30)  NOT NULL
        CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'LOCKED', 'PENDING_DELETION')),
    locale                  VARCHAR(10)  NOT NULL DEFAULT 'de',
    time_zone               VARCHAR(50)  NOT NULL DEFAULT 'UTC',
    email_verified_at       TIMESTAMPTZ,
    last_login_at           TIMESTAMPTZ,
    deletion_requested_at   TIMESTAMPTZ,
    ghosting_threshold_days INTEGER      NOT NULL DEFAULT 30,
    notifications_enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ  NOT NULL,
    created_by              VARCHAR(255) NOT NULL,
    updated_at              TIMESTAMPTZ  NOT NULL,
    updated_by              VARCHAR(255) NOT NULL,

    CONSTRAINT uq_app_user_email UNIQUE (email)
);

CREATE TABLE role
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,

    CONSTRAINT uq_role_name UNIQUE (name)
);

CREATE TABLE user_role
(
    user_id UUID NOT NULL REFERENCES app_user (id),
    role_id UUID NOT NULL REFERENCES role (id),

    PRIMARY KEY (user_id, role_id)
);

INSERT INTO role (id, name, description, created_at, created_by, updated_at, updated_by)
VALUES (gen_random_uuid(), 'ROLE_USER', 'Bewerber mit Vollzugriff auf eigene Daten', now(), 'SYSTEM', now(), 'SYSTEM'),
       (gen_random_uuid(), 'ROLE_ADMIN', 'Plattformbetrieb ohne Zugriff auf fachliche Nutzerinhalte', now(), 'SYSTEM', now(), 'SYSTEM');