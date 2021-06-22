package pt.ulisboa.tecnico.sec.services.dto;

import java.util.List;

public class DTOFactory {
	
	private DTOFactory() {}
	
	public static RequestProofDTO makeRequestProofDTO(int x, int y, int epoch, String userID, String digitalSignature) {
		RequestProofDTO requestProofDTO = new RequestProofDTO();
		
		requestProofDTO.setX(x);
		requestProofDTO.setY(y);
		requestProofDTO.setEpoch(epoch);
		requestProofDTO.setUserID(userID);
		requestProofDTO.setDigitalSignature(digitalSignature);
		
		return requestProofDTO;
	}
	
	public static ProofDTO makeProofDTO(int epoch, String userID, RequestProofDTO requestProofDTO, String digitalSignature) {
		ProofDTO proofDTO = new ProofDTO();
		
		proofDTO.setEpoch(epoch);
		proofDTO.setUserID(userID);
		proofDTO.setRequestProofDTO(requestProofDTO);
		proofDTO.setDigitalSignature(digitalSignature);
		
		return proofDTO;
	}
	
	public static ReportDTO makeReportDTO(RequestProofDTO requestProofDTO, List<ProofDTO> proofsList) {
		ReportDTO reportDTO = new ReportDTO();
		
		reportDTO.setRequestProofDTO(requestProofDTO);
		reportDTO.setProofsList(proofsList);
		
		return reportDTO;
	}
	
	public static RequestUserProofsDTO makeRequestUserProofsDTO(String userIdSender, String userIdRequested, List<Integer> epochs) {
		RequestUserProofsDTO requestUserProofsDTO = new RequestUserProofsDTO();
		
		requestUserProofsDTO.setUserIdSender(userIdSender);
		requestUserProofsDTO.setUserIdRequested(userIdRequested);
		requestUserProofsDTO.setEpochs(epochs);
		
		return requestUserProofsDTO;
	}

}
