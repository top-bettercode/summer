package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.*
import top.bettercode.summer.gradle.plugin.project.template.unit.*
import top.bettercode.summer.tools.generator.dom.unit.SourceSet

/**
 * @author Peter Wu
 */
open class Service(private val overwrite: Boolean = false) : ProjectGenerator() {

    override fun content() {
        +file(mapperXmlName, overwrite = overwrite, sourceSet = SourceSet.MAIN) {
            mapperXml(this)
        }

        +packageInfo(modulePackageInfoType, overwrite = false) {
            modulePackageInfo(this)
        }
        +packageInfo(packageInfoType, overwrite = false) {
            packageInfo(this)
        }

        +interfaze(repositoryType, overwrite = false) {
            repository(this)
        }

        if (interfaceService)
            +interfaze(iserviceType, overwrite = false) {
                iservice(this)
            }

        +clazz(serviceType, overwrite = false) {
            service(this)
        }

        +clazz(testServiceType, overwrite = overwrite, sourceSet = SourceSet.TEST) {
            testService(this)
        }

        if (!isCore) {
            //form
            +clazz(formType, overwrite = overwrite) {
                form(this)
            }
            +interfaze(mixInType, overwrite = overwrite) {
                mixIn(this)
            }
            if (serializationViewsType != coreSerializationViewsType)
                +interfaze(serializationViewsType, overwrite = false) {
                    serializationViews(this)
                }
        }
    }
}
