package top.bettercode.summer.web.support.packagescan

import org.apache.tomcat.util.buf.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.util.ClassUtils
import org.springframework.util.SystemPropertyUtils
import java.io.IOException
import java.util.*

open class PackageScanClassResolver @JvmOverloads constructor(classLoader: ClassLoader? = ClassUtils.getDefaultClassLoader()) {
    private val log = LoggerFactory.getLogger(PackageScanClassResolver::class.java)
    private var scanFilters: MutableSet<PackageScanFilter>? = null
    private val allClassesByPackage: MutableMap<String, MutableSet<Class<*>>> = HashMap()
    private val loadedPackages: MutableSet<String> = HashSet()
    private val resourcePatternResolver: ResourcePatternResolver
    private val metadataReaderFactory: MetadataReaderFactory

    init {
        resourcePatternResolver = PathMatchingResourcePatternResolver(classLoader)
        metadataReaderFactory = CachingMetadataReaderFactory(resourcePatternResolver)
    }

    fun addFilter(filter: PackageScanFilter) {
        if (scanFilters == null) {
            scanFilters = LinkedHashSet()
        }
        scanFilters!!.add(filter)
    }

    fun removeFilter(filter: PackageScanFilter) {
        if (scanFilters != null) {
            scanFilters!!.remove(filter)
        }
    }

    fun findImplementations(parent: Class<*>, vararg packageNames: String): Set<Class<*>> {
        if (packageNames.isEmpty()) {
            return emptySet()
        }
        if (log.isTraceEnabled) {
            log.trace("Searching for implementations of " + parent.name + " in packages: " + listOf(*packageNames))
        }
        val test = getCompositeFilter(AssignableToPackageScanFilter(parent))
        return findByFilter(test, *packageNames)
    }

    fun findByFilter(filter: PackageScanFilter, vararg packageNames: String): Set<Class<*>> {
        if (packageNames.isEmpty()) {
            return emptySet()
        }
        val classes: MutableSet<Class<*>> = LinkedHashSet()
        for (pkg in packageNames) {
            find(filter, pkg, classes)
        }
        if (log.isTraceEnabled) {
            log.trace("Found: $classes")
        }
        return classes
    }

    protected fun find(test: PackageScanFilter, packageName: String, classes: MutableSet<Class<*>>) {
        var name = packageName
        name = name.replace('.', '/')
        if (!loadedPackages.contains(name)) {
            findAllClasses(name)
            loadedPackages.add(name)
        }
        findInAllClasses(test, name, classes)
    }

    protected fun findAllClasses(packageName: String) {
        try {
            val packageSearchPath = (ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(packageName) + "/"
                    + "**/*.class")
            val resources = resourcePatternResolver.getResources(packageSearchPath)
            for (resource in resources) {
                if (resource.isReadable) {
                    val metadataReader = metadataReaderFactory.getMetadataReader(resource)
                    val className = metadataReader.classMetadata.className
                    try {
                        val type = Class.forName(className)
                        addFoundClass(type)
                    } catch (e: ClassNotFoundException) {
                        if (log.isTraceEnabled) {
                            log.trace("加载" + className + "失败，" + e.message)
                        }
                    } catch (e: NoClassDefFoundError) {
                        if (log.isTraceEnabled) {
                            log.trace("加载" + className + "失败，" + e.message)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            log.warn("Cannot read package: $packageName", e)
        }
    }

    private fun resolveBasePackage(basePackage: String): String {
        return ClassUtils
                .convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage))
    }

    protected fun findInAllClasses(test: PackageScanFilter, packageName: String,
                                   classes: MutableSet<Class<*>>) {
        if (log.isTraceEnabled) {
            log.trace("Searching for: $test in package: $packageName")
        }
        val packageClasses = getFoundClasses(packageName)
        if (packageClasses.isEmpty()) {
            if (log.isTraceEnabled) {
                log.trace("No classes found in package: $packageName")
            }
            return
        }
        for (type in packageClasses) {
            if (test.matches(type)) {
                classes.add(type)
            }
        }
    }

    protected fun addFoundClass(type: Class<*>) {
        if (type.getPackage() != null) {
            val packageName = type.getPackage().name
            val packageNameParts = listOf(*packageName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            for (i in packageNameParts.indices) {
                val thisPackage = StringUtils.join(packageNameParts.subList(0, i + 1), '/')
                addFoundClass(thisPackage, type)
            }
        }
    }

    protected fun addFoundClass(packageName: String, type: Class<*>) {
        var name = packageName
        name = name.replace("/", ".")
        if (!allClassesByPackage.containsKey(name)) {
            allClassesByPackage[name] = HashSet()
        }
        allClassesByPackage[name]!!.add(type)
    }

    protected fun getFoundClasses(packageName: String): Set<Class<*>> {
        var name = packageName
        name = name.replace("/", ".")
        return allClassesByPackage[name]!!
    }

    private fun getCompositeFilter(filter: PackageScanFilter): PackageScanFilter {
        if (scanFilters != null) {
            val composite = CompositePackageScanFilter(scanFilters!!)
            composite.addFilter(filter)
            return composite
        }
        return filter
    }
}
