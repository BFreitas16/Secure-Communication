package pt.ulisboa.tecnico.sec.services.dto;

/**
 * Object used during the "Byzantine Quorum with Listeners write".
 * This object is sent by the server after a write and is used by the
 * client to determine if the write was successful.
 */
public class AcknowledgeDto {
    // The timestamp at which the server wrote the value
    long timestamp;

    // The id of the server acknowledging the write request
    String serverId;

    public AcknowledgeDto() {}

    public AcknowledgeDto(long timestamp, String serverId) {
        this.timestamp = timestamp;
        this.serverId = serverId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
