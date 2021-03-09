/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package keystoregen;

import com.google.common.flogger.FluentLogger;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Class;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

/**
 * Utility to generate a java keystore. The keytool utility distributed with the
 * JDK does not allow the creation of a keystore without a password. This does.
 */
public class KeystoreGenerator {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     * Add an array of cert files to the keystore
     */
    private static void ingest(KeyStore keyStore, File[] files) {
        for (File file : files) {
            ingest(keyStore, file);
        }
    }

    /**
     * Add a cert file to the keystore. If a directory, recursively add the files inside
     */
    private static void ingest(KeyStore keyStore, File file) {
        if (file.isDirectory()) {
            ingest(keyStore, file.listFiles());
            return;
        }

        try (InputStream stream = new FileInputStream(file)) {
            keyStore.setCertificateEntry(file.getName(), CertificateFactory.getInstance("X.509").generateCertificate(stream));
        } catch (KeyStoreException | CertificateException | IOException e) {
            logger.atInfo().log("unable to add cert %s", file.getName());
        }
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor(KeystoreGenerator.class.getSimpleName())
                .build()
                .defaultHelp(true)
                .description("Create a BouncyCastle keystore from a set of local certificates")
                .version("0.0.1");

        parser.addArgument("-o", "--output")
                .setDefault("TrustStore")
                .help("TrustStore file name");

        parser.addArgument("-p", "--password")
                .setDefault("")
                .help("Keystore password");

        parser.addArgument("-d", "--provider")
                .setDefault("org.bouncycastle.jce.provider.BouncyCastleProvider")
                .help("Fully qualified Security provider name");

        parser.addArgument("-f", "--format")
                .setDefault("BKS")
                .help("Keystore format");

        parser.addArgument("certs")
                .nargs("*")
                .required(true)
                .help("Certificates to add");

        Namespace namespace = null;
        try {
            namespace = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        String outputFile = namespace.getString("output");
        String password = namespace.getString("password");
        String provider = namespace.getString("provider");
        String format = namespace.getString("format");
        List<String> certificates = namespace.getList("certs");

        try {
            Provider instance = (Provider) Class.forName(provider).getConstructor().newInstance();
            Security.addProvider(instance);
        } catch (Exception e) {
            logger.atWarning().withCause(e).log("");
            System.exit(1);
        }

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(format);
            keyStore.load(null, null);
        } catch (Exception e) {
            logger.atWarning().withCause(e).log("");
            System.exit(1);
        }

        for (String cert : certificates) {
            ingest(keyStore, new File(cert));
        }

        try {
            keyStore.store(new FileOutputStream(outputFile), password.toCharArray());
        } catch (Exception e) {
            logger.atWarning().withCause(e).log("");
            System.exit(1);
        }
    }
}
