CREATE TABLE IF NOT EXISTS "x_app_config"
(
    "id"    integer NOT NULL,
    "label" text,
    "value" text,
    "safe"  integer,
    "group" integer,
    PRIMARY KEY ("id")
);