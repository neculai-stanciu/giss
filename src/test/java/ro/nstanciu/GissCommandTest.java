package ro.nstanciu;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import ro.nstanciu.utils.TestConstants;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GissCommandTest {
    private static final Logger log = LoggerFactory.getLogger(GissCommandTest.class);

    @Test
    public void testWithCommandLineOption() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "-v", "doc-rust-lang-org.pem" };
            PicocliRunner.run(GissCommand.class, ctx, args);

            // giss
            assertTrue(baos.toString().contains("Hello! Debug mode activated!"));
        }
    }

    @Test
    @DisplayName("Should display correct url string when Issuer is present")
    public void testCommandLineWithFile() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "doc-rust-lang-org.pem" };
            PicocliRunner.run(GissCommand.class, ctx, args);

            // giss
            log.debug(baos.toString());
            assertTrue(baos.toString().contains(TestConstants.CERT_ISSUER_URL));
        }
    }

    @Test
    @DisplayName("Should fail with correct message when file not found")
    public void testCommandWithWrongFile() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "doc-lang-org.pem" };
            PicocliRunner.run(GissCommand.class, ctx, args);

            // giss
            log.error(baos.toString());
            assertTrue(baos.toString().contains("File provided cannot be read or not available."));
        }
    }
}
