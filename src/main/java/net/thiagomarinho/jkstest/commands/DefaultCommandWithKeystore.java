package net.thiagomarinho.jkstest.commands;

import com.beust.jcommander.Parameter;

import net.thiagomarinho.jkstest.FileUtils;
import net.thiagomarinho.jkstest.KeyStoreHandler;
import net.thiagomarinho.jkstest.PasswordHandler;
import net.thiagomarinho.jkstest.Main.Command;

public abstract class DefaultCommandWithKeystore implements Command {

    @Parameter(names = {"-k", "--keystore-file"})
    private String keyStoreFile;

    @Parameter(names = {"--keystore-password"})
    private String keyStorePassword;

    @Parameter(names = {"-t", "--keystore-type"})
    private String keyStoreType = "PKCS12";

    @Parameter(names = {"-a", "--keypair-alias"})
    private String keyPairAlias;

    @Parameter(names = {"--keypair-password"})
    private String keyPairPassword;

    @Parameter(names = {"-p", "--password"})
    private String password;

    private PasswordHandler passwordHandler;

    private KeyStoreHandler keyStoreHandler;

    private FileUtils fileUtils;

    public DefaultCommandWithKeystore() {
        fileUtils = new FileUtils();
    }

    @Override
    public void run() {
        passwordHandler = new PasswordHandler(password, keyStorePassword, keyPairPassword);
        passwordHandler.askPasswords();

        keyStoreHandler = KeyStoreHandler.newKeyStoreHandler(keyStoreFile, keyPairAlias, keyStoreType, passwordHandler);

        runCommand();
    }

    public abstract void runCommand();

    public KeyStoreHandler keyStoreHandler() {
        return keyStoreHandler;
    }

    public void writeFile(String filename, String content) {
        fileUtils.writeFile(filename, content);
    }

    public String readFile(String filename) {
        return fileUtils.readFile(filename);
    }
}
