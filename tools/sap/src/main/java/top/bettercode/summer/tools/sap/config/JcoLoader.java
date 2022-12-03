package top.bettercode.summer.tools.sap.config;

import com.sap.conn.jco.rt.JCoRuntime;
import com.sap.conn.jco.rt.JCoRuntimeFactory;
import java.io.File;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import top.bettercode.summer.tools.lang.util.OS;

/**
 * Jco native library extractor and loader.
 */
public class JcoLoader {

  private static final Logger log = LoggerFactory.getLogger(JcoLoader.class);


  /**
   * Extract and load native jco library in the provided folder.
   *
   * @throws Exception The provisioning failure exception.
   */
  public static synchronized void loadJco()
      throws Exception {
    File targetFolder = new File(
        System.getProperty("user.dir") + File.separator + "build" + File.separator + "native");
    if (!targetFolder.exists()) {
      targetFolder.mkdirs();
    }
    String libraryName;
    OS currentOs = OS.CURRENT_OS;
    switch (currentOs) {
      case MAC:
        libraryName = "libsapjco3.jnilib";
        break;
      case WINDOWS:
        libraryName = "sapjco3.dll";
        break;
      default:
        libraryName = "libsapjco3.so";
        break;
    }

    File targetPath = new File(targetFolder, libraryName).getAbsoluteFile();
    if (!targetPath.exists()) {
      Files.copy(new ClassPathResource("/native/" + libraryName).getInputStream(),
          targetPath.toPath());
    }

    String libraryPath = targetFolder.getAbsolutePath();

    String JCO_NATIVE_SYSTEM_PROPERTY = "java.library.path";
    String jcoSystemNativePath = System.getProperty(JCO_NATIVE_SYSTEM_PROPERTY);
    String pathSeparator;
    if (OS.WINDOWS.isCurrentOs()) {
      pathSeparator = ";";
    } else {
      pathSeparator = ":";
    }
    if (!jcoSystemNativePath.contains(pathSeparator + libraryPath)
        && !jcoSystemNativePath.startsWith(libraryPath + pathSeparator)) {
      jcoSystemNativePath += pathSeparator + libraryPath;
      System.setProperty(JCO_NATIVE_SYSTEM_PROPERTY, jcoSystemNativePath);
    }

    log.info("Jco system native path: " + System.getProperty(JCO_NATIVE_SYSTEM_PROPERTY));

    if (isJcoAlreadyLoaded()) {
      log.info("Jco library is already loaded.");
    }
  }


  private static boolean isJcoAlreadyLoaded() {
    try {
      JCoRuntime runtime = JCoRuntimeFactory.getRuntime();
      return runtime != null;
    } catch (Exception e) {
      log.error("Failed to load Jco library", e);
      return false;
    }
  }
}
