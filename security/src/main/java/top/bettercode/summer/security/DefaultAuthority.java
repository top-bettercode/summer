package top.bettercode.summer.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author Peter Wu
 */
public class DefaultAuthority {

  public static GrantedAuthority DEFAULT_GRANTED_AUTHORITY = new SimpleGrantedAuthority(
      "authenticated");
  public static SecurityConfig DEFAULT_AUTHENTICATED = new SecurityConfig("authenticated");
  public static final SecurityConfig ROLE_ANONYMOUS = new SecurityConfig("ROLE_ANONYMOUS");

  public static boolean isDefaultAuthority(String authority) {
    return DEFAULT_GRANTED_AUTHORITY.getAuthority().equals(authority);
  }

  public static boolean isDefaultAuthority(GrantedAuthority authority) {
    return DEFAULT_GRANTED_AUTHORITY.equals(authority);
  }

  public static Collection<? extends GrantedAuthority> defaultAuthority() {
    return Collections.singleton(DEFAULT_GRANTED_AUTHORITY);
  }


  public static Collection<? extends GrantedAuthority> addDefaultAuthority(
      GrantedAuthority... authorities) {
    HashSet<GrantedAuthority> objects = new HashSet<>(Arrays.asList(authorities));
    objects.add(DEFAULT_GRANTED_AUTHORITY);
    return objects;
  }


  public static Collection<? extends GrantedAuthority> addDefaultAuthority(
      String... authorities) {
    HashSet<GrantedAuthority> objects = new HashSet<>();
    for (String authority : authorities) {
      objects.add(new SimpleGrantedAuthority(authority));
    }
    objects.add(DEFAULT_GRANTED_AUTHORITY);
    return objects;
  }

  public static Collection<? extends GrantedAuthority> addDefaultAuthority(
      Collection<String> authorities) {
    HashSet<GrantedAuthority> objects = new HashSet<>();
    for (String authority : authorities) {
      objects.add(new SimpleGrantedAuthority(authority));
    }
    objects.add(DEFAULT_GRANTED_AUTHORITY);
    return objects;
  }
}
