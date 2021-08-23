# api_token
DROP TABLE IF EXISTS `api_token`;
CREATE TABLE `api_token` (
  `prefix` VARCHAR(255) NOT NULL COMMENT '前缀',
  `id` VARCHAR(255) NOT NULL COMMENT 'id',
  `access_token` VARCHAR(255) NOT NULL COMMENT 'accessToken',
  `refresh_token` VARCHAR(255) NOT NULL COMMENT 'refreshToken',
  `authentication` BLOB NULL COMMENT 'authentication'
) DEFAULT CHARSET=utf8 COMMENT = '接口令牌';
CREATE INDEX IDX_pitoken_prefix ON `api_token` (`prefix`);
CREATE INDEX IDX_pitoken_id ON `api_token` (`id`);
CREATE INDEX IDX_pitoken_sstoken ON `api_token` (`access_token`);
CREATE INDEX IDX_pitoken_shtoken ON `api_token` (`refresh_token`);

