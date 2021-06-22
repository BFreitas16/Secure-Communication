package pt.ulisboa.tecnico.sec.services.dto;

import java.util.Objects;

/**
 * Message sent by clients to the servers asking for a location report,
 * doesn't require digitalSignature as this message is encapsulated in a SecureDTO.
 */
public class RequestLocationDTO {
    private int x;  // Only important for special user endpoint
    private int y;  // Only important for special user endpoint
    private int epoch;
    private String userIDSender;    // The userId of the person sending
    private String userIDRequested; // The userId of the report requested

    public RequestLocationDTO() {}

    public int getEpoch() {
        return epoch;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getUserIDSender() {
        return userIDSender;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public void setUserIDSender(String userIDSender) {
        this.userIDSender = userIDSender;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getUserIDRequested() {
        return userIDRequested;
    }

    public void setUserIDRequested(String userIDRequested) {
        this.userIDRequested = userIDRequested;
    }

    @Override
    public String toString() {
        return "Request Location sent by " + userIDSender + ": (" + x + "," + y + ") at epoch " + epoch + " of user " + userIDRequested;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestLocationDTO other = (RequestLocationDTO) obj;
        if (epoch != other.epoch)
            return false;
        if (userIDRequested == null) {
            if (other.userIDRequested != null)
                return false;
        } else if (!userIDRequested.equals(other.userIDRequested))
            return false;
        if (userIDSender == null) {
            if (other.userIDSender != null)
                return false;
        } else if (!userIDSender.equals(other.userIDSender))
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, epoch, userIDSender, userIDRequested);
    }
}
