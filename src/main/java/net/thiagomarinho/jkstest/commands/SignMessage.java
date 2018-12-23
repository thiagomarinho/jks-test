package net.thiagomarinho.jkstest.commands;

import com.beust.jcommander.Parameter;

import net.thiagomarinho.jkstest.Utils;

public class SignMessage extends DefaultCommandWithKeystore {

    @Parameter(names = {"--algorithm"})
    private String signatureAlgorithm = "SHA256withRSA";

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
        String signatureForMessage = keyStoreHandler().signMessage(message, signatureAlgorithm);

        writeFile(outputFile, message);
        writeFile(signatureFile, signatureForMessage);
        writeFile(certificateFile, keyStoreHandler().certificateContent());
    }

}
