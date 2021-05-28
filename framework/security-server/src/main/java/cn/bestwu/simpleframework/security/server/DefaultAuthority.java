package cn.bestwu.simpleframework.security.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author Peter Wu
 */
public class DefaultAuthority {

  public static final String DEFAULT_AUTHORITY_STRING = "authenticated";
  public static final GrantedAuthority DEFAULT_GRANTED_AUTHORITY = new SimpleGrantedAuthority(
      DEFAULT_AUTHORITY_STRING);


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
