import top.bettercode.generator.dom.java.element.PackageInfo

/**
 * @author Peter Wu
 */
val modulePackageInfo: ModuleJavaGenerator.(PackageInfo) -> Unit = { unit ->
    unit.apply {
        +"""/**
 * $moduleName
 */
package ${type.packageName};"""
    }
}

val packageInfo: ModuleJavaGenerator.(PackageInfo) -> Unit = { unit ->
    unit.apply {
        +"""/**
 * $remarks
 */
package ${type.packageName};"""
    }
}
