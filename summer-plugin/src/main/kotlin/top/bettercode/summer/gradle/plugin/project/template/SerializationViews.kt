package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.coreSerializationViews
import top.bettercode.summer.tools.generator.dom.java.element.Interface
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility

/**
 * @author Peter Wu
 */
open class SerializationViews : ProjectGenerator() {

    override fun setUp() {
        add(interfaze(coreSerializationViewsType, true) {
            javadoc {
                +"/**"
                +" * 模型属性 json SerializationViews"
                +" */"
            }
            this.visibility = JavaVisibility.PUBLIC
        })

    }

    override fun content() {
        coreSerializationViews(this[coreSerializationViewsType.unitName] as Interface)
    }
}