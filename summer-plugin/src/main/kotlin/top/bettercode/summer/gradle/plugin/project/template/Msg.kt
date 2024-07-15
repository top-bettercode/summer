package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.msg
import top.bettercode.summer.tools.generator.dom.unit.PropertiesUnit

/**
 * @author Peter Wu
 */
class Msg : ProjectGenerator() {

    override fun setUp() {
        add(properties(msgName, true) { load(ext.projectDir) })
    }

    override fun content() {
        msg(this[msgName] as PropertiesUnit)
    }
}