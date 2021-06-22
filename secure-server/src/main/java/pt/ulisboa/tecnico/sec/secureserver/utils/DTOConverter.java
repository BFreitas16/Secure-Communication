package pt.ulisboa.tecnico.sec.secureserver.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.Report;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.ReportProof;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.User;
import pt.ulisboa.tecnico.sec.services.dto.DTOFactory;
import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.ResponseUserProofsDTO;
import pt.ulisboa.tecnico.sec.services.dto.SpecialUserResponseDTO;

/**
 * This class is used to convert objects from the Domain
 * to DTO objects to send to the client.
 * 
 * Note: there is a case that this class also converts from
 * DTO to DTO (when building ResponseUserProofsDTO)
 */
public class DTOConverter {
	
	private DTOConverter() {}
	
	// working with Domain -> DTO
	
	public static List<ProofDTO> makeListProofDTO(List<ReportProof> proofs) {
		List<ProofDTO> proofsDTO = new ArrayList<>();
		for (ReportProof proof : proofs) {
			proofsDTO.add(makeProofDTO(proof));
		}
		return proofsDTO;
	}
	
	public static ProofDTO makeProofDTO(ReportProof proof) {
		Report reportAux = proof.getReport();
		RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(reportAux.getX(), reportAux.getY(), reportAux.getEpoch(), reportAux.getUser().getUserId(), reportAux.getDigitalSignature());
		return DTOFactory.makeProofDTO(proof.getEpoch(), proof.getUser().getUserId(), requestProofDTO, proof.getDigitalSignature());
	}
	
	public static ReportDTO makeReportDTO(Report report) {
		// generate Request Proof DTO
		RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(report.getX(), report.getY(), report.getEpoch(), report.getUser().getUserId(), report.getDigitalSignature());
		
		// generate the Proof List DTO
		List<ProofDTO> proofs = new ArrayList<>();
		for (ReportProof proof : report.getReportProofList()) {
			ProofDTO proofDTO = DTOFactory.makeProofDTO(proof.getEpoch(), proof.getUser().getUserId(), requestProofDTO, proof.getDigitalSignature());
			proofs.add(proofDTO);
		}
		
		return DTOFactory.makeReportDTO(requestProofDTO, proofs);
	}
	
	public static SpecialUserResponseDTO makeSpecialUserResponseDTO(List<User> users) {
		SpecialUserResponseDTO responseDTO = new SpecialUserResponseDTO();

		// generate the users list
		List<String> usersList = users.stream().map(User::toString).collect(Collectors.toList());
		responseDTO.setUsers(usersList);
		
		return responseDTO;
	}

	// working with DTO -> DTO
	
	public static ResponseUserProofsDTO makeResponseUserProofsDTO(List<ProofDTO> proofsDTO) {
		return new ResponseUserProofsDTO(proofsDTO);
	}

}
