package cn.bestwu.simpleframework.security.impl;

import cn.bestwu.simpleframework.security.DefaultAuthority;
import cn.bestwu.simpleframework.security.server.IllegalUserException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 自定义UserDetailsService
 *
 * @author Peter Wu
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
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
    return new User(username, DigestUtils.md5DigestAsHex("123456".getBytes()),
        getAuthorities(username));
  }

  public Collection<? extends GrantedAuthority> getAuthorities(String username) {
    if (username.equals("root")) {
      Set<GrantedAuthority> authorities = new HashSet<>();
      authorities.add(new SimpleGrantedAuthority("a"));
      return authorities;
    }
    return Collections.singleton(DefaultAuthority.DEFAULT_GRANTED_AUTHORITY);
  }
}