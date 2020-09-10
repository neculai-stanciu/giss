package ro.nstanciu;

import java.io.File;

import io.micronaut.context.BeanResolutionContext.Path;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class SharedOptions {

  @Option(names = { "-v", "--verbose" }, description = "Show more info")
  private boolean verbose;

  @Parameters(paramLabel = "FILE", description = "Certificates file to be processed. Accepted formats: pem")
  private File inputFile;

  public File getInputFile() {
    return inputFile;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setInputFile(File inputFile) {
    this.inputFile = inputFile;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }
}
