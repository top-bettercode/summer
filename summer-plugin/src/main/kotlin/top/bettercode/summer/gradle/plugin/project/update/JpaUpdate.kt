package top.bettercode.summer.gradle.plugin.project.update

import org.gradle.api.Project
import top.bettercode.summer.gradle.plugin.project.DicCodeGen

/**
 *
 * @author Peter Wu
 */
class JpaUpdate {

    fun update(project: Project) {
        val replaceCodeNames: MutableMap<String, String> = mutableMapOf()
        project.logger.lifecycle("更新代码")

        replaceCodeNames["lowLevelUpdate"] = "updateLowLevel"
        replaceCodeNames["physicalUpdate"] = "updatePhysical"
        replaceCodeNames["dynamicSave"] = "saveDynamic"
        replaceCodeNames["physicalDelete"] = "deletePhysical"


        DicCodeGen.replaceOld(project, replaceCodeNames)
        project.logger.lifecycle("更新代码完成")
    }
}