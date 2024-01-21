CREATE TABLE IF NOT EXISTS "x_app_config"
(
    "id"    integer NOT NULL,
    "label" text NOT NULL UNIQUE,
    "value" text,
    "safe"  numeric,
    "group" integer,
    PRIMARY KEY ("id")
);