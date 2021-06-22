import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Enumeration;

public class keystoreCreator {
    static String rootPathToCertificates = "/home/allex/Secure-Communication/services/src/main/java/pt/ulisboa/tecnico/sec/services/keys/";
    static String keystorePath = "/home/allex/Secure-Communication/secKeystore.jks";

    public static void main(String[] args) {
        //createKeyStore();
        readKeyStore();
    }

    private static void readKeyStore() {
        try {
            char[] pwdArray = "sec".toCharArray();

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystorePath), pwdArray);

            PublicKey publicKey = ks.getCertificate("s2").getPublicKey();
            System.out.println(publicKey);
            if(publicKey != null)
                System.out.println("FOund s1 public key");

            /*
            Key s1 = ks.getKey("s1", pwdArray);
            if(s1 == null)
                System.out.println("Something is wrong");
            else
                System.out.println("s1 exists");
            */

            /*
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                System.out.println(alias);
            }
             */
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

            char[] pwdArray = "sec".toCharArray();
            ks.load(null, pwdArray);

            addKeysToStore(ks, pwdArray);

            try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
                ks.store(fos, pwdArray);
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void addKeysToStore(KeyStore ks, char[] pwdArray) {
        try {
            X509Certificate caCert = readCertificate(rootPathToCertificates+"rootCA.crt");

            addClientsToStore(ks, caCert, pwdArray);
            addServersToStore(ks, caCert, pwdArray);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void addServersToStore(KeyStore ks, X509Certificate caCert, char[] pwdArray) {
        try {
            for (int i = 1; i <= 4; i++) {
                System.out.println("Adding server key " + i);

                X509Certificate[] certificateChain = new X509Certificate[2];
                X509Certificate clientCert = readCertificate(rootPathToCertificates + "server/csrs/S" + i + ".crt");
                PrivateKey privateKey = readPrivateKey(rootPathToCertificates + "server/csrs/S" + i + "priv_pkcs8.key");

                certificateChain[0] = clientCert;
                certificateChain[1] = caCert;
                ks.setKeyEntry("S" + i, privateKey, pwdArray, certificateChain);
                System.out.println("Entry for server" + i + " added.");
            }
        } catch (Exception e) {
            System.out.println("Error adding server to store: " + e.getMessage());
        }
    }

    private static void addClientsToStore(KeyStore ks, X509Certificate caCert, char[] pwdArray) {
        try {
            for (int i = 1; i <= 4; i++) {
                System.out.println("Adding client key " + i);

                X509Certificate[] certificateChain = new X509Certificate[2];
                X509Certificate clientCert = readCertificate(rootPathToCertificates + "client/csrs/C" + i + ".crt");
                PrivateKey privateKey = readPrivateKey(rootPathToCertificates + "client/csrs/C" + i + "priv_pkcs8.key");

                certificateChain[0] = clientCert;
                certificateChain[1] = caCert;
                ks.setKeyEntry("C" + i, privateKey, pwdArray, certificateChain);
                System.out.println("Entry for client" + i + " added.");
            }

            System.out.println("Adding client key 100");

            X509Certificate[] certificateChain = new X509Certificate[2];
            X509Certificate clientCert = readCertificate(rootPathToCertificates + "client/csrs/C" + 100 + ".crt");
            PrivateKey privateKey = readPrivateKey(rootPathToCertificates + "client/csrs/C" + 100 + "priv_pkcs8.key");

            certificateChain[0] = clientCert;
            certificateChain[1] = caCert;
            ks.setKeyEntry("C" + 100, privateKey, pwdArray, certificateChain);
            System.out.println("Entry for client" + 100 + " added.");
        } catch(Exception e) {
            System.out.println("Error adding client to store: " + e.getMessage());
        }
    }

    private static PrivateKey readPrivateKey(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = new String(Files.readAllBytes(new File(path).toPath()), Charset.defaultCharset());

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }

    private static X509Certificate readCertificate(String path) throws CertificateException, FileNotFoundException {
        CertificateFactory fac = CertificateFactory.getInstance("X509");
        FileInputStream is = new FileInputStream(path);
        X509Certificate cert = (X509Certificate) fac.generateCertificate(is);
        return cert;
    }
}
