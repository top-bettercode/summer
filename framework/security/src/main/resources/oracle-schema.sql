-- api_token

DROP TABLE "api_token";
CREATE TABLE "api_token" (
  "prefix" VARCHAR(255) NOT NULL,
  "id" VARCHAR(255) NOT NULL,
  "access_token" VARCHAR(255) NOT NULL,
  "refresh_token" VARCHAR(255) NOT NULL,
  "authentication" BLOB NULL
);
CREATE INDEX IDX_pitoken_prefix ON "api_token" ("prefix");
CREATE INDEX IDX_pitoken_id ON "api_token" ("id");
CREATE INDEX IDX_pitoken_sstoken ON "api_token" ("access_token");
CREATE INDEX IDX_pitoken_shtoken ON "api_token" ("refresh_token");
COMMENT ON TABLE "api_token" IS '接口令牌';
COMMENT ON COLUMN "api_token"."prefix" IS '前缀';
COMMENT ON COLUMN "api_token"."id" IS 'id';
COMMENT ON COLUMN "api_token"."access_token" IS 'accessToken';
COMMENT ON COLUMN "api_token"."refresh_token" IS 'refreshToken';
COMMENT ON COLUMN "api_token"."authentication" IS 'authentication';

