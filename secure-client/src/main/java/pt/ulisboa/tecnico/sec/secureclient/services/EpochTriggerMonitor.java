package pt.ulisboa.tecnico.sec.secureclient.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import pt.ulisboa.tecnico.sec.secureclient.ClientApplication;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.DTOFactory;
import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.OutOfEpochException;
import pt.ulisboa.tecnico.sec.services.utils.Grid;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

@Component
public class EpochTriggerMonitor {
	
	private UserService userService;
	
	private LocationProofService locationProofService;
	
	@Autowired
	public EpochTriggerMonitor(UserService userService, LocationProofService locationProofService) {
		this.userService = userService;
		this.locationProofService = locationProofService;
	}
	
	@Scheduled(fixedRate = 10000, initialDelay = 5000)
	public void publish() throws ApplicationException {
		// For debug only, to test without constantly doing epoch stuff
		if(true)
			return;

		// In case this is on the bottom and if an exception occurs this value won't be increased and the client
		// will be delayed, possibly permanently.
		ClientApplication.incrementEpoch();

		// If the current epoch surpasses the number of designed epochs then quit
		if(ClientApplication.epoch > Grid.numberOfEpochs()) {
			System.out.println("\n[Client "+ClientApplication.userId+"] Nothing to do here, epochs have run out...");
			return;
		}

		// Proceed to ask for proofs, build a report and then submit it to the server
		int myId =  Integer.parseInt(ClientApplication.userId);
		int[] myLocation = findSelfLocation();

		// Don't do anything if not present in current epoch grid
		if(myLocation[0] == -1) {
			System.out.println("[Client " + ClientApplication.userId + "] Not present in the grid at epoch: " + ClientApplication.epoch + ". Currently in vacation, try again later.");
			return;
		}

		List<Integer> witnesses = gatherWitnesses(myLocation, myId);

		// Create request proof
		RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(myLocation[0], myLocation[1], 
				ClientApplication.epoch, ClientApplication.userId, "");
		requestProofDTO.setNonce(CryptoUtils.generateNonce());

		// Digitally sign the request proof dto
		CryptoService.signRequestProofDTO(requestProofDTO);

		// Send proof requests to nearby witnesses
		List<ProofDTO> proofs = gatherProofs(requestProofDTO, witnesses);

		// Send report to the server
		ReportDTO reportDTO = DTOFactory.makeReportDTO(requestProofDTO, proofs);

		System.out.println("\n[Client "+ClientApplication.userId+"] Sending report to server:\n" + reportDTO.toString());
		userService.submitLocationReport(ClientApplication.userId, reportDTO);

		System.out.println("\n[Client "+ClientApplication.userId+"] Report Sent!\nTrying to obtain the Report...");
		ReportDTO reportResponse = userService.obtainLocationReport(ClientApplication.userId, ClientApplication.userId, ClientApplication.epoch);

		// In case the report wasn't successfully submitted to the server the server will return null
		if(reportResponse != null)
			System.out.println("[Client "+ClientApplication.userId+"] Received report:\n" + reportResponse.toString());
	}

	private List<ProofDTO> gatherProofs(RequestProofDTO requestProofDTO, List<Integer> witnesses) {
		List<ProofDTO> proofs = new ArrayList<>();
		int curr = -1;
		for (int witness : witnesses) {
			try {
				curr = witness;
				String url = PathConfiguration.getClientURL(witness);
				System.out.println("Asking for proof at " + url);
				ProofDTO proof = locationProofService.requestLocationProof(url, requestProofDTO);

				if(proof != null)
					proofs.add(proof);
			} catch(Exception e) {
				System.out.println(("[ Client "+ClientApplication.userId+"] Wasn't able to contact client "+ curr));
			}
		}
		return proofs;
	}

	private List<Integer> gatherWitnesses(int[] myLocation, int myId) throws OutOfEpochException {
		System.out.println("[Client" + ClientApplication.userId + "] My location: (" + myLocation[0] + "," + myLocation[1] + ")");
		List<Integer> witnesses = Grid.getUsersInRangeAtEpoch( myId, ClientApplication.epoch, 1);

		System.out.println("[Client" + ClientApplication.userId + "] My neighbors are:");

		witnesses.forEach(x -> System.out.println("Neighbor " + x));
		System.out.println("\n");
		return witnesses;
	}

	private int[] findSelfLocation() throws OutOfEpochException {
		int myId = Integer.parseInt(ClientApplication.userId);

		System.out.println("\n[Client " + ClientApplication.userId + "] Going to grid at client epoch: " + ClientApplication.epoch);
		return Grid.getLocationOfUserAtEpoch(myId, ClientApplication.epoch);
	}

}
