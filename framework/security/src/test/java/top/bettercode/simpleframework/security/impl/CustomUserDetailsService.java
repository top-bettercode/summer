package top.bettercode.simpleframework.security.impl;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import top.bettercode.simpleframework.security.AdditionalUserDetails;
import top.bettercode.simpleframework.security.DefaultAuthority;
import top.bettercode.simpleframework.security.IllegalUserException;

/**
 * 自定义UserDetailsService
 *
 * @author Peter Wu
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {


  /**
   * @param username 用户名
   * @return UserDetails
   * @throws UsernameNotFoundException 未找到用户
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if ("disableUsername".equals(username)) {
      throw new IllegalUserException("帐户已禁用");
    }
    AdditionalUserDetails additionalUserDetails = new AdditionalUserDetails(username,
        DigestUtils.md5DigestAsHex("123456".getBytes()),
        getAuthorities(username));
    additionalUserDetails.put("key", "value");
    return additionalUserDetails;
  }

  public Collection<? extends GrantedAuthority> getAuthorities(String username) {
    if (username.equals("root")) {
      return DefaultAuthority.addDefaultAuthority(new SimpleGrantedAuthority("a"));
    }
    return DefaultAuthority.defaultAuthority();
  }
}