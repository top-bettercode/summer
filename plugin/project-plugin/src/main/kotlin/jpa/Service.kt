import jpa.unit.mapperXml
import jpa.unit.repository
import top.bettercode.generator.dom.unit.SourceSet

/**
 * @author Peter Wu
 */
open class Service : ProjectGenerator() {

    override fun content() {
        +file(mapperXmlName, overwrite = false, sourceSet = SourceSet.MAIN) {
            mapperXml(this)
        }

        +packageInfo(modulePackageInfoType) {
            modulePackageInfo(this)
        }
        +packageInfo(packageInfoType) {
            packageInfo(this)
        }

        +interfaze(repositoryType) {
            repository(this)
        }

        if (interfaceService)
            +interfaze(iserviceType) {
                iservice(this)
            }

        +clazz(serviceType) {
            service(this)
        }
    }
}
