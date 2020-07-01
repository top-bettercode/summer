package cn.bestwu.simpleframework.security.server;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * AuthorizationServer 配置适配
 *
 * @author Peter Wu
 */
public interface AuthorizationServerHttpSecurityConfigurerAdapter {

	void configure(HttpSecurity http);
}
