package top.bettercode.simpleframework.security.server;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.ECKey.Builder;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@ConditionalOnProperty(prefix = "summer.security.key-store", value = "resource-path")
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties(KeyStoreProperties.class)
public class KeyStoreConfiguration {

  @Bean
  public JWKSource<SecurityContext> jwkSource(KeyStoreProperties keyStoreProperties) {
    KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource(
        keyStoreProperties.getResourcePath()), keyStoreProperties.getPassword().toCharArray())
        .getKeyPair(keyStoreProperties.getAlias());
    ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
    ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
    Curve curve = Curve.forECParameterSpec(publicKey.getParams());
    ECKey ecKey = new Builder(curve, publicKey)
        .privateKey(privateKey)
        .keyID(UUID.randomUUID().toString())
        .build();
    JWKSet jwkSet = new JWKSet(ecKey);
    return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
  }

  private static class KeyStoreKeyFactory {

    private final Logger logger = LoggerFactory.getLogger(KeyStoreKeyFactory.class);

    private final Resource resource;

    private final char[] password;

    private KeyStore store;

    private final Object lock = new Object();

    public KeyStoreKeyFactory(Resource resource, char[] password) {
      this.resource = resource;
      this.password = password;
    }

    public KeyPair getKeyPair(String alias) {
      return getKeyPair(alias, password);
    }

    public KeyPair getKeyPair(String alias, char[] password) {
      InputStream inputStream = null;
      try {
        synchronized (lock) {
          if (store == null) {
            synchronized (lock) {
              store = KeyStore.getInstance("jks");
              inputStream = resource.getInputStream();
              store.load(inputStream, this.password);
            }
          }
        }
        RSAPrivateCrtKey key = (RSAPrivateCrtKey) store.getKey(alias, password);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
        return new KeyPair(publicKey, key);
      } catch (Exception e) {
        throw new IllegalStateException("Cannot load keys from store: " + resource, e);
      } finally {
        try {
          if (inputStream != null) {
            inputStream.close();
          }
        } catch (IOException e) {
          logger.warn("Cannot close open stream: ", e);
        }
      }
    }
  }

}