package pt.ulisboa.tecnico.sec.secureclient.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import pt.ulisboa.tecnico.sec.secureclient.ClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.services.ByzantineAtomicRegisterService;
import pt.ulisboa.tecnico.sec.secureclient.services.UserService;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.ProverOutOfRangeException;
import pt.ulisboa.tecnico.sec.services.exceptions.RepeatedNonceException;
import pt.ulisboa.tecnico.sec.services.exceptions.SignatureCheckFailedException;
import pt.ulisboa.tecnico.sec.services.utils.Grid;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class Controller {

	private List<String> nonces = new ArrayList<>();

	@Autowired
	private UserService userService;

	/**
	 *	Another client asked for a location proof
	 */
	@PostMapping("/proof")
	public ClientResponseDTO requestLocationProof(@RequestBody RequestProofDTO request) throws ApplicationException {
		System.out.println("\n[Client"+ClientApplication.userId+"] Received proof request");

		// Verify Nonce & Add it
		if(nonces.contains(request.getNonce()))
			throw new RepeatedNonceException("Request Proof Nonce repeated. (Replay Attack)");
		nonces.add(request.getNonce());

		// Verify check digital signature
		if (!CryptoService.checkDigitalSignature(CryptoService.buildRequestProofMessage(request), request.getDigitalSignature(), CryptoUtils.getClientPublicKey(request.getUserID()))) {
			throw new SignatureCheckFailedException("Can't validate request proof signature.");
		}

		// Check if the prover is in my range
		int proverId = Integer.parseInt(request.getUserID());
		List<Integer> usersNearby = Grid.getUsersInRangeAtEpoch(Integer.parseInt(ClientApplication.userId), ClientApplication.epoch, ByzantineConfigurations.RANGE);

		if(usersNearby.contains(proverId)) {
			ProofDTO proof = DTOFactory.makeProofDTO(ClientApplication.epoch, ClientApplication.userId, request, "");
			CryptoService.signProofDTO(proof);

			ClientResponseDTO clientResponse = new ClientResponseDTO();
			clientResponse.setProof(proof); // sends a Proof to a Witness
			clientResponse.setNonce(CryptoUtils.generateNonce());
			CryptoService.signClientResponse(clientResponse, CryptoUtils.getClientPrivateKey(ClientApplication.userId));

			return clientResponse;
		} else
			throw new ProverOutOfRangeException("[Client"+ClientApplication.userId+"] Prover is not in range, can't generate proof...");
	}

	/**
	 *	Client asks for its location report at a certain epoch
	 */
	@GetMapping("/locations/{epoch}")
	public ReportDTO requestLocationInformation(@PathVariable int epoch) throws ApplicationException {
		System.out.println("\n[Client"+ClientApplication.userId+"] Sending report request for user "+ ClientApplication.userId + " at epoch" + epoch);
		return userService.obtainLocationReport(ClientApplication.userId, ClientApplication.userId, epoch);
	}

	/**
	 *	Client submits report, only used for DEBUG as usually does this is EpochTriggerMonitor
	 */
	@GetMapping(PathConfiguration.SUBMIT_REPORT_ENDPOINT)
	public void submitReport() throws ApplicationException {
		System.out.println("Submitting report at epoch: " + ClientApplication.epoch);
		RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(10, 2, ClientApplication.epoch, ClientApplication.userId, "");
		CryptoService.signRequestProofDTO(requestProofDTO);

		ProofDTO proofDTO1 = DTOFactory.makeProofDTO(ClientApplication.epoch, "1", requestProofDTO, "");
		CryptoService.signProofDTO(proofDTO1);

		ProofDTO proofDTO2 = DTOFactory.makeProofDTO(ClientApplication.epoch, "2", requestProofDTO, "");
		CryptoService.signProofDTO(proofDTO2);

		ProofDTO proofDTO3 = DTOFactory.makeProofDTO(ClientApplication.epoch, "4", requestProofDTO, "");
		CryptoService.signProofDTO(proofDTO3);

		ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

		ClientApplication.epoch--;
		userService.submitLocationReport(ClientApplication.userId, report);
	}

	/**
	 *	Client asks for the proofs it has issued for other clients, used for DEBUG
	 */
	@GetMapping(PathConfiguration.GET_PROOFS_AT_EPOCHS_ENDPOINT)
	public ResponseUserProofsDTO requestIssuedProofs() throws ApplicationException {
		System.out.println("\n[Client"+ClientApplication.userId+"] Sending request to obtain issued proofs at epoch " + ClientApplication.epoch);
		ArrayList<Integer> epochs = new ArrayList<>();
		epochs.add(-1);
		ResponseUserProofsDTO response = userService.requestMyProofs(ClientApplication.userId, ClientApplication.userId, epochs);

		System.out.println("User issued proofs:");
		for (ProofDTO proof : response.getProofs()) {
			System.out.println(proof);
		}

		return response;
	}


	/***********************************************************************************************/
	/* 						Auxiliary Functions	- Byzantine Atomic Register						   */
	/***********************************************************************************************/


	@PostMapping(PathConfiguration.SPONTANEOUS_READ_ATOMIC_REGISTER)
	public void receiveSpontaneousRead(@RequestBody SecureDTO sec, @PathVariable String serverId) throws ApplicationException {
		ReportDTO report = (ReportDTO) CryptoService.clientExtractEncryptedData(sec, ReportDTO.class, ClientApplication.userId);
		if (report == null)
			throw new ApplicationException("[CLIENT " + ClientApplication.userId + "] SecureDTO object was corrupt or malformed, was not possible to extract the information at /spontaneousRead.");
		System.out.println("\n[SERVER" + ClientApplication.userId + "] Received an echo from server Id: " + serverId);

		// Check if the "spontaneous read" really comes from a server or some random malicious person
		if (!CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getServerPublicKey(serverId))) {
			throw new SignatureCheckFailedException("Digital signature check of client at /spontaneousRead failed.");
		}

		// Submit it to the answers received data structure
		ByzantineAtomicRegisterService.receiveSpontaneousRead(report, sec.getTimestamp(), serverId);
	}
}
