package pt.ulisboa.tecnico.sec.services.utils.crypto;

import pt.ulisboa.tecnico.sec.services.configs.CryptoConfiguration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyGenerator {

    public static void main(String[] args) throws Exception {

        // check args
        if (args.length != 2) {
            System.err.println("Usage: AESKeyGenerator [r|w] <key-file>");
            return;
        }

        final String mode = args[0];
        final String keyPath = args[1];

        if (mode.toLowerCase().startsWith("w")) {
            System.out.println("Generate and save keys");
            generateSecretKeyToFile(keyPath);
        } else {
            System.out.println("Load keys");
            readSecretKey(keyPath);
        }

        System.out.println("Done.");
    }

    public static Key generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(CryptoConfiguration.SYMMETRIC_ENCRYPTION_ALGO);
        keyGen.init(CryptoConfiguration.SYMMETRIC_KEY_SIZE);
        return keyGen.generateKey();
    }

    public static Key generateSecretKey(byte[] randomString) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(CryptoConfiguration.SYMMETRIC_ENCRYPTION_ALGO);
        keyGen.init(CryptoConfiguration.SYMMETRIC_KEY_SIZE, new SecureRandom(randomString));
        return keyGen.generateKey();
    }

    public static void generateSecretKeyToFile(String keyPath)
            throws NoSuchAlgorithmException, IOException {

        try (FileOutputStream fos = new FileOutputStream(keyPath)) {
            Key key = generateSecretKey();
            byte[] encoded = key.getEncoded();

            fos.write(encoded);
        }

    }

    public static Key readSecretKey(String keyPath) throws IOException {

        try (FileInputStream fis = new FileInputStream(keyPath)) {
            byte[] encoded = new byte[fis.available()];
            fis.read(encoded);
            return new SecretKeySpec(encoded, 0, 16, CryptoConfiguration.SYMMETRIC_ENCRYPTION_ALGO);
        }

    }

}
