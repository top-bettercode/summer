package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.*
import top.bettercode.summer.tools.generator.dom.java.element.Interface
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.unit.PropertiesUnit

/**
 * @author Peter Wu
 */
class Entity : ProjectGenerator() {

    override fun setUp() {
        add(properties(msgName, true) { load(ext.projectDir) })
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
        msg(this[msgName] as PropertiesUnit)
        coreSerializationViews(this[coreSerializationViewsType.unitName] as Interface)

        //entityClass
        +clazz(entityType, true) {
            entity(this)
        }

        //primaryKeyClass
        if (isCompositePrimaryKey)
            +clazz(primaryKeyType, true) {
                compositePrimaryKey(this)
            }

        +clazz(matcherType, true) {
            matcher(this)
        }

        //propertiesInterface
        +interfaze(propertiesType, true) {
            properties(this)
        }

        +interfaze(methodInfoType, true) {
            methodInfo(this)
        }

        +packageInfo(modulePackageInfoType) {
            modulePackageInfo(this)
        }
        +packageInfo(packageInfoType) {
            packageInfo(this)
        }
    }
}