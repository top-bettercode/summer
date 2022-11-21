package jpa.unit

import jpa.ProjectGenerator
import top.bettercode.generator.dom.java.element.PackageInfo

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
