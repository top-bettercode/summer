package plugin

import top.bettercode.generator.dsl.Generator
import java.io.File

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
    private val destFile: File
        get() = File(extension.basePath.parentFile, "database/change_character_set.sql")

    override fun setUp() {
        System.err.println(destFile)
        destFile.parentFile.mkdirs()
        destFile.writeText("# 修改数据库表及字段字符集\n")
        appendln(
            "ALTER DATABASE ${
                extension.datasource.url.replace(
                    Regex(".*/(.+?)\\?.*"),
                    "$1"
                )
            } CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;"
        )
        appendln("")
    }

    override fun call() {
        appendln("ALTER TABLE $tableName CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
        appendln("")
        columns.forEach {
            if (it.typeName == "VARCHAR" || it.typeName == "TEXT") {
                appendln("ALTER TABLE $tableName CHANGE ${it.columnName} ${it.columnName} ${it.typeName}${if (it.columnSize > 0) "(${it.columnSize}${if (it.decimalDigits > 0) ",${it.decimalDigits}" else ""})" else ""} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
                appendln("")
            }
        }
    }

    private fun appendln(text: String) {
        destFile.appendText("$text\n")
    }
}

