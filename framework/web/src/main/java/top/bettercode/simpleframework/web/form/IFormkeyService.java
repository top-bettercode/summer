package top.bettercode.simpleframework.web.form;

/**
 * @author Peter Wu
 */
public interface IFormkeyService {

  boolean exist(String formkey);

  void remove(String formkey);
}
