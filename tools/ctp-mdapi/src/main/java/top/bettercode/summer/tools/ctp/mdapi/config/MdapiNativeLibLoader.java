package top.bettercode.summer.tools.ctp.mdapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * native library extractor and loader.
 */
public class MdapiNativeLibLoader {

  private static final Logger log = LoggerFactory.getLogger(MdapiNativeLibLoader.class);
  private static final String LIB_NAME = "CTP-API";


  /**
   * Extract and load native library in the provided folder.
   *
   */
  public static synchronized void loadNativeLib() {
    if (isAlreadyLoaded()) {
      log.info(LIB_NAME + " library is already loaded.");
    }
  }


  private static boolean isAlreadyLoaded() {
    try {
      System.loadLibrary("thostmduserapi_se");
      System.loadLibrary("thostmduserapi_wrap");
      return true;
    } catch (Exception e) {
      log.error("Failed to load " + LIB_NAME + " library", e);
      return false;
    }
  }
}
