package net.thiagomarinho.jkstest.commands;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

import net.thiagomarinho.jkstest.ApplicationException;

public class ReadSignedMessage extends DefaultCommandWithKeystore {

	private final Logger logger = LoggerFactory.getLogger(ReadSignedMessage.class);

    @Parameter(names = {"--public-key-alias"})
    private String publicKeyAlias;

    @Parameter(names = {"-c", "--cipher"})
    private String cipher = "RSA";

    @Parameter(names = {"--input-file"})
    private String inputFile = "message.txt";

    @Parameter(names = {"--signature-file"})
    private String signatureFile = "signature.txt";

    @Parameter(names = {"--certificate-file"})
    private String certificateFile = "certificate.txt";

    @Parameter(names = {"--algorithm"})
    private String signatureAlgorithm = "SHA256withRSA";

    @Parameter(names = {"--use-ca"})
    private boolean shouldValidateWithCa = false;

    @Parameter(names = {"--ca-alias"})
    private String caAlias = "ca";

    @Override
    public void runCommand() {
        PublicKey publicKey;

        if (shouldValidateWithCa) {
            Certificate certificate = readCertificateFromFile();

            verifyCertificateWithCa(certificate);

        	publicKey = certificate.getPublicKey();
        } else {
            publicKey = keyStoreHandler().publicKey(publicKeyAlias);
        }

        String encryptedMessage = readFile(inputFile);
        verifySignatureWithPublicKey(encryptedMessage, publicKey);

        String decryptedMessage = keyStoreHandler().decryptMessageWithCipher(encryptedMessage, cipher);

        System.out.println(decryptedMessage);
    }

	private void verifyCertificateWithCa(Certificate certificate) {
		PublicKey caPublicKey = keyStoreHandler().publicKey(caAlias);

		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
            logger.error("Error while verifying certificate", e);
            throw new ApplicationException(e);
		}
	}

	private void verifySignatureWithPublicKey(String encryptedMessage, PublicKey publicKey) {
		Signature publicSignature = keyStoreHandler().signatureForVerification(signatureAlgorithm, publicKey);

		String signature = readFile(signatureFile);

		try {
		    publicSignature.update(encryptedMessage.getBytes(UTF_8));
		    byte[] signatureBytes = Base64.getDecoder().decode(signature);
		    System.out.println(publicSignature.verify(signatureBytes));
		} catch (SignatureException e) {
            logger.error("Error trying to verify signature with public key", e);
            throw new ApplicationException(e);
		}
	}

    public Certificate readCertificateFromFile() {
        try {
            String base64Certificate = readFile(certificateFile);
            byte[] byteKey = Base64.getDecoder().decode(base64Certificate);
            return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(byteKey));
        } catch (CertificateException e) {
            logger.error("Error trying to read certificate from file", e);
            throw new ApplicationException(e);
        }
    }
}
