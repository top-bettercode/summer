package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.dom.java.element.PackageInfo

/**
 * @author Peter Wu
 */
val modulePackageInfo: ProjectGenerator.(PackageInfo) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * ${table.subModuleName}"
            +" */"
        }
    }
}

val packageInfo: ProjectGenerator.(PackageInfo) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks"
            +" */"
        }
    }
}
