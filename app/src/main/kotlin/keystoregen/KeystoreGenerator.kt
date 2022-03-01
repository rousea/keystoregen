package keystoregen

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.io.InputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Class
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.Provider
import java.security.Security
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory

// Utility to generate a java keystore. The keytool utility distributed with the
// JDK does not allow the creation of a keystore without a password. This does.

fun main(args: Array<String>) {
    val parser = ArgumentParsers.newFor("KeystoreGenerator")
            .build()
            .defaultHelp(true)
            .description("Create a BouncyCastle keystore from a set of local certificates")
            .version("0.0.1")

    parser.addArgument("-o", "--output")
            .setDefault("TrustStore")
            .help("TrustStore file name")

    parser.addArgument("-p", "--password")
            .setDefault("")
            .help("Keystore password")

    parser.addArgument("-d", "--provider")
            .setDefault("org.bouncycastle.jce.provider.BouncyCastleProvider")
            .help("Fully qualified Security provider name")

    parser.addArgument("-f", "--format")
            .setDefault("BKS")
            .help("Keystore format")

    parser.addArgument("certs")
            .nargs("*")
            .required(true)
            .help("Certificates to add")

    val namespace = try {
        parser.parseArgs(args)
    } catch (e: HelpScreenException) {
        parser.handleError(e)
        System.exit(0)
        null
    }!!

    val outputFile = namespace.getString("output")
    val password = namespace.getString("password")
    val provider = namespace.getString("provider")
    val format = namespace.getString("format")
    val certificates: MutableList<String> = namespace.getList("certs")

    val instance = Class.forName(provider).getConstructor().newInstance() as Provider
    Security.addProvider(instance)

    val keystore = KeyStore.getInstance(format)
    keystore.load(null, null)

    for (cert in certificates) {
        File(cert).walk().forEach { file ->
            if (!file.isDirectory) {
                FileInputStream(file).use { stream ->
                    keystore.setCertificateEntry(file.getName(), CertificateFactory.getInstance("X.509").generateCertificate(stream))
                }
            }
        }
    }

    keystore.store(FileOutputStream(outputFile), password.toCharArray())
}
