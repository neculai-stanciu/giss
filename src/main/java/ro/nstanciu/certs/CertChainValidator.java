package ro.nstanciu.certs;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CertChainValidator {

  public static boolean validateKeyChain(final X509Certificate client, final KeyStore keyStore)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException,
      NoSuchProviderException {
    final var certs = Collections.list(keyStore.aliases()).stream().map(a -> {
      try {
        return keyStore.getCertificate(a);
      } catch (KeyStoreException e) {
        e.printStackTrace();
        throw new ro.nstanciu.errors.InternalException(e.getMessage(), e);
      }
    })
        .toArray(X509Certificate[]::new);
    return validateKeyChain(client, certs);
  }

  /**
   * A simple method to validate a certificate chain
   *
   * @param client - {@literal X509Certificate}
   * @param certs  - is an Array containing all trusted X509Certificate
   * @return - true if validation until root certificate success, false otherwise
   * @throws NoSuchAlgorithmException
   * @throws CertificateException
   * @throws InvalidAlgorithmParameterException
   * @throws NoSuchProviderException
   */
  private static boolean validateKeyChain(final X509Certificate client, final X509Certificate... trustedCerts)
      throws NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException,
      NoSuchProviderException {
    var found = false;
    var certFactory = CertificateFactory.getInstance("X.509");
    var i = trustedCerts.length;
    TrustAnchor anchor;
    Set<TrustAnchor> anchors;
    CertPath path;
    List<Certificate> list;
    PKIXParameters params;
    var validator = CertPathValidator.getInstance("PKIX");

    while (!found && i > 0) {
      anchor = new TrustAnchor(trustedCerts[--i], null);
      anchors = Collections.singleton(anchor);

      list = List.of((Certificate) client);
      path = certFactory.generateCertPath(list);

      params = new PKIXParameters(anchors);
      params.setRevocationEnabled(false);

      if (client.getIssuerDN().equals(trustedCerts[i].getSubjectDN())) {
        try {
          validator.validate(path, params);
          if (isSelfSigned(trustedCerts[i])) {
            // found root CA
            found = true;
            System.out.println("validating root" + trustedCerts[i].getSubjectX500Principal().getName());
          } else if (!client.equals(trustedCerts[i])) {
            // find parent CA
            System.out.println("validating via: " + trustedCerts[i].getSubjectX500Principal().getName());
            found = validateKeyChain(trustedCerts[i], trustedCerts);
          }
        } catch (CertPathValidatorException e) {
          // validation fail, check next certificate in the trustedCerts array
        }
      }
    }
    return found;
  }

  private static boolean isSelfSigned(X509Certificate cert) throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
    try {
      var publicKey = cert.getPublicKey();
      cert.verify(publicKey);
      return true;
    } catch (SignatureException sigEx) {
      return false;
    } catch (InvalidKeyException keyEx) {
      return false;
    }
  }
}
