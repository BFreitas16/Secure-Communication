package pt.ulisboa.tecnico.sec.secureclient.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.interfaces.ISpecialUserService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SpecialUserServiceWithRegisters implements ISpecialUserService {
	@Override
	public ReportDTO obtainLocationReport(String userIdSender, String userIdRequested, int epoch) throws ApplicationException {
		System.out.println("[Special Client "+ SpecialClientApplication.userId+"] I am user '" + userIdSender + "', asking for a Report of user '" + userIdRequested + " at epoch: " + epoch);
		
		// Prepare the body of the HTTP request
        RequestLocationDTO req = new RequestLocationDTO();
        req.setUserIDSender(userIdSender);
        req.setUserIDRequested(userIdRequested);
        req.setEpoch(epoch);

        // Convert the above request body to a secure request object
        ReportDTO response = ByzantineAtomicRegisterService.readFromRegisters(req);
        return response;
	}

    /**
     *      Special user normal behavior doesn't submit reports in the same way as a normal
     *      client. This functionality allows to create tests of normal behavior and byzantine behavior.
     */
	@Override
	public void submitLocationReport(String userID, ReportDTO reportDTO) throws ApplicationException {
        System.out.println("\n[Special Client" + SpecialClientApplication.userId + "] Report being sent:\n" + reportDTO.toString());
        ByzantineAtomicRegisterService.writeToRegisters(reportDTO, userID);
	}

    /**
     *  Obtain the location of a certain user in a given epoch
     */
	@Override
	public SpecialUserResponseDTO obtainUsersAtLocation(String userId, int x, int y, int epoch) throws ApplicationException {
		RequestLocationDTO req = new RequestLocationDTO();
		req.setUserIDSender(userId);
		req.setEpoch(epoch);
		req.setX(x);
		req.setY(y);
		
		// Convert the above request body to a secure request object
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        SecureDTO secureDTO = CryptoService.generateNewSecureDTO(req, userId, randomBytes, "1");
        
        String urlAPI = PathConfiguration.getObtainUsersAtLocationEpochURL(1);

        SpecialUserResponseDTO response = ByzantineRegularRegisterService.readFromRegisters(secureDTO, SpecialUserResponseDTO.class, SpecialClientApplication.userId, urlAPI);
        return response;
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
     *  Used to realize tests with a pre-created secureDTO
     *  for example to test invalid signatures, nonces...
     */
    public ReportDTO obtainInfo(ArrayList<SecureDTO> secureDTOs, byte[] randomBytes, String endpoint) {
        SecureDTO sec = NetworkService.sendMessageToServers(secureDTOs, endpoint);

        // Check digital signature
        ReportDTO report = (ReportDTO) CryptoService.extractEncryptedData(sec, ReportDTO.class, CryptoUtils.createSharedKeyFromString(randomBytes));
        if(CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getServerPublicKey("1"))) {
            return report;
        } else
            return null;
    }

    /**
     *  Used to realize tests with a pre-created secureDTO
     *  for example to test invalid signatures, nonces...
     */
    public void sendInfo(ArrayList<SecureDTO> secureDTOs, byte[] randomBytes, String endpoint) {
        SecureDTO sec = NetworkService.sendMessageToServers(secureDTOs, endpoint);
        CryptoService.extractEncryptedData(sec, AcknowledgeDto.class, CryptoUtils.createSharedKeyFromString(randomBytes));
    }

    /**
     *  Asking another client for proofs
     */
    public ProofDTO requestLocationProof(String url, RequestProofDTO request) {
        ClientResponseDTO clientResponse = NetworkService.sendMessageToClient(request, url);

        if(clientResponse != null && clientResponse.getErr() != null) {
            System.out.println("[Special Client " + SpecialClientApplication.userId + "] Error occurred asking for proof: " + clientResponse.getErr().getDescription());
            return null;
        }

        return clientResponse.getProof();
    }

    public SecureDTO sendSecureDtoToServerOrClient(String url, SecureDTO sec) {
        RestTemplate restTemplate = new RestTemplate();
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        // Send request and return the SecureDTO with the ReportDTO encapsulated
        HttpEntity<SecureDTO> entity = new HttpEntity<>(sec, headers);
        ResponseEntity<SecureDTO> result = restTemplate.exchange(url, HttpMethod.POST, entity, SecureDTO.class);
        return result.getBody();
    }

}
