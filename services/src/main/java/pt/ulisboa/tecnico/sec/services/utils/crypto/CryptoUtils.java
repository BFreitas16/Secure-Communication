package pt.ulisboa.tecnico.sec.services.utils.crypto;

import pt.ulisboa.tecnico.sec.services.configs.CryptoConfiguration;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

public class CryptoUtils {
	
	private CryptoUtils() {}

    // Nonce and Randoms

    public static String generateNonce() {
        return UUID.randomUUID().toString();
    }

    public static SecureRandom generateSecureRandom() {
        return new SecureRandom();
    }
    
    public static byte[] generateRandom32Bytes() {
        byte[] randomBytes = new byte[32];
        new Random().nextBytes(randomBytes);
        return randomBytes;
    }

    // Encryption - Symmetric

    public static String encrypt(SecretKey secretKey, String dataToEncrypt, byte[] ivBytes) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance(CryptoConfiguration.CIPHER_ALGO);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] cipherBytes = cipher.doFinal(dataToEncrypt.getBytes());
        return encodeBase64(cipherBytes);

    }

    public static String decrypt(SecretKey secretKey, String encryptedData, byte[] ivBytes) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance(CryptoConfiguration.CIPHER_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] cipherBytes = cipher.doFinal(decodeBase64(encryptedData));
        return new String(cipherBytes, StandardCharsets.UTF_8);

    }
    
    // Encryption - Asymmetric
    
    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws NoSuchPaddingException, 
    NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
    	
        Cipher cipher = Cipher.getInstance(CryptoConfiguration.ASYMMETRIC_ENCRYPTION_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
        
    }
    
    public static byte[] decrypt(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, 
    NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
    	
        Cipher cipher = Cipher.getInstance(CryptoConfiguration.ASYMMETRIC_ENCRYPTION_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
        
    }
    

    // Signatures

    public static String sign(PrivateKey privateKey, String message)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature signature = Signature.getInstance(CryptoConfiguration.SIGN_ALGO);
        signature.initSign(privateKey, generateSecureRandom());
        signature.update(message.getBytes());
        byte[] signatureBytes = signature.sign();
        return encodeBase64(signatureBytes);

    }

    public static boolean confirmSignature(PublicKey publicKey, String message, String signatureString)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature signature = Signature.getInstance(CryptoConfiguration.SIGN_ALGO);
        signature.initVerify(publicKey);
        signature.update(message.getBytes());
        return signature.verify(decodeBase64(signatureString));

    }

    // Base64

    public static String encodeBase64(byte[] dataArray) {
        return Base64.getEncoder().encodeToString(dataArray);
    }

    public static byte[] decodeBase64(String encodedData) {
        return Base64.getDecoder().decode(encodedData);
    }
    
    // Keys Management - Symmetric
    
    public static Key generateSecretKey() throws NoSuchAlgorithmException {
    	return AESKeyGenerator.generateSecretKey();
    }
    
    public static Key generateSecretKey(byte[] randomString) throws NoSuchAlgorithmException {
    	return AESKeyGenerator.generateSecretKey(randomString);
    }
    
    public static void generateSecretKeyToFile(String keyPath)
            throws NoSuchAlgorithmException, IOException {
    	AESKeyGenerator.generateSecretKeyToFile(keyPath);
    }
    
    public static Key readSecretKey(String keyPath) throws IOException {
    	return AESKeyGenerator.readSecretKey(keyPath);
    }
    
    /**
     *  Generate a shared key from random bytes
     */
    public static SecretKeySpec createSharedKeyFromString(byte[] randomBytes) {
        return new SecretKeySpec(randomBytes, 0, randomBytes.length, CryptoConfiguration.SYMMETRIC_ENCRYPTION_ALGO);
    }
    
    // Keys Management - Asymmetric
    
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    	return RSAKeyGenerator.generateKeyPair();
    }
    
    public static void generateKeyPairToFile(String publicKeyPath, String privateKeyPath)
            throws NoSuchAlgorithmException, IOException {
    	RSAKeyGenerator.generateKeyPairToFile(publicKeyPath, privateKeyPath);
    }
    
    public static KeyPair readKeyPairFromFile(String publicKeyPath, String privateKeyPath)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    	return RSAKeyGenerator.readKeyPairFromFile(publicKeyPath, privateKeyPath);
    }

    // Auxiliary functions to return cryptographic keys
    
    public static PrivateKey getClientPrivateKey(String userId) {
        try {
            //TODO: Replace by SYSTEM ENVIRONMENT VARIABLE "keystore_password"
            char[] pwdArray = "sec".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(PathConfiguration.KEYSTORE_LOCATION), pwdArray);

            return (PrivateKey) ks.getKey("c"+userId, pwdArray);
        } catch(Exception e) {
            System.out.println("Error reading client "+userId+ " private key: " + e.getMessage());
        }
        return null;
    }

    public static PublicKey getClientPublicKey(String userId) {
        try {
            //TODO: Replace by SYSTEM ENVIRONMENT VARIABLE "keystore_password"
            char[] pwdArray = "sec".toCharArray();

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(PathConfiguration.KEYSTORE_LOCATION), pwdArray);

            return ks.getCertificate("C"+userId).getPublicKey();
        } catch(Exception e) {
            System.out.println("Error reading client "+userId+ " private key: " + e.getMessage());
        }

        return null;
    }

    public static PrivateKey getServerPrivateKey(String serverId) {
        try {
            //TODO: Replace by SYSTEM ENVIRONMENT VARIABLE "keystore_password"
            char[] pwdArray = "sec".toCharArray();

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(PathConfiguration.KEYSTORE_LOCATION), pwdArray);

            return (PrivateKey) ks.getKey("s"+serverId, pwdArray);
        } catch(Exception e) {
            System.out.println("Error reading server private key: " + e.getMessage());
            System.out.println(e.getStackTrace());
        }

        return null;
    }

    public static PublicKey getServerPublicKey(String serverId) {
        try {
            //TODO: Replace by SYSTEM ENVIRONMENT VARIABLE "keystore_password"
            char[] pwdArray = "sec".toCharArray();

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(PathConfiguration.KEYSTORE_LOCATION), pwdArray);

            return ks.getCertificate("S"+serverId).getPublicKey();
        } catch(Exception e) {
            System.out.println("Error reading server public key: " + e.getMessage());
        }

        return null;
    }


    public static byte[] computeSHA256Hash(String... input) throws NoSuchAlgorithmException {
        String messageConcat = String.join("", input);
        return computeSHA256Hash(messageConcat.getBytes());
    }

    private static byte[] computeSHA256Hash(byte[] inputBytes) throws NoSuchAlgorithmException {
        byte[] digestBytes;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digestBytes = digest.digest(inputBytes);
        return digestBytes;
    }

}
