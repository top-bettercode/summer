package top.bettercode.simpleframework.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class AdditionalUserDetails extends User {

  private static final long serialVersionUID = 1L;

  private final Map<String, Object> additionalInformation = new HashMap<>();

  public AdditionalUserDetails(String username, String password,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
  }

  public AdditionalUserDetails(String username, String password, boolean enabled,
      boolean accountNonExpired,
      boolean credentialsNonExpired, boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
        authorities);
  }

  public void put(String key, Object value) {
    this.additionalInformation.put(key, value);
  }

  public Object get(String key){
    return this.additionalInformation.get(key);
  }

  public Map<String, Object> getAdditionalInformation() {
    return additionalInformation;
  }
}
