package net.thiagomarinho.jkstest.commands;

import com.beust.jcommander.Parameter;

import net.thiagomarinho.jkstest.Utils;

public class WriteSignedMessage extends DefaultCommandWithKeystore {

    @Parameter(names = {"--public-key-alias"})
    private String publicKeyAlias;

    @Parameter(names = {"--algorithm"})
    private String signatureAlgorithm = "SHA256withRSA";

    @Parameter(names = {"-c", "--cipher"})
    private String cipher = "RSA";

    @Parameter(names = {"-m", "--message"})
    private String message = Utils.DEFAULT_MESSAGE;

    @Parameter(names = {"--output-file"})
    private String outputFile = "message.txt";

    @Parameter(names = {"--signature-file"})
    private String signatureFile = "signature.txt";

    @Parameter(names = {"--certificate-file"})
    private String certificateFile = "certificate.txt";

    @Override
    public void runCommand() {
        String encryptedMessage = keyStoreHandler().encryptMessageWithCipher(message, cipher, publicKeyAlias);

    	String signatureForMessage = keyStoreHandler().signMessage(encryptedMessage, signatureAlgorithm);

    	writeFile(outputFile, encryptedMessage);
    	writeFile(signatureFile, signatureForMessage);
    	writeFile(certificateFile, keyStoreHandler().certificateContent());
    }
}
