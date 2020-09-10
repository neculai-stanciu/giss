package ro.nstanciu.certs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;

import javax.inject.Singleton;

import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nstanciu.errors.InternalException;

@Singleton
public class CertificateExtractor {
  private static final Logger log = LoggerFactory.getLogger(CertificateExtractor.class);

  public X509Certificate readCertificate(File certPath) {
    try (InputStream in = new FileInputStream(certPath)) {
      CertificateFactory factory = CertificateFactory.getInstance("X.509");
      return (X509Certificate) factory.generateCertificate(in);
    } catch (IOException e) {
      throw new InternalException("File provided cannot be read or not available.", e);
    } catch (CertificateException e) {
      throw new InternalException("File provided cannot be parsed.", e);
    }
  }

  public Optional<URL> extractAuthorityInformationAccessIssuerUrl(final X509Certificate cert) {
    // get Authority Information Access extension (will be null if extension is not
    // present)
    AuthorityInformationAccess aia = extractAuthorityInformationAccess(cert);

    // check if there is a URL to issuer's certificate
    AccessDescription[] descriptions = aia.getAccessDescriptions();
    for (AccessDescription ad : descriptions) {
      // check if it's a URL to issuer's certificate
      if (ad.getAccessMethod().equals(X509ObjectIdentifiers.id_ad_caIssuers)) {
        GeneralName location = ad.getAccessLocation();
        if (location.getTagNo() == GeneralName.uniformResourceIdentifier) {
          String issuerUrl = location.getName().toString();
          // http URL to issuer (test in your browser to see if it's a valid certificate)
          // you can use java.net.URL.openStream() to create a InputStream and create
          // the certificate with your CertificateFactory
          try {
            return Optional.of(new URL(issuerUrl));
          } catch (MalformedURLException e) {
            throw new InternalException("Cannot parse issuer url", e);
          }
        }
      }
    }
    return Optional.empty();
  }

  private AuthorityInformationAccess extractAuthorityInformationAccess(final X509Certificate cert) {
    try {
      var extensionData = cert.getExtensionValue(Extension.authorityInfoAccess.getId());
      return AuthorityInformationAccess.getInstance(JcaX509ExtensionUtils.parseExtensionValue(extensionData));

    } catch (IOException e) {
      throw new InternalException("Failed to parse AuthorityInformationAccess", e);
    }
  }

  public Optional<URL> extractUrl(File inputFile) {
    try {
      var cert = readCertificate(inputFile);
      var maybeIssuerUrl = extractAuthorityInformationAccessIssuerUrl(cert);

      if (maybeIssuerUrl.isPresent()) {
        log.info("{}", maybeIssuerUrl.get());
      } else {
        log.error("Issuer not present");
      }

      return maybeIssuerUrl;
    } catch (InternalException e) {
      log.error("{}", e.getMessage());
      return Optional.empty();
    }
  }
}
