package top.bettercode.summer.security.support;

public interface SecurityParameterNames {

  /**
   * {@code grant_type} - used in Access Token Request.
   */
  String GRANT_TYPE = "grant_type";

  /**
   * {@code scope} - used in Authorization Request, Authorization Response, Access Token Request and
   * Access Token Response.
   */
  String SCOPE = "scope";

  /**
   * {@code access_token} - used in Authorization Response and Access Token Response.
   */
  String ACCESS_TOKEN = "access_token";

  /**
   * 兼容toekn名称
   */
  String COMPATIBLE_ACCESS_TOKEN = "accessToken";

  /**
   * {@code refresh_token} - used in Access Token Request and Access Token Response.
   */
  String REFRESH_TOKEN = "refresh_token";

  /**
   * {@code username} - used in Access Token Request.
   */
  String USERNAME = "username";

	/**
	 * {@code password} - used in Access Token Request.
	 */
	String PWDNAME = "password";

}
