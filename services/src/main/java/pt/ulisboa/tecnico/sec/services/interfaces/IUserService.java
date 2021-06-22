package pt.ulisboa.tecnico.sec.services.interfaces;

import java.util.List;

import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.ResponseUserProofsDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

public interface IUserService {
	
	public ReportDTO obtainLocationReport(String userIdSender, String userIdRequested, int epoch) throws ApplicationException;
	
	public void submitLocationReport(String userID, ReportDTO reportDTO) throws ApplicationException;
	
	public ResponseUserProofsDTO requestMyProofs(String userIdSender, String userIdRequested, List<Integer> epochs) throws ApplicationException;

}
