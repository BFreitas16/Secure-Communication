package pt.ulisboa.tecnico.sec.services.dto;

import java.util.Objects;

public class RequestDTO {

    private String clientId; // The sender
    private String serverId; // The server who sent

    private RequestLocationDTO requestLocationDTO;
    private ReportDTO reportDTO;
    private RequestUserProofsDTO requestUserProofsDTO;

    public RequestDTO() {}

    public RequestLocationDTO getRequestLocationDTO() {
        return requestLocationDTO;
    }

    public void setRequestLocationDTO(RequestLocationDTO requestLocationDTO) {
        this.requestLocationDTO = requestLocationDTO;
    }

    public ReportDTO getReportDTO() {
        return reportDTO;
    }

    public void setReportDTO(ReportDTO reportDTO) {
        this.reportDTO = reportDTO;
    }

    public RequestUserProofsDTO getRequestUserProofsDTO() {
        return requestUserProofsDTO;
    }

    public void setRequestUserProofsDTO(RequestUserProofsDTO requestUserProofsDTO) {
        this.requestUserProofsDTO = requestUserProofsDTO;
    }
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public boolean encapsulationOfRequestLocationIsEmpty() {
        return requestLocationDTO == null;
    }

    public boolean encapsulationOfReportIsEmpty() {
        return reportDTO == null;
    }

    public boolean encapsulationOfRequestUserProofsIsEmpty() {
        return requestUserProofsDTO == null;
    }

    @Override
    public String toString() {
        if (!encapsulationOfRequestLocationIsEmpty()) {
            return "Request: [" + requestLocationDTO.toString() + "]";
        }
        if (!encapsulationOfReportIsEmpty()) {
            return "Request: [" + reportDTO.toString() + "]";
        }
        return "Request: [" + requestUserProofsDTO.toString() + "]";
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        RequestDTO that = (RequestDTO) o;
//        if (!this.encapsulationOfRequestLocationIsEmpty() && !that.encapsulationOfRequestLocationIsEmpty()) return clientId.equals(that.clientId) && requestLocationDTO.equals(that.requestLocationDTO);
//        if (!this.encapsulationOfReportIsEmpty() && !that.encapsulationOfReportIsEmpty()) return clientId.equals(that.clientId) && reportDTO.equals(that.reportDTO);
//        if (!this.encapsulationOfRequestUserProofsIsEmpty() && !that.encapsulationOfRequestUserProofsIsEmpty()) return clientId.equals(that.clientId) && requestUserProofsDTO.equals(that.requestUserProofsDTO);
//        return false;
//    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestDTO other = (RequestDTO) obj;
        if (clientId == null) {
            if (other.clientId != null)
                return false;
        } else if (!clientId.equals(other.clientId))
            return false;
        if (reportDTO == null) {
            if (other.reportDTO != null)
                return false;
        } else if (!reportDTO.equals(other.reportDTO))
            return false;
        if (requestLocationDTO == null) {
            if (other.requestLocationDTO != null)
                return false;
        } else if (!requestLocationDTO.equals(other.requestLocationDTO))
            return false;
        if (requestUserProofsDTO == null) {
            if (other.requestUserProofsDTO != null)
                return false;
        } else if (!requestUserProofsDTO.equals(other.requestUserProofsDTO))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, requestLocationDTO, reportDTO, requestUserProofsDTO);
    }

}
