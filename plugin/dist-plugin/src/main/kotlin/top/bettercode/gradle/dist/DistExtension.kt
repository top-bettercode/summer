package top.bettercode.gradle.dist


/**
 * @author Peter Wu
 */
open class DistExtension(
        var unwrapResources: Boolean = true,
        var includeJre: Boolean = false,
        var x64: Boolean = true,
        var windows: Boolean = false,
        /**
         * 相对当前项目的路径
         */
        var nativePath: String = "native",
        /**
         * 运行用户
         */
        var runUser: String = "",
        var jreWindowsI586Gz: String = "",
        var jreWindowsX64Gz: String = "",
        var jreLinuxI586Gz: String = "",
        var jreLinuxX64Gz: String = "",
        /**
         * windows service 老版本路径 用于生成更新包
         */
        var windowsServiceOldPath: String = "",
        /**
         * dist 老版本路径 用于生成更新包
         */
        var distOldPath: String = "",
        var jvmArgs: List<String> = listOf())
