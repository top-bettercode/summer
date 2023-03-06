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
 * native library extractor and loader.
 */
public class SapNativeLibLoader {

  private static final Logger log = LoggerFactory.getLogger(SapNativeLibLoader.class);
  private static final String LIB_NAME = "Jco";

  /**
   * Extract and load native library in the provided folder.
   *
   * @throws Exception The provisioning failure exception.
   */
  public static synchronized void loadNativeLib()
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

    String nativeSystemProperty = "java.library.path";
    String systemNativePath = System.getProperty(nativeSystemProperty);
    String pathSeparator;
    if (OS.WINDOWS.isCurrentOs()) {
      pathSeparator = ";";
    } else {
      pathSeparator = ":";
    }
    if (!systemNativePath.contains(pathSeparator + libraryPath)
        && !systemNativePath.startsWith(libraryPath + pathSeparator)) {
      systemNativePath += pathSeparator + libraryPath;
      System.setProperty(nativeSystemProperty, systemNativePath);
    }

    log.info(LIB_NAME + " system native path: " + System.getProperty(nativeSystemProperty));

    if (isAlreadyLoaded()) {
      log.info(LIB_NAME + " library is already loaded.");
    }
  }


  private static boolean isAlreadyLoaded() {
    try {
      JCoRuntime runtime = JCoRuntimeFactory.getRuntime();
      return runtime != null;
    } catch (Exception e) {
      log.error("Failed to load " + LIB_NAME + " library", e);
      return false;
    }
  }
}
