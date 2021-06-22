package pt.ulisboa.tecnico.sec.secureserver.business.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.Report;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.ReportCatalog;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.ReportProof;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.User;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.UserCatalog;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidReportException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateReportHandler {
	
	private UserCatalog userCatalog;
	
	private ReportCatalog reportCatalog;
	
	@Autowired
	public CreateReportHandler(UserCatalog userCatalog, ReportCatalog reportCatalog) {
		this.userCatalog = userCatalog;
		this.reportCatalog = reportCatalog;
	}

	/**
	 *	Tries to submit a report to the server. The report must pass
	 *	all defined rules of what is "valid".
	 */
	public void submitLocationReport(String userID, ReportDTO reportDTO) throws ApplicationException {
		RequestProofDTO requestProofDTO = reportDTO.getRequestProofDTO();
		List<ProofDTO> proofDTOList = reportDTO.getProofsList();

		// Check report validity
		verifyReport(requestProofDTO, proofDTOList);

		User currentUser = userCatalog.getUserById(userID);

		Report newReport = currentUser.createAndSaveReport(userID, requestProofDTO.getEpoch(), requestProofDTO.getX(), requestProofDTO.getY(), requestProofDTO.getDigitalSignature());

		List<ReportProof> reportProofList = createReportProofs(newReport, proofDTOList);
		newReport.setReportProofList(reportProofList);

		reportCatalog.saveReport(newReport);
	}
	
	/***********************************************************************************************/
	/*                                     Auxiliary Functions                                     */
	/***********************************************************************************************/

	/**
	 * 	Create a proof list (creates on the db)
	 */
	private List<ReportProof> createReportProofs(Report report, List<ProofDTO> proofs) throws ApplicationException {
		List<ReportProof> reportProofList = new ArrayList<>();
		for (ProofDTO proof : proofs) {
			User user = userCatalog.getUserById(proof.getUserID());
			ReportProof reportProof = new ReportProof(user, proof.getEpoch(), report, proof.getDigitalSignature());
			reportProofList.add(reportProof);
		}
		return reportProofList;
	}

	/**
	 *	Verifies the report received from a user.
	 *	1º Verifies if the report epoch is correct (smaller) according to the server current epoch.
	 *	2º Verifies if the number of proofs associated to the report is larger than the number of
	 *		byzantine users assumed to exist near an individual. (Byzantine Rule)
	 *	3º Verify if the report already exists or not, duplicate reports or byzantine user changing position
	 *		during same epoch.
	 */
	private void verifyReport(RequestProofDTO requestProofDTO, List<ProofDTO> proofDTOList) throws ApplicationException {
		int epoch = requestProofDTO.getEpoch();
		String userId = requestProofDTO.getUserID();

		if (proofDTOList.size() < ByzantineConfigurations.MIN_CORRECT_USERS)
			throw new InvalidReportException("Not enough Proofs to approve the Report.");

		if (reportCatalog.getReportOfUserIdAtEpoch(userId, epoch) != null)
			throw new InvalidReportException("Duplicated report detected at epoch " + epoch + " for userId " + userId);
	}
}
