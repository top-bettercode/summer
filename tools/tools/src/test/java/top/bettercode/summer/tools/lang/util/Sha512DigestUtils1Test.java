package top.bettercode.summer.tools.lang.util;

import org.junit.jupiter.api.Test;

/**
 * @author Peter Wu
 */
public class Sha512DigestUtils1Test {

  @Test
  void shaHex() {
    System.err.println(
        Sha512DigestUtils.shaHex("552ab01dc30855d207f545bd881c455fPOST/outStores/create"));
    System.err.println(
        Sha512DigestUtils.shaHex("552ab01dc30855d207f545bd881c455fPOST/outStores/create"));
    System.err.println(
        Sha512DigestUtils.shaHex("552ab01dc30855d207f545bd881c455fPOST/outStores/create"));
  }
}
