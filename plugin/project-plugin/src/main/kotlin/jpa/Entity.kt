import jpa.unit.*
import top.bettercode.generator.dom.unit.PropertiesUnit

/**
 * @author Peter Wu
 */
class Entity : ProjectGenerator() {

    override fun setUp() {
        add(properties(msgName, overwrite = true) { load(ext.projectDir) })
    }

    override fun content() {
        msg(this[msgName] as PropertiesUnit)

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