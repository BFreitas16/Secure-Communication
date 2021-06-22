package pt.ulisboa.tecnico.sec.services.configs;

public class CryptoConfiguration {
	
	private CryptoConfiguration(){}
	
	public static final String KEYSTORE_PASSWORD = System.getenv("keystore_password");

    // Key sizes
	
    public static final int SYMMETRIC_KEY_SIZE = 256;
    public static final int ASYMMETRIC_KEY_SIZE = 2048;

    // Algorithms
    
    public static final String ASYMMETRIC_ENCRYPTION_ALGO = "RSA";
    public static final String SYMMETRIC_ENCRYPTION_ALGO = "AES";
    public static final int IV_SIZE_BYTES = 16;
    public static final String CIPHER_ALGO = SYMMETRIC_ENCRYPTION_ALGO + "/CBC/PKCS5Padding";
    public static final String SIGN_ALGO = "SHA256withRSA";

}
