package pt.ulisboa.tecnico.sec.services.dto;

/**
 * DTO used by the client to announce to the
 * servers that it finished reading and they should
 * unregister the client from the listening list.
 */
public class ReadCompleteDTO {
    // Identifier to remove from the list
    int rid;

    // The id of the client sending this message
    String clientId;

    public ReadCompleteDTO() {}

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
