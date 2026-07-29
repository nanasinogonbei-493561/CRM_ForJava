-- =============================================================
-- V1__create_users_table.sql
-- USERS（ユーザー）テーブル
--   営業担当者・サポート・管理者のアカウントを管理する
-- =============================================================

CREATE TABLE users (
    id                  BIGINT          NOT NULL AUTO_INCREMENT              COMMENT 'ユーザーID',
    username            VARCHAR(50)     NOT NULL                            COMMENT '担当者名',
    email               VARCHAR(255)    NOT NULL                            COMMENT 'メールアドレス（ログインID・一意）',
    role                VARCHAR(20)     NOT NULL                            COMMENT '権限：SALES / SUPPORT / ADMIN',
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE              COMMENT '論理削除フラグ（退職者などはTRUE）',
    password_hash       VARCHAR(255)    NOT NULL                            COMMENT 'パスワードのハッシュ値（平文保存禁止）',
    locked              BOOLEAN         NOT NULL DEFAULT FALSE              COMMENT 'アカウントロック状態',
    failed_login_count  INT             NOT NULL DEFAULT 0                  COMMENT 'ログイン連続失敗回数（3回でロック）',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '作成日時',

    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('SALES', 'SUPPORT', 'ADMIN'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'ユーザー（社内担当者）アカウント';