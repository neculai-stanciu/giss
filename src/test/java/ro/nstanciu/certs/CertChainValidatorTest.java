package ro.nstanciu.certs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CertChainValidatorTest {
  private KeyStore keyStore;

  @BeforeEach
  void initAll()
      throws IOException, URISyntaxException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
    var keyStoreUrl = ClassLoader.getSystemResource("keystore.jks");
    var keyStorePass = "keystore".toCharArray();
    var keyStoreFile = Path.of(keyStoreUrl.toURI()).toFile();
    this.keyStore = KeyStore.getInstance("JKS");
    this.keyStore.load(new FileInputStream(keyStoreFile), keyStorePass);
  }

  @Test
  @DisplayName("Keystore should be ok")
  void testKeystore() throws KeyStoreException {
    Assertions.assertThat(keyStore).isNotNull();
    Assertions.assertThat(keyStore.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Should contain a valid certificate")
  void validateKeyChain() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
      InvalidAlgorithmParameterException, NoSuchProviderException {
        var validCertificate = CertChainValidator.validateKeyChain((X509Certificate) keyStore.getCertificate("clientint21int2"), keyStore);
    if (validCertificate) {
      System.out.println("validate success");
      assertTrue(validCertificate, "Certificate chain is valid");
    } else {
      System.out.println("validate fail");
    }
  }
}
