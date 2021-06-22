package pt.ulisboa.tecnico.sec.services.dto;

public class ClientResponseDTO {
    private ProofDTO proof;
    private ErrorMessageResponse err;
    private String nonce;
    private String digitalSignature;

    public ClientResponseDTO() {}

    public ErrorMessageResponse getErr() {
        return err;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getNonce() {
        return nonce;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public ProofDTO getProof() {
        return proof;
    }

    public void setErr(ErrorMessageResponse err) {
        this.err = err;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setProof(ProofDTO proof) {
        this.proof = proof;
    }

}
