package pt.ulisboa.tecnico.sec.services.utils.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pt.ulisboa.tecnico.sec.services.configs.CryptoConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.SignatureCheckFailedException;

import javax.crypto.*;
import java.security.*;

public class CryptoService {
	
	private CryptoService() {}


    /**
     *  Extracts secret key from the SecureDTO object, only called by the server
     *  as the client already possesses the key.
     */
	// to be used by the server cuz it uses Server private key
	public static SecretKey getServerSecretKeyFromDTO(SecureDTO sec, String serverId) {
	    try {
            // Obtain encrypted secret key in base64
            String randomString = sec.getRandomString();

            // Getting the encrypted random string from Custom Protocol Response
            byte[] encryptedStringBytes = CryptoUtils.decodeBase64(randomString);
            PrivateKey kp = CryptoUtils.getServerPrivateKey(serverId);
            byte[] decryptedStringBytes = CryptoUtils.decrypt(encryptedStringBytes, kp);

            // Generate Secret Key
            return CryptoUtils.createSharedKeyFromString(decryptedStringBytes);
        } catch(Exception e) {
            System.out.println("Error extracting secret key");
        }

	    return null;
    }

    /**
     *  Extracts secret key from the SecureDTO object, only called by the server
     *  as the client already possesses the key.
     */
    // to be used by the server cuz it uses Server private key
    public static SecretKey getClientSecretKeyFromDTO(SecureDTO sec, String clientId) {
        try {
            // Obtain encrypted secret key in base64
            String randomString = sec.getRandomString();

            // Getting the encrypted random string from Custom Protocol Response
            byte[] encryptedStringBytes = CryptoUtils.decodeBase64(randomString);
            PrivateKey kp = CryptoUtils.getClientPrivateKey(clientId);
            byte[] decryptedStringBytes = CryptoUtils.decrypt(encryptedStringBytes, kp);

            // Generate Secret Key
            return CryptoUtils.createSharedKeyFromString(decryptedStringBytes);
        } catch(Exception e) {
            System.out.println("Error extracting secret key");
        }

        return null;
    }

    /**
     *  Function only called by the client to generate a secret key from random bytes
     */
    public static SecretKey generateSecretKey(byte[] randomBytes) {
	    try {
            // Use random bytes to generate a symmetric AES Key
            return CryptoUtils.createSharedKeyFromString(randomBytes);
        } catch(Exception e) {
            System.out.println("Error generating secret key");
        }

	    return null;
    }

    /**
     * USED BY CLIENTS
     *
     *  Returns the randomBytes (used to create the shared key) encrypted and encoded in base64,
     *  only used by the client.
     */
    public static String encryptRandomBytes(byte[] randomBytes, String serverId) {
        try {
            // Encrypt the random bytes with the server public key, so he can decrypt it later
            PublicKey pk = CryptoUtils.getServerPublicKey(serverId);
            byte[] encryptedRandomBytes = CryptoUtils.encrypt(randomBytes, pk);
            return CryptoUtils.encodeBase64(encryptedRandomBytes);
        } catch(Exception e) {
            System.out.println("Error encrypting random bytes with server public key");
        }
        return null;
    }

    /**
     * USED BY CLIENTS
     *
     *  Returns the randomBytes (used to create the shared key) encrypted and encoded in base64,
     *  only used by the client.
     */
    public static String serverEncryptRandomBytes(byte[] randomBytes, String userId) {
        try {
            // Encrypt the random bytes with the server public key, so he can decrypt it later
            PublicKey pk = CryptoUtils.getClientPublicKey(userId);
            byte[] encryptedRandomBytes = CryptoUtils.encrypt(randomBytes, pk);
            return CryptoUtils.encodeBase64(encryptedRandomBytes);
        } catch(Exception e) {
            System.out.println("Error encrypting random bytes with server public key");
        }
        return null;
    }

    /**
     *  Extracts the data field from a SecureDTO, decrypts it and converts it to a
     *  DTO Object.
     */
    public static Object extractEncryptedData(SecureDTO sec, Class<?> aClass, SecretKey originalKey) {
        String data = "";
        try {
            // Use the original key to decrypt the data field
            String dataEncrypted = sec.getData();

            // Get IV
            String IVString = sec.getIv();
            byte[] IV = CryptoUtils.decodeBase64(IVString);

            // Get the original JSON Object as String
            data = CryptoUtils.decrypt(originalKey, dataEncrypted, IV);

            // Convert string json to DTO
            return convertStringToJson(data, aClass);
        } catch(JsonProcessingException e) {
            // The object that we tried to convert to wasn't the class specified, therefore it can be a
            // error object.
            try {
                if (data.contains("errorName")) {
                    ErrorMessageResponse err = (ErrorMessageResponse) convertStringToJson(data, ErrorMessageResponse.class);
                    System.out.println("[Error - "+err.getErrorName()+"] "+err.getDescription());
                } else {
                    System.out.println("[CryptoService - extractEncryptedData] JsonProcessingException error, unencrypted data: " + data);
                    System.out.println(e.getMessage());
                }
            } catch(Exception egg) {
                System.out.println("Message wasn't a ErrorMessage either.");
            }
        } catch(Exception e) {
            System.out.println("Error caught in the extractEncryptedData function..." + e.getMessage());
        }

        return null;
    }
    
    // to be used by the server cuz it uses Server private key
    public static Object serverExtractEncryptedData(SecureDTO sec, Class<?> aClass, String serverId) {
    	SecretKey originalKey = getServerSecretKeyFromDTO(sec, serverId);
    	return extractEncryptedData(sec, aClass, originalKey);
    }

    // to be used by the client cuz it uses Server private key
    public static Object clientExtractEncryptedData(SecureDTO sec, Class<?> aClass, String clientId) {
        SecretKey originalKey = getClientSecretKeyFromDTO(sec, clientId);
        return extractEncryptedData(sec, aClass, originalKey);
    }


    /**
     *  Creates a SecureDTO
     */
    public static SecureDTO createSecureDTO(Object dataDTO, SecretKey key, String randomString, PrivateKey signKey) {
        try {
            // Convert dataDTO (e.g. ReportDTO) to a string
            ObjectMapper mapper = new ObjectMapper();
            String stringDTO = mapper.writeValueAsString(dataDTO);

            // Generate IV
            byte[] ivBytes = new byte[CryptoConfiguration.IV_SIZE_BYTES];
            new SecureRandom().nextBytes(ivBytes);
            String ivString = CryptoUtils.encodeBase64(ivBytes);

            // Encrypt DataDTO with AES with the previously generated key
            String data = CryptoUtils.encrypt(key, stringDTO, ivBytes);

            // Build the secureDTO
            SecureDTO sec = new SecureDTO(data, randomString, ivString, CryptoUtils.generateNonce());

            // Sign the secureDTO
            signSecureDTO(sec, signKey);

            return sec;
        } catch(Exception e) {
            System.out.println("Error creating secureDTO...");
        }

        return null;
    }

    /**
     *  Creates a Partial SecureDTO
     */
    public static SecureDTO createPartialSecureDTO(Object dataDTO, SecretKey key) {
        try {
            // Convert dataDTO (e.g. ReportDTO) to a string
            ObjectMapper mapper = new ObjectMapper();
            String stringDTO = mapper.writeValueAsString(dataDTO);

            // Generate IV
            byte[] ivBytes = new byte[CryptoConfiguration.IV_SIZE_BYTES];
            new SecureRandom().nextBytes(ivBytes);
            String ivString = CryptoUtils.encodeBase64(ivBytes);

            // Encrypt DataDTO with AES with the previously generated key
            String data = CryptoUtils.encrypt(key, stringDTO, ivBytes);

            // Build the secureDTO
            SecureDTO sec = new SecureDTO(data, ivString);

            return sec;
        } catch(Exception e) {
            System.out.println("Error creating Partial secureDTO...");
        }

        return null;
    }

    /**
     *  Generates a partial secureDTO that will still require a timestamp and the rid
     */
    public static SecureDTO creteCompleteSecureDTO(SecureDTO secureDTO, String randomString, PrivateKey signKey){
        try {
            SecureDTO sec = new SecureDTO(secureDTO.getData(), secureDTO.getIv());
            sec.setNonce(CryptoUtils.generateNonce());
            sec.setRandomString(randomString);
            sec.setProofOfWork(secureDTO.getProofOfWork());

            // Sign the secureDTO
            signSecureDTO(sec, signKey);

            return sec;
        } catch(Exception e) {
            System.out.println("Error creating secureDTO...");
        }

        return null;
    }

    /**
     *  Verifies the digital signature of request report and proofs
     */
    public static boolean checkDigitalSignature(String message, String digitalSignature, PublicKey pk) {
        try {
            return CryptoUtils.confirmSignature(
                    pk,
                    message,
                    digitalSignature
            );
        } catch(Exception e) {
            System.out.println("Digital signature of report or proof failed.");
            return false;
        }
    }
    
    /**
     * USED BY CLIENTS
     *
     * Generates a new SecureDTO from user with userId specified that encapsulates 
     * a LocationReportDTO or a ReportDTO
     */
    public static <T> SecureDTO generateNewSecureDTO(T unsecureDTO, String userId, byte[] randomBytes, String serverId) {
        SecretKey key = generateSecretKey(randomBytes);
        return createSecureDTO(unsecureDTO, key, encryptRandomBytes(randomBytes,serverId), CryptoUtils.getClientPrivateKey(userId));
    }

    /**
     * USED BY CLIENTS
     *
     * Generates a new Partial SecureDTO from user that encapsulates
     * a LocationReportDTO or a ReportDTO
     */
    public static <T> SecureDTO generatePartialSecureDTO(T unsecureDTO, byte[] randomBytes) {
        SecretKey key = generateSecretKey(randomBytes);
        return createPartialSecureDTO(unsecureDTO, key);
    }

    public static SecureDTO completeSecureDTO(SecureDTO partialSecureDTO, String userId, byte[] randomBytes, String serverId){
        return creteCompleteSecureDTO(partialSecureDTO, encryptRandomBytes(randomBytes, serverId), CryptoUtils.getClientPrivateKey(userId));
    }

    /**
     * USED BY SERVERS
     *
     * Generates a new SecureDTO from user with userId specified that encapsulates
     * a LocationReportDTO or a ReportDTO
     */
    public static <T> SecureDTO serverGenerateNewSecureDTO(T unsecureDTO, String userId, byte[] randomBytes, String serverId) {
        SecretKey key = generateSecretKey(randomBytes);
        return createSecureDTO(unsecureDTO, key, serverEncryptRandomBytes(randomBytes, userId), CryptoUtils.getServerPrivateKey(serverId));
    }
    
    /**
     * Generates a response SecureDTO of a client request that encapsulates a ReportDTO
     */
    public static <T> SecureDTO generateResponseSecureDTO(SecureDTO receivedSecureDTO, T unsecureResponseDTO, String serverId) {
    	SecretKey key = getServerSecretKeyFromDTO(receivedSecureDTO, serverId);
    	return createSecureDTO(unsecureResponseDTO, key, "", CryptoUtils.getServerPrivateKey(serverId));
    }

    /**
     * Builds the request proof message to be digitally signed.
     */
    public static String buildRequestProofMessage(RequestProofDTO reqProof) throws SignatureCheckFailedException {
        if(reqProof == null)
            throw new SignatureCheckFailedException("Can't validate proof signature, as the proof as no request associated with it.");
        String message = reqProof.getX() + reqProof.getY() + reqProof.getEpoch() + reqProof.getUserID() + (reqProof.getNonce() != null ? reqProof.getNonce() : "");
        return message;
    }

    /**
     * Builds the proof message to be digitally signed.
     */
    public static String buildProofMessage(ProofDTO proof) throws ApplicationException  {
        return proof.getEpoch() +
                proof.getUserID() +
                buildRequestProofMessage(proof.getRequestProofDTO()) +
                proof.getRequestProofDTO().getDigitalSignature();
    }

    /**
     * Sign Request DTO
     */
    public static void signRequestProofDTO(RequestProofDTO req) {
        try {
            req.setDigitalSignature(
                    CryptoUtils.sign(
                            CryptoUtils.getClientPrivateKey(req.getUserID()), buildRequestProofMessage(req)
                    )
            );
        } catch(Exception e) {
            System.out.println("Unable to sign the request proof, sending without signature even thought " +
                    "the failure is imminent and inevitable.");
        }
    }

    /**
     * Sign proof DTO
     */
    public static void signProofDTO(ProofDTO proof) {
        try {
            proof.setDigitalSignature(
                    CryptoUtils.sign(
                            CryptoUtils.getClientPrivateKey(proof.getUserID()), buildProofMessage(proof)
                    )
            );
        } catch(Exception e) {
            System.out.println("Unable to sign the proof, sending without signature even thought " +
                    "the failure is imminent and inevitable.");
        }
    }


    /************************************* Secure DTO Digital Signature Aux Funcs *************************************/

    /**
     * Build secureDTO message signature
     */
    public static String buildSecureDTOMessage(SecureDTO sec) {
        return sec.getData() + sec.getRandomString() + sec.getIv() + sec.getNonce() + sec.getTimestamp() + sec.getRid() + sec.getProofOfWork();
    }


    /**
     * Sign secure dto
     */
    public static void signSecureDTO(SecureDTO sec, PrivateKey signKey) {
        try {
            sec.setDigitalSignature(
                    CryptoUtils.sign(
                            signKey, buildSecureDTOMessage(sec)
                    )
            );
        } catch(Exception e) {
            System.out.println("Unable to sign the secure DTO, sending without signature even thought " +
                    "the failure is imminent and inevitable.");
        }
    }

    /**
     * Check the digital signature of the secureDTO received
     * from the server.
     *
     * TODO: Currently it assumes there is only one server, and retrieves
     * that key, in case there are multiple servers with different keys
     * then this has to be changed.
     */
    public static boolean checkSecureDTODigitalSignature(SecureDTO sec, PublicKey pk) {
        return checkDigitalSignature(
                buildSecureDTOMessage(sec),
                sec.getDigitalSignature(),
                pk
        );
    }

    public static Object convertStringToJson(String data, Class<?> aClass) throws JsonProcessingException {
        // Convert string json to DTO
        ObjectMapper mapper = new ObjectMapper();
        Object converted = mapper.readValue(data, aClass);
        return converted;
    }

    public static void signClientResponse(ClientResponseDTO clientResponse, PrivateKey signKey) {
        try {
            clientResponse.setDigitalSignature(
                    CryptoUtils.sign(
                            signKey, buildClientResponseMessage(clientResponse)
                    )
            );
        } catch(Exception e) {
            System.out.println("Unable to sign client response, sending without signature.");
        }
    }

    public static String buildClientResponseMessage(ClientResponseDTO clientResponse) {
        return clientResponse.getNonce() + (clientResponse.getErr() != null ? clientResponse.getErr().toString() : clientResponse.getProof().toString());
    }
}
