package top.bettercode.simpleframework.support.packagescan;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import top.bettercode.simpleframework.web.Response;

/**
 * @author Peter Wu
 */
public class PackageScanClassResolverTest {

  final PackageScanClassResolver packageScanClassResolver = new PackageScanClassResolver();

  @Test
  public void findClass() {
    System.err.println(packageScanClassResolver.findImplementations(Response.class, "top.bettercode"));
  }

  @Test
  public void findResource() throws Exception {
    String target = "*/**/*";
    Resource[] resources = new PathMatchingResourcePatternResolver(
        Response.class.getClassLoader())
        .getResources("classpath*:" + target + ".class");
    for (Resource resource : resources) {
      System.err.println(resource.getURI());
    }
  }

  @Test
  public void findClass2() {

  }
}