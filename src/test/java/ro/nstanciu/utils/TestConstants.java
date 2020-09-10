package ro.nstanciu.utils;

import java.io.File;

public class TestConstants {
  private TestConstants() {
  }

  public static final String CERT_PATH = "doc-rust-lang-org.pem";
  public static final File CERT_FILE = new File(CERT_PATH);
  public static final String CERT_ISSUER_URL = "http://crt.sca1b.amazontrust.com/sca1b.crt";
}
