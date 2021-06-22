package pt.ulisboa.tecnico.sec.secureserver.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pt.ulisboa.tecnico.sec.secureserver.business.handlers.CreateReportHandler;
import pt.ulisboa.tecnico.sec.secureserver.business.handlers.VerifyCryptoHandler;
import pt.ulisboa.tecnico.sec.secureserver.business.handlers.ViewReportHandler;
import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.ResponseUserProofsDTO;
import pt.ulisboa.tecnico.sec.services.dto.SpecialUserResponseDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.interfaces.ISpecialUserService;

@Service
public class UserService implements ISpecialUserService {
	
	private CreateReportHandler createReportHandler;
	
	private ViewReportHandler viewReportHandler;

	private VerifyCryptoHandler verifyCryptoHandler;
	
	@Autowired
	public UserService(CreateReportHandler createReportHandler, ViewReportHandler viewReportHandler, VerifyCryptoHandler verifyCryptoHandler) {
		this.createReportHandler = createReportHandler;
		this.viewReportHandler = viewReportHandler;
		this.verifyCryptoHandler = verifyCryptoHandler;
	}

	@Override
	public ReportDTO obtainLocationReport(String userIdSender, String userIdRequested, int epoch) throws ApplicationException {
		return this.viewReportHandler.obtainLocationReport(userIdSender, userIdRequested, epoch);
	}
	
	@Override
	public void submitLocationReport(String userID, ReportDTO reportDTO) throws ApplicationException {
		ReportDTO verifiedReport = this.verifyCryptoHandler.verifyAllCryptoConditions(reportDTO);
		this.createReportHandler.submitLocationReport(userID, verifiedReport);
	}

	@Override
	public SpecialUserResponseDTO obtainUsersAtLocation(String userId, int x, int y, int epoch) throws ApplicationException {
		return this.viewReportHandler.obtainUsersAtLocation(userId, x, y, epoch);
	}

	public void verifyNonce(String userId, String nonce) throws ApplicationException {
		this.verifyCryptoHandler.verifyNonce(userId, nonce);
	}

	@Override
	public ResponseUserProofsDTO requestMyProofs(String userIdSender, String userIdRequested, List<Integer> epochs) throws ApplicationException {
		return viewReportHandler.requestMyProofs(userIdSender, userIdRequested, epochs);
	}

}
