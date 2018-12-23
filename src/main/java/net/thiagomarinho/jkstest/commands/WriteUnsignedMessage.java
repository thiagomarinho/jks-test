package net.thiagomarinho.jkstest.commands;

import com.beust.jcommander.Parameter;

import net.thiagomarinho.jkstest.Utils;

public class WriteUnsignedMessage extends DefaultCommandWithKeystore {

    @Parameter(names = {"--public-key-alias"})
    private String publicKeyAlias;

    @Parameter(names = {"-c", "--cipher"})
    private String cipher = "RSA";

    @Parameter(names = {"-m", "--message"})
    private String message = Utils.DEFAULT_MESSAGE;

    @Parameter(names = {"--output-file"})
    private String outputFile = "message.txt";

    @Override
    public void runCommand() {
    	String encryptedMessage = keyStoreHandler().encryptMessageWithCipher(message, cipher, publicKeyAlias);

    	writeFile(outputFile, encryptedMessage);
    }
}
