package top.bettercode.simpleframework.web.form;

/**
 * @author Peter Wu
 */
public interface IFormKeyService {

   String putKey(String formKey);

   boolean exist(String formKey);

}
