package top.bettercode.simpleframework.web.form;

/**
 * @author Peter Wu
 */
public interface IFormkeyService {

   String putKey(String formkey);

   boolean exist(String formkey);

}
