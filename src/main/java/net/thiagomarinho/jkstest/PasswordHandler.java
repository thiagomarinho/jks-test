package net.thiagomarinho.jkstest;

import org.apache.commons.lang3.StringUtils;

public class PasswordHandler {

    private String keyStorePassword;
    private String keyPairPassword;

    public PasswordHandler() {
        // empty constructor
    }

    public PasswordHandler(String password, String keyStorePassword, String keyPairPassword) {
        if (StringUtils.isEmpty(password)) {
            this.keyPairPassword = keyPairPassword;
            this.keyStorePassword = keyStorePassword;
        } else {
            this.keyStorePassword = password;
            this.keyPairPassword = password;
        }
    }

    public void askPasswords() {
        String additionalMessageForKeyPairPassword = "";

        if (StringUtils.isEmpty(keyStorePassword)) {
            keyStorePassword = new String(System.console().readPassword("Please provide password for KeyStore: "));
            additionalMessageForKeyPairPassword = " (empty if same as KeyStore password)";
        }

        if (StringUtils.isEmpty(keyPairPassword)) {
            keyPairPassword = new String(System.console().readPassword("Please provide password for key pair" + additionalMessageForKeyPairPassword + ": "));
        }

        if (StringUtils.isEmpty(keyPairPassword)) {
            keyPairPassword = keyStorePassword;
        }
    }

    public String keyStorePassword() {
        return keyStorePassword;
    }

    public String keyPairPassword() {
        return keyPairPassword;
    }

    // return any password (use it only if you know that both passwords are the same)
    public String password() {
        return keyPairPassword;
    }

    public boolean hasDifferentPasswords() {
        return !keyStorePassword.contentEquals(keyPairPassword);
    }
}
