import top.bettercode.generator.dom.java.element.PackageInfo

/**
 * @author Peter Wu
 */
val modulePackageInfo: ProjectGenerator.(PackageInfo) -> Unit = { unit ->
    unit.apply {
        +"""/**
 * ${table.subModuleName}
 */
package ${type.packageName};"""
    }
}

val packageInfo: ProjectGenerator.(PackageInfo) -> Unit = { unit ->
    unit.apply {
        +"""/**
 * $remarks
 */
package ${type.packageName};"""
    }
}
