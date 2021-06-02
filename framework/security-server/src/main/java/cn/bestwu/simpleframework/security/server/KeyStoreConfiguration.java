package cn.bestwu.simpleframework.security.server;

import java.security.KeyPair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

@Deprecated
@ConditionalOnProperty(prefix = "summer.security.key-store", value = "resource-path")
@ConditionalOnClass(KeyStoreKeyFactory.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties(KeyStoreProperties.class)
public class KeyStoreConfiguration {

  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter(KeyStoreProperties keyStoreProperties) {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource(
        keyStoreProperties.getResourcePath()), keyStoreProperties.getPassword().toCharArray())
        .getKeyPair(keyStoreProperties.getAlias());
    converter.setKeyPair(keyPair);
    return converter;
  }


}