import jpa.unit.mapperXml
import jpa.unit.repository
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter

/**
 * @author Peter Wu
 */
open class Service : ModuleJavaGenerator() {

    override fun content() {
        file(mapperXmlName, isResourcesFile = true) {
            mapperXml(this)
        }

        packageInfo(modulePackageInfoType) {
            modulePackageInfo(this)
        }
        packageInfo(packageInfoType) {
            packageInfo(this)
        }

        interfaze(repositoryType) {
            repository(this)
        }

        clazz(serviceType) {
            annotation("@org.springframework.stereotype.Service")
            javadoc {
                +"/**"
                +" * $remarks 服务层实现"
                +" */"
            }
            superClass =
                JavaType("top.bettercode.simpleframework.data.jpa.BaseService").typeArgument(
                    entityType,
                    primaryKeyType,
                    repositoryType
                )


            //constructor
            constructor(Parameter("repository", repositoryType)) {
                +"super(repository);"
            }
        }
    }
}