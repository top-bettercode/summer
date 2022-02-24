import jpa.unit.mapperXml
import jpa.unit.repository

/**
 * @author Peter Wu
 */
open class Service : ProjectGenerator() {

    override fun content() {
        file(mapperXmlName) {
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
            service(this)
        }
    }
}
