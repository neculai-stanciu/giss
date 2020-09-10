package ro.nstanciu.subcommands;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import ro.nstanciu.SharedOptions;
import ro.nstanciu.certs.CertificateExtractor;
import ro.nstanciu.certs.FileParameterValidator;

@Command(name = "download", description = "Download Issuer certificate interface if present", subcommands = {
    HelpCommand.class }, helpCommand = true, mixinStandardHelpOptions = true)
public class DownloadCertificateCmd implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(DownloadCertificateCmd.class);
  private CertificateExtractor certExtractor;
  private FileParameterValidator fileParameterValidator;

  @Mixin
  private SharedOptions sharedOptions = new SharedOptions();;

  @Option(names = { "-t",
      "--target" }, paramLabel = "TARGET_DIR", scope = ScopeType.INHERIT, description = "Directory where certificate will be stored. Default is current working directory.")
  private Path targetPath;

  @Override
  public void run() {
    if(targetPath == null) {
      targetPath = Paths.get(System.getProperty("user.dir"));
    }
    var inputFile = sharedOptions.getInputFile();
    if (fileParameterValidator.validateAcceptedCertType(inputFile)) {
      var maybeIssuerUrl = certExtractor.extractUrl(inputFile);
      if (maybeIssuerUrl.isPresent()) {
        var client = HttpClient.newBuilder().build();
        try {
          var issuerCertUri = maybeIssuerUrl.get().toURI();
          var request = HttpRequest.newBuilder().GET().uri(issuerCertUri).build();
          try {
          var certName = issuerCertUri.toString().substring(issuerCertUri.toString().lastIndexOf("/"));
          var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
          log.info("target path: {}", targetPath);
          var filePath = Paths.get(targetPath.toString(),certName);
          log.info("Try to write file: {}", filePath);
          Files.write(filePath, response.body());
          } catch (IOException | InterruptedException e) {
            log.error("Cannot get certificate from uri: {}", issuerCertUri);
            log.error(e.getMessage(), e);
          }
        } catch (URISyntaxException e) {
          log.error("Cannot parse retrieved uri");
        }
      }
    }
  }

  public CertificateExtractor getCertExtractor() {
    return certExtractor;
  }

  @Inject
  public void setCertExtractor(CertificateExtractor certExtractor) {
    this.certExtractor = certExtractor;
  }

  public FileParameterValidator getFileParameterValidator() {
    return fileParameterValidator;
  }

  @Inject
  public void setFileParameterValidator(FileParameterValidator fileParameterValidator) {
    this.fileParameterValidator = fileParameterValidator;
  }

}
