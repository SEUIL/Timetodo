SET @fk_name := (
    SELECT constraint_name
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'task_entity'
      AND column_name = 'category_id_category_id'
      AND referenced_table_name IS NOT NULL
    LIMIT 1
);
SET @sql := IF(@fk_name IS NULL, 'SELECT 1', CONCAT('ALTER TABLE task_entity DROP FOREIGN KEY `', @fk_name, '`'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_name := (
    SELECT constraint_name
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'task_entity'
      AND column_name = 'user_id_user_id'
      AND referenced_table_name IS NOT NULL
    LIMIT 1
);
SET @sql := IF(@fk_name IS NULL, 'SELECT 1', CONCAT('ALTER TABLE task_entity DROP FOREIGN KEY `', @fk_name, '`'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_name := (
    SELECT constraint_name
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'calendar_entity'
      AND column_name = 'category_id_category_id'
      AND referenced_table_name IS NOT NULL
    LIMIT 1
);
SET @sql := IF(@fk_name IS NULL, 'SELECT 1', CONCAT('ALTER TABLE calendar_entity DROP FOREIGN KEY `', @fk_name, '`'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_name := (
    SELECT constraint_name
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'calendar_entity'
      AND column_name = 'user_id_user_id'
      AND referenced_table_name IS NOT NULL
    LIMIT 1
);
SET @sql := IF(@fk_name IS NULL, 'SELECT 1', CONCAT('ALTER TABLE calendar_entity DROP FOREIGN KEY `', @fk_name, '`'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'task_entity' AND column_name = 'category_id_category_id')
    AND NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'task_entity' AND column_name = 'category_id'),
    'ALTER TABLE task_entity RENAME COLUMN category_id_category_id TO category_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'task_entity' AND column_name = 'user_id_user_id')
    AND NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'task_entity' AND column_name = 'user_id'),
    'ALTER TABLE task_entity RENAME COLUMN user_id_user_id TO user_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'calendar_entity' AND column_name = 'category_id_category_id')
    AND NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'calendar_entity' AND column_name = 'category_id'),
    'ALTER TABLE calendar_entity RENAME COLUMN category_id_category_id TO category_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'calendar_entity' AND column_name = 'user_id_user_id')
    AND NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'calendar_entity' AND column_name = 'user_id'),
    'ALTER TABLE calendar_entity RENAME COLUMN user_id_user_id TO user_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.key_column_usage WHERE table_schema = DATABASE() AND table_name = 'task_entity' AND column_name = 'category_id' AND referenced_table_name IS NOT NULL),
    'ALTER TABLE task_entity ADD CONSTRAINT fk_task_category FOREIGN KEY (category_id) REFERENCES category_entity (category_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.key_column_usage WHERE table_schema = DATABASE() AND table_name = 'task_entity' AND column_name = 'user_id' AND referenced_table_name IS NOT NULL),
    'ALTER TABLE task_entity ADD CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES user_entity (user_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.key_column_usage WHERE table_schema = DATABASE() AND table_name = 'calendar_entity' AND column_name = 'category_id' AND referenced_table_name IS NOT NULL),
    'ALTER TABLE calendar_entity ADD CONSTRAINT fk_calendar_category FOREIGN KEY (category_id) REFERENCES category_entity (category_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.key_column_usage WHERE table_schema = DATABASE() AND table_name = 'calendar_entity' AND column_name = 'user_id' AND referenced_table_name IS NOT NULL),
    'ALTER TABLE calendar_entity ADD CONSTRAINT fk_calendar_user FOREIGN KEY (user_id) REFERENCES user_entity (user_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
