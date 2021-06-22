package pt.ulisboa.tecnico.sec.services.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Server answer to client that asked for a location report,
 * doesn't require digitalSignature as this message is encapsulated in a SecureDTO.
 */
public class ReportDTO {

    private RequestProofDTO requestProofDTO;
    private List<ProofDTO> proofsList;

    public ReportDTO() {}

    public ReportDTO(RequestProofDTO reqProof, List<ProofDTO> validProofs) {
        this.requestProofDTO = reqProof;
        this.proofsList = validProofs;
    }

    public RequestProofDTO getRequestProofDTO() {
        return requestProofDTO;
    }

    public void setRequestProofDTO(RequestProofDTO requestProofDTO) {
        this.requestProofDTO = requestProofDTO;
    }

    public List<ProofDTO> getProofsList() {
        return proofsList;
    }

    public void setProofsList(List<ProofDTO> proofsList) {
        this.proofsList = proofsList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (ProofDTO proofDTO : proofsList)
            sb.append(proofDTO.toString() + "\n");

        return  "Report:\n" + requestProofDTO.toString() + "\n" + sb.toString();
    }

    /**
     *      Used to build invalid reports, example when user requested an report that doesn't exist
     *      it would on client-side throw a NullPointerException.
     */
    public ReportDTO invalidReportBuilder() {
        System.out.println("I just built an invalid report DTO.");
        this.requestProofDTO = new RequestProofDTO();
        this.proofsList = new ArrayList<>();
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportDTO other = (ReportDTO) obj;
        if (proofsList == null) {
            if (other.proofsList != null)
                return false;
        } else if (!proofsList.equals(other.proofsList))
            return false;
        if (requestProofDTO == null) {
            if (other.requestProofDTO != null)
                return false;
        } else if (!requestProofDTO.equals(other.requestProofDTO))
            return false;
        return true;
    }


    @Override
    public int hashCode() {
        return Objects.hash(requestProofDTO, proofsList);
    }
}
