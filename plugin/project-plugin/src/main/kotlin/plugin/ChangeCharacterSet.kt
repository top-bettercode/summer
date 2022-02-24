package plugin

import top.bettercode.generator.dom.unit.FileUnit
import top.bettercode.generator.dom.unit.SourceSet
import top.bettercode.generator.dsl.Generator

/**
 * <pre>
[client]
default-character-set = utf8mb4
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
[mysql]
default-character-set = utf8mb4
 * </pre>
 * MySQL Innodb 的索引长度限制为 767 字节，UTF8mb4 字符集是 4 个字节，
767 字节 / 4 字节每字符 = 191 字符（即默认的索引最大长度）
 * @author Peter Wu
 */
class ChangeCharacterSet : Generator() {

    private val name = "database/change_character_set.sql"

    override fun setUp() {
        add(file(name)).apply {
            +"# 修改数据库表及字段字符集\n"
            +"ALTER DATABASE ${
                datasource.url.replace(
                    Regex(".*/(.+?)\\?.*"),
                    "$1"
                )
            } CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;"
            +""
        }
    }

    override fun call() {
        (this[name] as FileUnit).apply {
            +"ALTER TABLE $tableName CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
            +""
            columns.forEach {
                if (it.typeName == "VARCHAR" || it.typeName == "TEXT") {
                    +"ALTER TABLE $tableName CHANGE ${it.columnName} ${it.columnName} ${it.typeName}${if (it.columnSize > 0) "(${it.columnSize}${if (it.decimalDigits > 0) ",${it.decimalDigits}" else ""})" else ""} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
                    +""
                }
            }
        }
    }


}

