package top.bettercode.summer.test.web.support.packagescan

import org.junit.jupiter.api.Assertions
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
        val findImplementations =
            packageScanClassResolver.findImplementations(Response::class.java, "top.bettercode")
        Assertions.assertTrue(findImplementations.isNotEmpty())
//        System.err.println(findImplementations)
    }

    @Test
    fun findResource() {
        val target = "*/**/*"
        val resources = PathMatchingResourcePatternResolver(
            Response::class.java.classLoader
        ) .getResources("classpath*:$target.class")
        Assertions.assertTrue(resources.isNotEmpty())
//        for (resource in resources) {
//            try {
//                System.err.println(resource.uri)
//            } catch (_: Exception) {
//            }
//        }
    }

}