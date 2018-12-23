package net.thiagomarinho.jkstest.commands;

import com.beust.jcommander.Parameter;

public class ReadUnsignedMessage extends DefaultCommandWithKeystore {

    @Parameter(names = {"-c", "--cipher"})
    private String cipher = "RSA";

    @Parameter(names = {"--input-file"})
    private String inputFile = "message.txt";

    @Override
    public void runCommand() {
        String encryptedMessage = readFile(inputFile);

        String message = keyStoreHandler().decryptMessageWithCipher(encryptedMessage, cipher);

        System.out.println(message);        
    }
}
