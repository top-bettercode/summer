package top.bettercode.summer.test.web.support.packagescan

import org.junit.jupiter.api.Test
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import top.bettercode.summer.web.*
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
    @Throws(Exception::class)
    fun findResource() {
        val target = "*/**/*"
        val resources = PathMatchingResourcePatternResolver(
                Response::class.java.classLoader)
                .getResources("classpath*:$target.class")
        for (resource in resources) {
            System.err.println(resource.uri)
        }
    }

    @Test
    fun findClass2() {
    }
}