package com.malkeith.pgpUtils;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class PgpEncryptionTest {

    public static final TemporaryFolder tempFolder = new TemporaryFolder();

    private PgpEncryptionUtil pgpEncryptionUtil = null;
    private PgpDecryptionUtil pgpDecryptionUtil = null;

    private static URL loadResource(String resourcePath) {
        return Optional.ofNullable(PgpEncryptionTest.class.getResource(resourcePath))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    private static final String passkey = "dummy";
    private final URL privateKey = loadResource("/private.pgp");
    private final URL publicKey = loadResource("/public.pgp");

    private final URL testFile = loadResource("/Sample_CSV_5300kb.csv");

    private static final String testString = "This text needs to be PGP encrypted";

    @Before
    public void init() {
        pgpEncryptionUtil = PgpEncryptionUtil.builder()
                .armor(true)
                .compressionAlgorithm(CompressionAlgorithmTags.ZIP)
                .symmetricKeyAlgorithm(SymmetricKeyAlgorithmTags.AES_128)
                .withIntegrityCheck(true)
                .build();

        try {
            pgpDecryptionUtil = new PgpDecryptionUtil(privateKey.openStream(), passkey);
        } catch (IOException | PGPException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testByteEncryption() throws IOException, PGPException {
        // Encrypting the test bytes
        byte[] encryptedBytes = pgpEncryptionUtil.encrypt(testString.getBytes(Charset.defaultCharset()),
                publicKey.openStream());
        // Decrypting the generated encrypted bytes
        byte[] decryptedBytes = pgpDecryptionUtil.decrypt(encryptedBytes);
        // Comparing the original test string with string generated using the decrypted bytes
        assertEquals(testString, new String(decryptedBytes, Charset.defaultCharset()));
    }

}
