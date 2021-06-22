package pt.ulisboa.tecnico.sec.services.utils.crypto;

import pt.ulisboa.tecnico.sec.services.configs.CryptoConfiguration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAKeyGenerator {
    public static void main(String[] args) throws Exception {

        // check args
        if (args.length != 3) {
            System.err.println("Usage: RSAKeyGenerator [r|w] <priv-key-file> <pub-key-file>");
            return;
        }

        final String mode = args[0];
        final String privkeyPath = args[1];
        final String pubkeyPath = args[2];

        if (mode.toLowerCase().startsWith("w")) {
            System.out.println("Generate and save keys");
            generateKeyPairToFile(pubkeyPath, privkeyPath);
        } else {
            System.out.println("Load keys");
            readKeyPairFromFile(pubkeyPath, privkeyPath);
        }

        System.out.println("Done.");
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(CryptoConfiguration.ASYMMETRIC_ENCRYPTION_ALGO);
        keyGen.initialize(CryptoConfiguration.ASYMMETRIC_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    public static void generateKeyPairToFile(String publicKeyPath, String privateKeyPath)
            throws NoSuchAlgorithmException, IOException {

        try (FileOutputStream privFos = new FileOutputStream(privateKeyPath);
             FileOutputStream pubFos = new FileOutputStream(publicKeyPath)) {

            KeyPair keys = generateKeyPair();

            PrivateKey privKey = keys.getPrivate();
            byte[] privKeyEncoded = privKey.getEncoded();
            PublicKey pubKey = keys.getPublic();
            byte[] pubKeyEncoded = pubKey.getEncoded();


            privFos.write(privKeyEncoded);
            pubFos.write(pubKeyEncoded);

        }

    }

    public static KeyPair readKeyPairFromFile(String publicKeyPath, String privateKeyPath)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        try (FileInputStream pubFis = new FileInputStream(publicKeyPath);
             FileInputStream privFis = new FileInputStream(privateKeyPath)) {

            byte[] pubEncoded = new byte[pubFis.available()];
            pubFis.read(pubEncoded);

            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
            KeyFactory keyFacPub = KeyFactory.getInstance(CryptoConfiguration.ASYMMETRIC_ENCRYPTION_ALGO);
            PublicKey pub = keyFacPub.generatePublic(pubSpec);  // Error

            byte[] privEncoded = new byte[privFis.available()];
            privFis.read(privEncoded);

            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
            KeyFactory keyFacPriv = KeyFactory.getInstance(CryptoConfiguration.ASYMMETRIC_ENCRYPTION_ALGO);
            PrivateKey priv = keyFacPriv.generatePrivate(privSpec);

            return new KeyPair(pub, priv);

        }

    }

}
