package pt.ulisboa.tecnico.sec.secureserver.business.handlers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.Report;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.ReportCatalog;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.ReportProof;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.User;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.UserCatalog;
import pt.ulisboa.tecnico.sec.secureserver.utils.DTOConverter;
import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.ResponseUserProofsDTO;
import pt.ulisboa.tecnico.sec.services.dto.SpecialUserResponseDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidRequestException;
import pt.ulisboa.tecnico.sec.services.exceptions.NoRequiredPrivilegesException;

@Service
public class ViewReportHandler {
	
	private UserCatalog userCatalog;
	
	private ReportCatalog reportCatalog;
	
	@Autowired
	public ViewReportHandler(UserCatalog userCatalog, ReportCatalog reportCatalog) {
		this.userCatalog = userCatalog;
		this.reportCatalog = reportCatalog;
	}
	
	/**
	 *	Used by any user to request a report
	 */
	public ReportDTO obtainLocationReport(String userIdSender, String userIdRequested, int epoch) throws ApplicationException {
		User userSender = userCatalog.getUserById(userIdSender);
		User userRequest = userCatalog.getUserById(userIdRequested);

		validateUserSenderAndRequestedOfRequest(userSender, userRequest);

		Report report = reportCatalog.getReportOfUserIdAtEpoch(userRequest.getUserId(), epoch);
		if(report == null)
			throw new InvalidRequestException("No report found.");

		return DTOConverter.makeReportDTO(report);
	}

	/**
	 *	Used by the special user to request all users at a specific location at a given epoch
	 */
	public SpecialUserResponseDTO obtainUsersAtLocation(String userId, int x, int y, int epoch) throws ApplicationException {
		User user = userCatalog.getUserById(userId);
		if (!user.isSpecialUser())
			throw new NoRequiredPrivilegesException("The user cannot do this task because it is not a special user.");
		
		List<Report> reportsFound = reportCatalog.getReportsOfLocationAt(x, y, epoch);
		List<User> users = new ArrayList<>();
		
		for (Report report : reportsFound) {
			User userAtLocation = userCatalog.getUserById(report.getUser().getUserId());
			users.add(userAtLocation);
		}
		
		return DTOConverter.makeSpecialUserResponseDTO(users);
	}
	
	/**
	 *	Used by any user to request all his proofs
	 */
	public ResponseUserProofsDTO requestMyProofs(String userIdSender, String userIdRequested, List<Integer> epochs) throws ApplicationException {
		User userSender = userCatalog.getUserById(userIdSender);
		User userRequest = userCatalog.getUserById(userIdRequested);
		
		validateUserSenderAndRequestedOfRequest(userSender, userRequest);
		
		List<ReportProof> proofs = reportCatalog.getProofsWrittenByUserAtEpochs(userRequest, epochs);
		
		List<ProofDTO> proofsDTO = DTOConverter.makeListProofDTO(proofs);
		return DTOConverter.makeResponseUserProofsDTO(proofsDTO);
	}
	
	/***********************************************************************************************/
	/* 							Auxiliary Functions	- Double Echo Broadcast						   */
	/***********************************************************************************************/
	
	private void validateUserSenderAndRequestedOfRequest(User userSender, User userRequest) throws ApplicationException {
		if(userSender == null || userRequest == null)
			throw new InvalidRequestException("Request malformed");

		if(!userSender.isSpecialUser() && !userSender.getUserId().equals(userRequest.getUserId()))
			throw new NoRequiredPrivilegesException("The sender id can not request the information of the requested id.");
	}

}
