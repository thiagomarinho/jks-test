package net.thiagomarinho.jkstest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreHandler {

	private static final Logger logger = LoggerFactory.getLogger(KeyStoreHandler.class);

    private String keyPairAlias;
    private String keyPairPassword;

	private KeyStore keyStore;
	private KeyPair keyPair;

    public static  KeyStoreHandler newKeyStoreHandler(String keyStoreFile, String keyPairAlias, String keyStoreType, PasswordHandler passwordHandler) {
        if (passwordHandler.hasDifferentPasswords()) {
            return KeyStoreHandler.newInstance(keyStoreFile, passwordHandler.keyPairPassword(), passwordHandler.keyStorePassword(), keyPairAlias, keyStoreType);
        } else {
            return KeyStoreHandler.newInstance(keyStoreFile, passwordHandler.password(), keyPairAlias, keyStoreType);
        }
    }	

	public static KeyStoreHandler newInstance(String filename, String password, String keyPairAlias, String keyStoreType) {
		KeyStore keyStore = keyStoreInstance(keyStoreType, filename, password);

		return new KeyStoreHandler(keyStore, keyPairAlias, password);
	}

	public static KeyStoreHandler newInstance(String filename, String keyPairPassword, String keyStorePassword, String keyPairAlias, String keyStoreType) {
		KeyStore keyStore = keyStoreInstance(keyStoreType, filename, keyStorePassword);

		return new KeyStoreHandler(keyStore, keyPairAlias, keyPairPassword);
	}

    private static KeyStore keyStoreInstance(String keyStoreType, String filename, String keyStorePassoword) {
        try {
            KeyStore instance = KeyStore.getInstance(keyStoreType);
            instance.load(new FileInputStream(new File(filename)), keyStorePassoword.toCharArray());
            return instance;
        } catch (KeyStoreException e) {
            logger.error("Error creating KeyStore instance with type " + keyStoreType, e);
            throw new ApplicationException(e);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            logger.error("Error loading keystore", e);
            throw new ApplicationException(e);
        }
    }

    private KeyStoreHandler(KeyStore keyStore, String alias, String password) {
    	this.keyStore = keyStore;
    	this.keyPairAlias = alias;
    	this.keyPairPassword = password;
    }

    public String signMessage(String messageToBeSigned, String algorithm) {
        try {
            Signature privateSignature = signatureForSign(algorithm);
            privateSignature.update(messageToBeSigned.getBytes());
            byte[] signature = privateSignature.sign();
            return Base64.getEncoder().encodeToString(signature);
        } catch (SignatureException e) {
            logger.error("Error while trying to sign message", e);
            throw new ApplicationException(e);
        }
    }

    public KeyStore keyStore() {
    	return keyStore;
    }

    public KeyPair keyPair() {
    	if (keyPair == null) {
    		keyPair = new KeyPair(publicKey(), privateKey());
    	}

        return keyPair;
    }

    public PublicKey publicKey() {
        return publicKey(keyPairAlias);
    }

    public PublicKey publicKey(String alias) {
    	return certificate(alias).getPublicKey();
    }

    public String certificateContent() {
        try {
            return Base64.getEncoder().encodeToString(certificate().getEncoded());
        } catch (CertificateEncodingException e) {
            logger.error("Error trying to get certificate as byte array", e);
            throw new ApplicationException(e);
        }
    }

    public Certificate certificate() {
        return certificate(keyPairAlias);
    }

    public Certificate certificate(String alias) {
        try {
            return keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            logger.error("Error trying to get certificate for " + alias, e);
            throw new ApplicationException(e);
        }
    }

    public Signature signatureForSign(String algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey());
            return signature;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while trying to get Signature with algorithm " + algorithm, e);
            throw new ApplicationException(e);
        } catch (InvalidKeyException e) {
            logger.error("Error while trying to use key to init signature with algorithm" + algorithm, e);
            throw new ApplicationException(e);
        }
    }

    public Signature signatureForVerification(String algorithm, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            return signature;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while trying to get Signature with algorithm " + algorithm, e);
            throw new ApplicationException(e);
        } catch (InvalidKeyException e) {
            logger.error("Error while trying to use key to init signature with algorithm" + algorithm, e);
            throw new ApplicationException(e);
        }
    }

    public Cipher cipherForEncryption(String cipher, PublicKey publicKey) {
        try {
            Cipher encryptCipher = Cipher.getInstance(cipher);
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return encryptCipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            logger.error("Error while trying to get chiper to encrypt message", e);
            throw new ApplicationException(e);
        }
    }

    public Cipher cipherForDecryption(String cipher) {
        try {
            Cipher encryptCipher = Cipher.getInstance(cipher);
            encryptCipher.init(Cipher.DECRYPT_MODE, privateKey());
            return encryptCipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            logger.error("Error while trying to get chiper to encrypt message", e);
            throw new ApplicationException(e);
        }
    }

	public String encryptMessageWithCipher(String message, String cipherName, String publicKeyAlias) {
		Cipher cipher = cipherForEncryption(cipherName, publicKey(publicKeyAlias));

        try {
            byte[] cipherText = cipher.doFinal(message.getBytes(UTF_8));

            return Base64.getEncoder().encodeToString(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.error("Error trying to encrypt message", e);
            throw new ApplicationException(e);
        }
	}

    public String decryptMessageWithCipher(String encryptedMessage, String cipherName) {
        byte[] encryptedMessageAsArray = Base64.getDecoder().decode(encryptedMessage);
        Cipher decriptCipher = cipherForDecryption(cipherName);

        try {
            return new String(decriptCipher.doFinal(encryptedMessageAsArray), UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.error("Error while trying to decrypt message", e);
            throw new ApplicationException(e);
        }
    }

    private PrivateKey privateKey() {
    	return privateKeyEntry().getPrivateKey();
    }

    private KeyStore.PrivateKeyEntry privateKeyEntry() {
        try {
            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(keyPairPassword.toCharArray());

            return (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyPairAlias, keyPassword);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            logger.error("Error while trying to get private key " + keyPairAlias, e);
            throw new ApplicationException(e);
        }
    }
}
