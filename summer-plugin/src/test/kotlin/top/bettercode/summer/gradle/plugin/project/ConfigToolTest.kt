package top.bettercode.summer.gradle.plugin.project

import org.junit.jupiter.api.Test
import java.io.File

/**
 *
 * @author Peter Wu
 */
internal class ConfigToolTest {

    @Test
    fun prettyConfig() {
        ConfigTool.prettyConfig(
                File("/data/repositories/bettercode/wintruelife/acitve/npk/conf"),
                listOf(
                        File("/data/repositories/bettercode/wintruelife/acitve/npk/admin/src/main/resources/application.yml"),
                        File("/data/repositories/bettercode/wintruelife/acitve/npk/app/src/main/resources/application.yml")
                )
        )
    }
}