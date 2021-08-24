-- api_token

DROP TABLE "api_token";
CREATE TABLE "api_token"
(
    "id"             VARCHAR(255) NOT NULL,
    "app"            VARCHAR(255) NOT NULL,
    "access_token"   VARCHAR(255) NOT NULL,
    "refresh_token"  VARCHAR(255) NOT NULL,
    "authentication" BLOB         NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX IDX_token_access ON "api_token" ("app", "access_token");
CREATE INDEX IDX_token_refresh ON "api_token" ("app", "refresh_token");
COMMENT ON TABLE "api_token" IS 'api token';
COMMENT ON COLUMN "api_token"."id" IS 'id';
COMMENT ON COLUMN "api_token"."app" IS 'app';
COMMENT ON COLUMN "api_token"."access_token" IS 'accessToken';
COMMENT ON COLUMN "api_token"."refresh_token" IS 'refreshToken';
COMMENT ON COLUMN "api_token"."authentication" IS 'authentication';
;

