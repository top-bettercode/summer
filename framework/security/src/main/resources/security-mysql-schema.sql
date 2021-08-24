# api_token
DROP TABLE IF EXISTS `api_token`;
CREATE TABLE `api_token`
(
    `id`             VARCHAR(255) NOT NULL COMMENT 'id',
    `access_token`   VARCHAR(255) NOT NULL COMMENT 'accessToken',
    `refresh_token`  VARCHAR(255) NOT NULL COMMENT 'refreshToken',
    `authentication` BLOB         NULL COMMENT 'authentication',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8 COMMENT = 'api token';
CREATE UNIQUE INDEX UK_token_access ON `api_token` (`access_token`);
CREATE UNIQUE INDEX UK_token_refresh ON `api_token` (`refresh_token`);



