package ro.nstanciu.certs;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.inject.Singleton;

@Singleton
public class FileParameterValidator {
  private static final List<String> ACCEPTED_EXT = List.of("pem");

  public boolean validateAcceptedCertType(File inputFile) {
    return containExt(inputFile.getName(), ACCEPTED_EXT);
  }

  private boolean containExt(final String filename, final List<String> extensions) {
    return Optional.ofNullable(filename).filter(f -> f.contains("."))
            .map(f -> f.substring(filename.lastIndexOf(".") + 1)).map(ext -> extensions.contains(ext))
            .orElse(false);
}
}
