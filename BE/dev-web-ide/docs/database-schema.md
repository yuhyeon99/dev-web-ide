# Web IDE ERD

```mermaid
erDiagram
    USERS ||--o{ OAUTH_ACCOUNTS : has
    USERS ||--o{ AUTH_SESSIONS : logs_in
    USERS ||--o{ TERMS_AGREEMENTS : agrees
    USERS ||--o{ PROJECTS : owns
    USERS ||--o{ PROJECT_MEMBERS : joins
    USERS ||--o{ PROJECT_ACCESS_LOGS : opens
    USERS ||--o{ PROJECT_SAVE_BATCHES : saves
    USERS ||--o{ WORKSPACE_SESSIONS : starts

    GUEST_SESSIONS ||--o{ AUTH_SESSIONS : has
    GUEST_SESSIONS ||--o{ PROJECTS : owns
    GUEST_SESSIONS ||--o{ PROJECT_ACCESS_LOGS : opens
    GUEST_SESSIONS ||--o{ PROJECT_SAVE_BATCHES : saves
    GUEST_SESSIONS ||--o{ WORKSPACE_SESSIONS : starts

    RUNTIMES ||--o{ PROJECTS : selected_by
    RUNTIMES ||--o{ WORKSPACE_SESSIONS : runs_with

    PROJECTS ||--o{ PROJECT_MEMBERS : has
    PROJECTS ||--o{ PROJECT_FILES : contains
    PROJECTS ||--|| PROJECT_SETTINGS : has
    PROJECTS ||--o{ PROJECT_ACCESS_LOGS : records
    PROJECTS ||--o{ PROJECT_SAVE_BATCHES : saves
    PROJECTS ||--o{ WORKSPACE_SESSIONS : runs

    PROJECT_FILES ||--o{ PROJECT_FILES : parent_child
    PROJECT_FILES ||--o{ FILE_VERSIONS : versioned_by

    PROJECT_SAVE_BATCHES ||--o{ FILE_VERSIONS : includes

    WORKSPACE_SESSIONS ||--o{ CONTAINER_INSTANCES : uses
    WORKSPACE_SESSIONS ||--o{ TERMINAL_LOGS : writes

    USERS {
        BIGINT id PK
        VARCHAR(200) email UK_NN
        VARCHAR(50) nickname NN
        ENUM role NN
        ENUM status NN
        DATETIME created_at NN
        DATETIME updated_at NN
    }

    OAUTH_ACCOUNTS {
        BIGINT id PK
        BIGINT user_id FK_NN
        ENUM provider NN
        VARCHAR(200) provider_user_id NN
        VARCHAR(200) provider_email
        DATETIME created_at NN
    }

    AUTH_SESSIONS {
        BIGINT id PK
        BIGINT user_id FK
        BIGINT guest_session_id FK
        VARCHAR(200) refresh_token_hash UK_NN
        VARCHAR(45) client_ip
        VARCHAR(500) user_agent
        DATETIME expires_at NN
        DATETIME created_at NN
        DATETIME revoked_at
    }

    TERMS_AGREEMENTS {
        BIGINT id PK
        BIGINT user_id FK_NN
        BOOLEAN terms_agreed NN
        BOOLEAN privacy_agreed NN
        DATETIME agreed_at NN
    }

    GUEST_SESSIONS {
        BIGINT id PK
        VARCHAR(200) guest_token UK_NN
        VARCHAR(45) client_ip
        DATETIME expires_at NN
        DATETIME created_at NN
    }

    RUNTIMES {
        BIGINT id PK
        VARCHAR(100) name UK_NN
        VARCHAR(100) display_name NN
        VARCHAR(50) version NN
        VARCHAR(200) docker_image NN
        ENUM language NN
        ENUM status NN
        DATETIME created_at NN
    }

    PROJECTS {
        BIGINT id PK
        BIGINT owner_user_id FK
        BIGINT guest_session_id FK
        BIGINT runtime_id FK_NN
        VARCHAR(200) name NN
        VARCHAR(500) description
        ENUM project_type NN
        ENUM visibility NN
        ENUM status NN
        VARCHAR(500) storage_path NN
        DATETIME created_at NN
        DATETIME updated_at NN
    }

    PROJECT_MEMBERS {
        BIGINT id PK
        BIGINT project_id FK_NN
        BIGINT user_id FK_NN
        ENUM role NN
        ENUM status NN
        BIGINT invited_by_user_id FK
        DATETIME invited_at
        DATETIME joined_at
    }

    PROJECT_SETTINGS {
        BIGINT id PK
        BIGINT project_id FK_UK_NN
        BOOLEAN auto_save_enabled NN
        BOOLEAN format_on_save_enabled NN
        BOOLEAN guest_can_edit NN
        BOOLEAN share_cursor_position NN
        DATETIME updated_at NN
    }

    PROJECT_ACCESS_LOGS {
        BIGINT id PK
        BIGINT project_id FK_NN
        BIGINT user_id FK
        BIGINT guest_session_id FK
        ENUM access_type NN
        DATETIME accessed_at NN
    }

    PROJECT_FILES {
        BIGINT id PK
        BIGINT project_id FK_NN
        BIGINT parent_file_id FK
        VARCHAR(200) name NN
        VARCHAR(1000) path NN
        ENUM file_type NN
        VARCHAR(100) mime_type
        BIGINT size_bytes NN
        ENUM status NN
        DATETIME created_at NN
        DATETIME updated_at NN
    }

    PROJECT_SAVE_BATCHES {
        BIGINT id PK
        BIGINT project_id FK_NN
        BIGINT user_id FK
        BIGINT guest_session_id FK
        ENUM status NN
        INT saved_file_count NN
        DATETIME saved_at NN
    }

    FILE_VERSIONS {
        BIGINT id PK
        BIGINT project_file_id FK_NN
        BIGINT save_batch_id FK
        INT version_no NN
        VARCHAR(1000) storage_path NN
        VARCHAR(200) content_hash NN
        BIGINT size_bytes NN
        DATETIME created_at NN
    }

    WORKSPACE_SESSIONS {
        BIGINT id PK
        BIGINT project_id FK_NN
        BIGINT runtime_id FK_NN
        BIGINT user_id FK
        BIGINT guest_session_id FK
        ENUM status NN
        DATETIME started_at NN
        DATETIME stopped_at
        DATETIME last_heartbeat_at
    }

    CONTAINER_INSTANCES {
        BIGINT id PK
        BIGINT workspace_session_id FK_NN
        VARCHAR(50) provider NN
        VARCHAR(500) task_arn
        VARCHAR(200) container_id
        VARCHAR(200) docker_image NN
        ENUM status NN
        VARCHAR(500) efs_mount_path
        DATETIME created_at NN
        DATETIME stopped_at
    }

    TERMINAL_LOGS {
        BIGINT id PK
        BIGINT workspace_session_id FK_NN
        BIGINT sequence_no NN
        ENUM stream_type NN
        TEXT content NN
        DATETIME created_at NN
    }
```