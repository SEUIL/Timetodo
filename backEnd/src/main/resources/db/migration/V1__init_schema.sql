CREATE TABLE preferences_entity (
    preferences_id BIGINT NOT NULL AUTO_INCREMENT,
    theme VARCHAR(20) NOT NULL,
    user_entity_user_id BIGINT NULL,
    PRIMARY KEY (preferences_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_entity (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    preferences_preferences_id BIGINT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_entity_username (username),
    UNIQUE KEY uk_user_entity_email (email),
    KEY idx_user_entity_preferences (preferences_preferences_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE category_entity (
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(255) NOT NULL,
    color VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_category_entity_category_name (category_name),
    KEY idx_category_entity_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE calendar_entity (
    calendar_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NULL,
    description VARCHAR(255) NULL,
    start_time DATETIME(6) NULL,
    end_time DATETIME(6) NULL,
    location VARCHAR(255) NULL,
    repeat_type VARCHAR(255) NULL,
    category_id BIGINT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (calendar_id),
    KEY idx_calendar_entity_category_id (category_id),
    KEY idx_calendar_entity_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE task_entity (
    task_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    due_date DATETIME(6) NOT NULL,
    priority VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    repeat_type VARCHAR(255) NULL,
    category_id BIGINT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (task_id),
    KEY idx_task_entity_category_id (category_id),
    KEY idx_task_entity_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reminder_entity (
    reminder_id BIGINT NOT NULL AUTO_INCREMENT,
    notifications_enabled BIT NOT NULL,
    time_before INT NOT NULL,
    repeats BIT NOT NULL,
    task_id BIGINT NULL,
    calendar_id BIGINT NULL,
    PRIMARY KEY (reminder_id),
    KEY idx_reminder_entity_task_id (task_id),
    KEY idx_reminder_entity_calendar_id (calendar_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `log` (
    log_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    input_text TEXT NOT NULL,
    openai_response TEXT NOT NULL,
    processed_result TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE preferences_entity
    ADD CONSTRAINT fk_preferences_user
    FOREIGN KEY (user_entity_user_id) REFERENCES user_entity (user_id);

ALTER TABLE user_entity
    ADD CONSTRAINT fk_user_preferences
    FOREIGN KEY (preferences_preferences_id) REFERENCES preferences_entity (preferences_id);

ALTER TABLE category_entity
    ADD CONSTRAINT fk_category_user
    FOREIGN KEY (user_id) REFERENCES user_entity (user_id);

ALTER TABLE calendar_entity
    ADD CONSTRAINT fk_calendar_category
    FOREIGN KEY (category_id) REFERENCES category_entity (category_id);

ALTER TABLE calendar_entity
    ADD CONSTRAINT fk_calendar_user
    FOREIGN KEY (user_id) REFERENCES user_entity (user_id);

ALTER TABLE task_entity
    ADD CONSTRAINT fk_task_category
    FOREIGN KEY (category_id) REFERENCES category_entity (category_id);

ALTER TABLE task_entity
    ADD CONSTRAINT fk_task_user
    FOREIGN KEY (user_id) REFERENCES user_entity (user_id);

ALTER TABLE reminder_entity
    ADD CONSTRAINT fk_reminder_task
    FOREIGN KEY (task_id) REFERENCES task_entity (task_id);

ALTER TABLE reminder_entity
    ADD CONSTRAINT fk_reminder_calendar
    FOREIGN KEY (calendar_id) REFERENCES calendar_entity (calendar_id);
