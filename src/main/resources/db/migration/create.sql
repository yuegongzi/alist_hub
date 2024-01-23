CREATE TABLE IF NOT EXISTS "x_app_config"
(
    "id" text NOT NULL,
    "value" text,
    "group" integer,
    PRIMARY KEY ("id")
);