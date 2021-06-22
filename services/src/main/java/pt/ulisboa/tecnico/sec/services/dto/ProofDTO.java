package pt.ulisboa.tecnico.sec.services.dto;


import java.util.Objects;

/**
 * Object issued by witnesses to the prover, to acknowledge the location of the prover
 */
public class ProofDTO {

    private int epoch;
    private String userID; // Witness
    private RequestProofDTO requestProofDTO;
    private String digitalSignature;

    public ProofDTO() {}

    public int getEpoch() { return epoch; }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public RequestProofDTO getRequestProofDTO() {
        return requestProofDTO;
    }

    public void setRequestProofDTO(RequestProofDTO requestProofDTO) {
        this.requestProofDTO = requestProofDTO;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    @Override
    public String toString() {
        return " * Proof of epoch " + epoch + " made by user " + userID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProofDTO other = (ProofDTO) obj;
        if (digitalSignature == null) {
            if (other.digitalSignature != null)
                return false;
        } else if (!digitalSignature.equals(other.digitalSignature))
            return false;
        if (epoch != other.epoch)
            return false;
        if (requestProofDTO == null) {
            if (other.requestProofDTO != null)
                return false;
        } else if (!requestProofDTO.equals(other.requestProofDTO))
            return false;
        if (userID == null) {
            if (other.userID != null)
                return false;
        } else if (!userID.equals(other.userID))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(epoch, userID, requestProofDTO, digitalSignature);
    }

}
