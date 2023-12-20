package top.bettercode.summer.test.web.support.packagescan

import org.junit.jupiter.api.Test
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import top.bettercode.summer.web.Response
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver

/**
 * @author Peter Wu
 */
class PackageScanClassResolverTest {
    val packageScanClassResolver = PackageScanClassResolver()

    @Test
    fun findClass() {
        System.err.println(
                packageScanClassResolver.findImplementations(Response::class.java, "top.bettercode"))
    }

    @Test
    fun findResource() {
        val target = "*/**/*"
        val resources = PathMatchingResourcePatternResolver(
                Response::class.java.classLoader)
                .getResources("classpath*:$target.class")
        for (resource in resources) {
            try {
                System.err.println(resource.uri)
            } catch (_: Exception) {
            }
        }
    }

    @Test
    fun findClass2() {
    }
}