package pt.ulisboa.tecnico.sec.services.dto;

/**
 * Message used to create a "secure channel" between clients
 * and servers. All other DTO's swapped between server and client are encapsulated in the
 * data field of this object encrypted with a shared key generated from the unencrypted
 * randomString, using the IV.
 */
public class SecureDTO {
    private String data;
    private String randomString;
    private String digitalSignature;
    private String nonce;
    private String iv;

    // Registers
    private long timestamp;
    private int rid;

    // Proof of Work
    private String proofOfWork;

    //Don't delete constructor, necessary for Jackson to work
    public SecureDTO() {}

    public SecureDTO(String data, String randomString, String iv, String nonce) {
        this.data = data;
        this.randomString = randomString;
        this.nonce = nonce;
        this.iv = iv;
    }

    public SecureDTO(String data, String iv) {
        this.data = data;
        this.iv = iv;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRandomString() {
        return randomString;
    }

    public void setRandomString(String randomString) {
        this.randomString = randomString;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getProofOfWork() {
        return proofOfWork;
    }

    public void setProofOfWork(String proofOfWork) {
        this.proofOfWork = proofOfWork;
    }

    @Override
    public String toString() {
        return "SecureDTO{" +
                "data='" + data + '\'' +
                ", randomString='" + randomString + '\'' +
                ", digitalSignature='" + digitalSignature + '\'' +
                ", IV='" + iv + '\'' +
                ", Timestamp=" + timestamp +
                ", RID=" + rid +
                '}';
    }
}
