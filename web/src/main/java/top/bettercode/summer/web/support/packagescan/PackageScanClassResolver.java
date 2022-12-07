package top.bettercode.summer.web.support.packagescan;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

public class PackageScanClassResolver {

  private final Logger log = LoggerFactory.getLogger(PackageScanClassResolver.class);
  private Set<PackageScanFilter> scanFilters;
  private final Map<String, Set<Class<?>>> allClassesByPackage = new HashMap<>();
  private final Set<String> loadedPackages = new HashSet<>();
  private final ResourcePatternResolver resourcePatternResolver;
  private final MetadataReaderFactory metadataReaderFactory;

  public PackageScanClassResolver() {
    this(ClassUtils.getDefaultClassLoader());
  }

  public PackageScanClassResolver(ClassLoader classLoader) {
    resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);
    metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
  }

  public void addFilter(PackageScanFilter filter) {
    if (scanFilters == null) {
      scanFilters = new LinkedHashSet<>();
    }
    scanFilters.add(filter);
  }

  public void removeFilter(PackageScanFilter filter) {
    if (scanFilters != null) {
      scanFilters.remove(filter);
    }
  }

  public Set<Class<?>> findImplementations(Class<?> parent, String... packageNames) {
    if (packageNames == null) {
      return Collections.emptySet();
    }

    if (log.isTraceEnabled()) {
      log.trace("Searching for implementations of " + parent.getName() + " in packages: " + Arrays
          .asList(packageNames));
    }

    PackageScanFilter test = getCompositeFilter(new AssignableToPackageScanFilter(parent));

    return findByFilter(test, packageNames);
  }

  public Set<Class<?>> findByFilter(PackageScanFilter filter, String... packageNames) {
    if (packageNames == null) {
      return Collections.emptySet();
    }

    Set<Class<?>> classes = new LinkedHashSet<>();
    for (String pkg : packageNames) {
      find(filter, pkg, classes);
    }

    if (log.isTraceEnabled()) {
      log.trace("Found: " + classes);
    }

    return classes;
  }

  protected void find(PackageScanFilter test, String packageName, Set<Class<?>> classes) {
    packageName = packageName.replace('.', '/');

    if (!loadedPackages.contains(packageName)) {
      this.findAllClasses(packageName);
      loadedPackages.add(packageName);
    }

    findInAllClasses(test, packageName, classes);
  }

  protected void findAllClasses(String packageName) {
    try {
      String packageSearchPath =
          ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(packageName) + "/"
              + "**/*.class";
      Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
      for (Resource resource : resources) {
        if (resource.isReadable()) {
          MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
          String className = metadataReader.getClassMetadata().getClassName();
          try {
            Class<?> type = Class.forName(className);
            addFoundClass(type);
          } catch (ClassNotFoundException | NoClassDefFoundError e) {
            if (log.isTraceEnabled()) {
              log.trace("加载" + className + "失败，" + e.getMessage());
            }
          }
        }
      }
    } catch (IOException e) {
      log.warn("Cannot read package: " + packageName, e);
    }
  }

  private String resolveBasePackage(String basePackage) {
    return ClassUtils
        .convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
  }

  protected void findInAllClasses(PackageScanFilter test, String packageName,
      Set<Class<?>> classes) {
    if (log.isTraceEnabled()) {
      log.trace("Searching for: " + test + " in package: " + packageName);
    }

    Set<Class<?>> packageClasses = getFoundClasses(packageName);
    if (packageClasses == null) {
      if (log.isTraceEnabled()) {
        log.trace("No classes found in package: " + packageName);
      }
      return;
    }
    for (Class<?> type : packageClasses) {
      if (test.matches(type)) {
        classes.add(type);
      }
    }

  }

  protected void addFoundClass(Class<?> type) {
    if (type.getPackage() != null) {
      String packageName = type.getPackage().getName();
      List<String> packageNameParts = Arrays.asList(packageName.split("\\."));
      for (int i = 0; i < packageNameParts.size(); i++) {
        String thisPackage = StringUtils.join(packageNameParts.subList(0, i + 1), '/');
        addFoundClass(thisPackage, type);
      }
    }
  }

  protected void addFoundClass(String packageName, Class<?> type) {
    packageName = packageName.replace("/", ".");

    if (!this.allClassesByPackage.containsKey(packageName)) {
      this.allClassesByPackage.put(packageName, new HashSet<>());
    }

    this.allClassesByPackage.get(packageName).add(type);
  }

  protected Set<Class<?>> getFoundClasses(String packageName) {
    packageName = packageName.replace("/", ".");
    return this.allClassesByPackage.get(packageName);
  }

  private PackageScanFilter getCompositeFilter(PackageScanFilter filter) {
    if (scanFilters != null) {
      CompositePackageScanFilter composite = new CompositePackageScanFilter(scanFilters);
      composite.addFilter(filter);
      return composite;
    }
    return filter;
  }

}
