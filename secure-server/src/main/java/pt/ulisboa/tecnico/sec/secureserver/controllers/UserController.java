package pt.ulisboa.tecnico.sec.secureserver.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.sec.secureserver.ServerApplication;
import pt.ulisboa.tecnico.sec.secureserver.services.*;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.SignatureCheckFailedException;
import pt.ulisboa.tecnico.sec.services.interfaces.ISpecialUserService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import javax.crypto.SecretKey;

@RestController
public class UserController {
	
	private ISpecialUserService userService;
	private NetworkService networkService;

	@Autowired
	public UserController(ISpecialUserService userService, NetworkService networkService) {
		this.userService = userService;
		this.networkService = networkService;
	}

	/**
	 *	User queries server for a location report
	 */
	@PostMapping(PathConfiguration.GET_REPORT_ENDPOINT)
	public SecureDTO obtainLocationReport(@RequestBody SecureDTO sec) throws ApplicationException {
		try {
			System.out.println("\n[SERVER " + ServerApplication.serverId + "] Received obtain report request.");

			// Validations
			RequestLocationDTO req = validateRequest(sec, RequestLocationDTO.class);
			if (req == null)
				throw new ApplicationException("[SERVER " + ServerApplication.serverId + "] SecureDTO object was corrupt or malformed, was not possible to extract the information.");
			verifyRequestSignatureAndNonce(sec, req.getUserIDSender(), PathConfiguration.GET_REPORT_ENDPOINT);

			// Executes the "Double Echo Broadcast" protocol to guarantee that all machines achieve the same status
			startDoubleEcho(req);

			// Read report
			ReportDTO report = ByzantineAtomicRegisterService.receiveReadRequest(req, sec.getRid(), (UserService) userService);

			// Report read, return to client
			System.out.println("[SERVER " + ServerApplication.serverId + "] Requested report was:"+report.toString());
			return CryptoService.generateResponseSecureDTO(sec, report, ServerApplication.serverId);
		} catch(ApplicationException e) {
			return handleException(sec, e);
		}
	}

	/**
	 *	User submits location report to server
	 */
	@PostMapping(PathConfiguration.SUBMIT_REPORT_ENDPOINT)
	public SecureDTO submitLocationReport(@RequestBody SecureDTO sec) throws ApplicationException {
		try {
			System.out.println("\n[SERVER" + ServerApplication.serverId + "] Received submit report request.");
			
			// Validations
			ReportDTO report = validateRequest(sec, ReportDTO.class);
			if (report == null)
				throw new ApplicationException("[SERVER " + ServerApplication.serverId + "] SecureDTO object was corrupt or malformed, was not possible to extract the information.");

			String clientId = report.getRequestProofDTO().getUserID();
			verifyRequestSignatureAndNonce(sec, clientId, PathConfiguration.SUBMIT_REPORT_ENDPOINT);

			// Executes the "Double Echo Broadcast" protocol to guarantee that all machines achieve the same status
			startDoubleEcho(report);

			// Submit report
			AcknowledgeDto ack = ByzantineAtomicRegisterService.receiveWriteRequest(clientId, report, sec.getTimestamp(), (UserService) userService);

			// Report submitted, return to client
			System.out.println("[SERVER " + ServerApplication.serverId + "] Report submitted successfully for client " + clientId);
			return CryptoService.generateResponseSecureDTO(sec, ack, ServerApplication.serverId);
		} catch(ApplicationException e) {
			return handleException(sec, e);
		}
	}


	/**
	 *	HA user asks for classified info for all users
	 */
	@PostMapping(PathConfiguration.OBTAIN_USERS_AT_LOCATION_EPOCH_ENDPOINT)
	public SecureDTO obtainUsersAtLocation(@RequestBody SecureDTO sec) throws ApplicationException {
		try {
			System.out.println("\n[SERVER" + ServerApplication.serverId + "] Received obtain users at location request.");

			// Validations
			RequestLocationDTO req = validateRequest(sec, RequestLocationDTO.class);
			if (req == null)
				throw new ApplicationException("[SERVER " + ServerApplication.serverId + "] SecureDTO object was corrupt or malformed, was not possible to extract the information.");
			verifyRequestSignatureAndNonce(sec, req.getUserIDSender(), PathConfiguration.OBTAIN_USERS_AT_LOCATION_EPOCH_ENDPOINT);

			// Executes the "Double Echo Broadcast" protocol to guarantee that all machines achieve the same status
			startDoubleEcho(req);

			// Reads from the register, return to client
			return ByzantineRegularRegisterService.receiveReadRequest(req, sec, userService);
		} catch(ApplicationException e) {
			return handleException(sec, e);
		}
	}


	/**
	 *	User requests all the proofs he previously issued
	 */
	@PostMapping(PathConfiguration.GET_PROOFS_AT_EPOCHS_ENDPOINT)
	public SecureDTO requestMyProofs(@RequestBody SecureDTO sec) throws ApplicationException {
		try {
			System.out.println("\n[SERVER" + ServerApplication.serverId + "] Received get client issued proofs request.");

			// Validations
			RequestUserProofsDTO requestUserProofs = validateRequest(sec, RequestUserProofsDTO.class);
			if (requestUserProofs == null)
				throw new ApplicationException("[SERVER " + ServerApplication.serverId + "] SecureDTO object was corrupt or malformed, was not possible to extract the information.");
	
			String clientIdSender = requestUserProofs.getUserIdSender();
			verifyRequestSignatureAndNonce(sec, clientIdSender, PathConfiguration.GET_PROOFS_AT_EPOCHS_ENDPOINT);

			// Executes the "Double Echo Broadcast" protocol to guarantee that all machines achieve the same status
			startDoubleEcho(requestUserProofs);

			// Reads from the register, return to client
			return ByzantineRegularRegisterService.receiveReadRequest(requestUserProofs, sec, userService);
		} catch(ApplicationException e) {
			return handleException(sec, e);
		}
	}


	/***********************************************************************************************/
	/* 							Auxiliary Functions	- Double Echo Broadcast						   */
	/***********************************************************************************************/

	/**
	 * Start "Double Echo Broadcast" protocol for the request user proofs
	 */
	private void startDoubleEcho(RequestUserProofsDTO requestUserProofs) throws ApplicationException {
		// Double-Echo Broadcast
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setRequestUserProofsDTO(requestUserProofs);
		requestDTO.setClientId(requestUserProofs.getUserIdSender());
		requestDTO.setServerId(ServerApplication.serverId);
		NetworkService.sendBroadcast(requestDTO);
	}


	/**
	 * Start "Double Echo Broadcast" protocol for the request report DTO
	 */
	private void startDoubleEcho(ReportDTO report) throws ApplicationException {
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setReportDTO(report);
		requestDTO.setClientId(report.getRequestProofDTO().getUserID());
		requestDTO.setServerId(ServerApplication.serverId);
		NetworkService.sendBroadcast(requestDTO);
	}

	/**
	 * Start "Double Echo Broadcast" protocol for the request location DTO
	 */
	private void startDoubleEcho(RequestLocationDTO req) throws ApplicationException {
		// Double-Echo Broadcast
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setRequestLocationDTO(req);
		requestDTO.setClientId(req.getUserIDSender());
		requestDTO.setServerId(ServerApplication.serverId);
		NetworkService.sendBroadcast(requestDTO);
	}

	/**
	 * Received ECHO message during the "Double Echo Broadcast" protocol, calls
	 * NetworkService to add it to the data structures.
	 */
	@PostMapping(PathConfiguration.SERVER_ECHO_ENDPOINT)
	public void echo(@RequestBody SecureDTO secureDTO, @PathVariable String sendingServerId) throws ApplicationException {
		RequestDTO requestDTO = (RequestDTO) CryptoService.serverExtractEncryptedData(secureDTO, RequestDTO.class, ServerApplication.serverId);
		if (requestDTO == null)
			throw new ApplicationException("[SERVER " + ServerApplication.serverId + "] SecureDTO object was corrupt or malformed, was not possible to extract the information.");
		System.out.println("\n[SERVER" + ServerApplication.serverId + "] Received an echo from " + requestDTO.getServerId());

		// Verify if the "echo" comes really from a server
		verifyServerSignatureAndNonce(secureDTO, sendingServerId, PathConfiguration.SERVER_ECHO);

		networkService.echo(requestDTO);
	}

	/**
	 * Received READY message during the "Double Echo Broadcast" protocol, calls
	 * NetworkService to add it to the data structures.
	 */
	@PostMapping(PathConfiguration.SERVER_READY_ENDPOINT)
	public void ready(@RequestBody SecureDTO secureDTO, @PathVariable String sendingServerId) throws ApplicationException {
		RequestDTO requestDTO = (RequestDTO) CryptoService.serverExtractEncryptedData(secureDTO, RequestDTO.class, ServerApplication.serverId);
		if (requestDTO == null)
			throw new ApplicationException("[SERVER " + ServerApplication.serverId + "] SecureDTO object was corrupt or malformed, was not possible to extract the information.");
		System.out.println("\n[SERVER" + ServerApplication.serverId + "] Received a ready from " + requestDTO.getServerId());

		// Verify if the "ready" comes really from a server
		verifyServerSignatureAndNonce(secureDTO, sendingServerId, PathConfiguration.SERVER_READY);

		networkService.ready(requestDTO);
	}


	/***********************************************************************************************/
	/* 						Auxiliary Functions	- Byzantine Atomic Register						   */
	/***********************************************************************************************/

	@PostMapping(PathConfiguration.READ_COMPLETE_ENDPOINT)
	public void readComplete(@RequestBody SecureDTO secureDTO) throws ApplicationException {
		ReadCompleteDTO readCompleteDTO = (ReadCompleteDTO) CryptoService.serverExtractEncryptedData(secureDTO, ReadCompleteDTO.class, ServerApplication.serverId);
		if(readCompleteDTO == null)
			throw new ApplicationException("[SERVER " + ServerApplication.serverId + "] SecureDTO object was corrupt or malformed, was not possible to extract the information.");
		System.out.println("\n[SERVER" + ServerApplication.serverId + "] Received a READ COMPLETE from " + readCompleteDTO.getClientId());

		verifyRequestSignatureAndNonce(secureDTO, readCompleteDTO.getClientId(), PathConfiguration.SUBMIT_REPORT_ENDPOINT);

		ByzantineAtomicRegisterService.readCompleteReceived(readCompleteDTO);
	}



	/***********************************************************************************************/
	/* 							Auxiliary Functions	- Secure Channels							   */
	/***********************************************************************************************/

	/**
	 * Validate if the SecureDTO which encapsulates the request is valid by
	 * 	1. if SecureDTO != null then is valid
	 * 	2. if verifyProofOfWork(SecureDTO) then is valid
	 * @return the encapsulated request if it was not corrupted or malformed, or null otherwise
	 * @throws ApplicationException if some validations fails
	 */
	@SuppressWarnings("unchecked")
	private <T> T validateRequest(SecureDTO request, Class<T> returnClass) throws ApplicationException {
		if (request == null)
			throw new ApplicationException("[SERVER " + ServerApplication.serverId + "] SecureDTO object was corrupt or malformed.");

		verifyProofOfWork(request);
		
		return (T) CryptoService.serverExtractEncryptedData(request, returnClass, ServerApplication.serverId);
	}
	
	/**
	 * Verifies if the proof of work of a request is valid and if it is not throws a exception
	 */
	private void verifyProofOfWork(SecureDTO sec) throws ApplicationException {
		// Verify if the solution is correct
		if(!ProofOfWorkService.verifySolution(sec)) {
			System.out.println("[Server id: " + ServerApplication.serverId+"] Proof of work failed.");
			throw new ApplicationException("Invalid proof of work.");
		} else {
			System.out.println("[Server id: " + ServerApplication.serverId+"] Proof of work valid.");
		}
	}
	
	/**
	 * Verifies if the signature of a client request is valid and if it is not throws a exception
	 */
	private void verifyRequestSignatureAndNonce(SecureDTO sec, String userId, String endpoint) throws ApplicationException {
		// Verifies the signature of the Secure DTO
		if (!CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getClientPublicKey(userId))) {
			throw new SignatureCheckFailedException("Digital signature check of client at " + endpoint + " failed.");
		}

		// Verifies if the nonce is repeated, if not adds it to the database to the according user.
		((UserService) this.userService).verifyNonce(userId, sec.getNonce());
	}


	/**
	 * Verifies if the signature of a server request is valid and if it is not throws a exception
	 */
	private void verifyServerSignatureAndNonce(SecureDTO sec, String serverId, String endpoint) throws ApplicationException {
		// Verifies the signature of the Secure DTO
		if (!CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getServerPublicKey(serverId))) {
			throw new SignatureCheckFailedException("Digital signature check of server at " + endpoint + " with serverId: " + serverId + " failed.");
		}

		// Verifies if the nonce is repeated, if not adds it to the database to the according user.
		((UserService) this.userService).verifyNonce(serverId, sec.getNonce());
	}
	
	/**
	 * Handles the exception occurred in some step while trying to process the request
	 * @param sec The request arrived at the server
	 * @param e The exception thrown
	 * @return null if it was not possible to get the session key
	 * @throws ApplicationException the exception thrown setted with the session 
	 *         key for encryption if all went good in handling the exception
	 */
	private SecureDTO handleException(SecureDTO sec, ApplicationException e) throws ApplicationException {
		System.out.println("\n[SERVER " + ServerApplication.serverId + "] Exception caught, rethrowing for ExceptionHandler.");
		
		SecretKey sk = CryptoService.getServerSecretKeyFromDTO(sec, ServerApplication.serverId);
		if(sk == null) return null;

		e.setSecretKey(sk);
		throw e;
	}
}
