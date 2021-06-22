package pt.ulisboa.tecnico.sec.services.dto;


import java.util.Objects;

/**
 * A request made by the prover sent to witnesses, to ask for location acknowledgement
 */
public class RequestProofDTO {

    private int x;
    private int y;
    private int epoch;
    private String userID; // Prover
    private String digitalSignature;
    private String nonce;

    public RequestProofDTO() {}

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "Request of epoch " + epoch + " at location (" + x + "," + y + ") made by user " + userID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestProofDTO other = (RequestProofDTO) obj;
        if (digitalSignature == null) {
            if (other.digitalSignature != null)
                return false;
        } else if (!digitalSignature.equals(other.digitalSignature))
            return false;
        if (epoch != other.epoch)
            return false;
        if (nonce == null) {
            if (other.nonce != null)
                return false;
        } else if (!nonce.equals(other.nonce))
            return false;
        if (userID == null) {
            if (other.userID != null)
                return false;
        } else if (!userID.equals(other.userID))
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, epoch, userID, digitalSignature, nonce);
    }
}
