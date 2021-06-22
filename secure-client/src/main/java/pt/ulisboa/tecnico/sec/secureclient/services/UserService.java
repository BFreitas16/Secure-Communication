package pt.ulisboa.tecnico.sec.secureclient.services;

import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestLocationDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestUserProofsDTO;
import pt.ulisboa.tecnico.sec.services.dto.ResponseUserProofsDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.interfaces.IUserService;

import java.util.List;

@Service
public class UserService implements IUserService {

    /**
     *  Requests a location report of a certain user at a certain epoch
     */
    @Override
    public ReportDTO obtainLocationReport(String userIdSender, String userIdRequested, int epoch) throws ApplicationException {
        // Prepare the body of the HTTP request
        RequestLocationDTO req = buildRequestLocation(userIdSender, userIdRequested, epoch);

        return ByzantineAtomicRegisterService.readFromRegisters(req);
    }

    /**
     *  Submits a location report to the server
     */
    @Override
    public void submitLocationReport(String userID, ReportDTO reportDTO) throws ApplicationException {
        ByzantineAtomicRegisterService.writeToRegisters(reportDTO, userID);
    }

    /**
     *  Requests user issued proofs
     */
	@Override
	public ResponseUserProofsDTO requestMyProofs(String userIdSender, String userIdRequested, List<Integer> epochs)
			throws ApplicationException {
		RequestUserProofsDTO requestUserProofsDTO = new RequestUserProofsDTO();
		requestUserProofsDTO.setUserIdSender(userIdSender);
		requestUserProofsDTO.setUserIdRequested(userIdRequested);
		requestUserProofsDTO.setEpochs(epochs);

	    return ByzantineRegularRegisterService.readFromRegisters(requestUserProofsDTO, ResponseUserProofsDTO.class, userIdSender, PathConfiguration.GET_PROOFS_AT_EPOCHS_ENDPOINT);
	}


    /**
     *  Auxiliary function to build a request location DTO
     */
    private RequestLocationDTO buildRequestLocation(String userIdSender, String userIdRequested, int epoch) {
        String reqId = userIdRequested;

        RequestLocationDTO req = new RequestLocationDTO();
        req.setUserIDSender(userIdSender);
        req.setUserIDRequested(reqId); // For the normal client this ID should be the same, it must be checked server-side
        req.setEpoch(epoch);

        return req;
    }
}
