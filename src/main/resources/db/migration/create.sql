PRAGMA foreign_keys = false;


CREATE TABLE IF NOT EXISTS "x_app_config"
(
    "id" text NOT NULL,
    "value" text,
    "group" integer,
    PRIMARY KEY ("id")
);


-- ----------------------------
-- Table structure for x_search_nodes
-- ----------------------------
CREATE TABLE IF NOT EXISTS `x_search_node`
(
    `parent` text,
    `name`   text,
    `is_dir` numeric,
    `size`   integer,
    "id"     integer NOT NULL,
    PRIMARY KEY ("id")
);

-- ----------------------------
-- Indexes structure for table x_search_nodes
-- ----------------------------
CREATE INDEX IF NOT EXISTS "main"."idx_x_search_node_parent"
    ON "x_search_node" (
                        "parent" ASC
        );

PRAGMA foreign_keys = true;
