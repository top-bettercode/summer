package cn.bestwu.simpleframework.support.packagescan;

import cn.bestwu.simpleframework.web.Response;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @author Peter Wu
 */
public class PackageScanClassResolverTest {

  PackageScanClassResolver packageScanClassResolver = new PackageScanClassResolver();

  @Test
  public void findClass() {
    System.err.println(packageScanClassResolver.findImplementations(Response.class, "cn.bestwu"));
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