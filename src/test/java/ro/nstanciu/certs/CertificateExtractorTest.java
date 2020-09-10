package ro.nstanciu.certs;

import java.io.File;
import java.security.cert.X509Certificate;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.micronaut.test.annotation.MicronautTest;
import ro.nstanciu.errors.InternalException;
import ro.nstanciu.utils.TestConstants;

@MicronautTest
public class CertificateExtractorTest {
  @Inject
  private CertificateExtractor certExtractor;

  @Test
  @DisplayName("Should be able to read certificate")
  public void readCert() {
    var cert = certExtractor.readCertificate(TestConstants.CERT_FILE);
    Assertions.assertThat(cert).isInstanceOf(X509Certificate.class);
    Assertions.assertThat(cert).isNotNull();
  }

  @Test
  @DisplayName("Should throw InternalException when file not found")
  public void readCertWrong() {

    Assertions.assertThatThrownBy(() -> {
      certExtractor.readCertificate(new File(TestConstants.CERT_PATH + "wrong"));
    }).isInstanceOf(InternalException.class);
  }
}
