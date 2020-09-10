package ro.nstanciu;

import java.io.File;
import java.security.Security;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.HelpCommand;
import ro.nstanciu.certs.CertificateExtractor;
import ro.nstanciu.certs.FileParameterValidator;
import ro.nstanciu.errors.InternalException;
import ro.nstanciu.subcommands.DownloadCertificateCmd;

@Command(name = "giss", version = "v0.0.1", mixinStandardHelpOptions = true,
        subcommands = {
        DownloadCertificateCmd.class,
        HelpCommand.class },
        description = "A simple interface that tries to obtain Authority Information Access from a certificate")
public class GissCommand implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GissCommand.class);

    private CertificateExtractor certExtractor;
    private FileParameterValidator fileParameterValidator;

    @Mixin
    private SharedOptions sharedOptions = new SharedOptions();

    public static void main(final String[] args) throws Exception {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
        PicocliRunner.run(GissCommand.class, args);
    }

    public FileParameterValidator getFileParameterValidator() {
        return fileParameterValidator;
    }

    @Inject
    public void setFileParameterValidator(FileParameterValidator fileParameterValidator) {
        this.fileParameterValidator = fileParameterValidator;
    }

    @Inject
    public void setCertExtractor(CertificateExtractor certExtractor) {
        this.certExtractor = certExtractor;
    }

    public void run() {
        // business logic here
        if (sharedOptions.isVerbose()) {
            setLoggerLevel(Level.DEBUG);
            log.debug("Hello! Debug mode activated!");
        }
        var inputFile = sharedOptions.getInputFile();
        if (fileParameterValidator.validateAcceptedCertType(inputFile)) {
            try {
                var cert = certExtractor.readCertificate(inputFile);
                var maybeIssuerUrl = certExtractor.extractAuthorityInformationAccessIssuerUrl(cert);
                if (maybeIssuerUrl.isPresent()) {
                    log.info("{}", maybeIssuerUrl.get());
                } else {
                    log.error("Issuer not present");
                }
            } catch (InternalException e) {
                log.error("{}", e.getMessage());
            }
        }
    }

    private void setLoggerLevel(final ch.qos.logback.classic.Level level) {
        final var rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(level);
    }
}
