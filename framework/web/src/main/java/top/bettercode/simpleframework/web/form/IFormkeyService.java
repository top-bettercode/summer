package top.bettercode.simpleframework.web.form;

/**
 * @author Peter Wu
 */
public interface IFormkeyService {

  boolean exist(String formkey, long expireSeconds);

  void remove(String formkey);
}
