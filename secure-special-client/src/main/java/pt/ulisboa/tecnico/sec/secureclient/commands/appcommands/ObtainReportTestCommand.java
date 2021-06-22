package pt.ulisboa.tecnico.sec.secureclient.commands.appcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.commands.Command;
import pt.ulisboa.tecnico.sec.secureclient.services.ProofOfWorkService;
import pt.ulisboa.tecnico.sec.secureclient.services.SpecialUserServiceWithRegisters;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.DTOFactory;
import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestLocationDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.SecureDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

public class ObtainReportTestCommand extends Command {
	private SpecialUserServiceWithRegisters userService = new SpecialUserServiceWithRegisters();

    public static final int EXPECTED_ARGUMENTS = 1;

	@Override
	public void execute(List<String> arguments) throws ApplicationException {
		verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);
		System.out.println("[Tests] TODO");
        switch (arguments.get(0)) {
            case "1":   reportFromInvalidUser(); break;
            case "2":   reportFromAnotherUser(); break;
            case "3":   reportWithInvalidSignature(); break;
            case "4":   reportWithInvalidEpoch(); break;
            case "5":   packetNonceRepeated(); break;
        }
	}
	
	private void reportFromInvalidUser() {
		System.out.println("[Test case 1] It will try to obtain a report from an invalid user (no-existant). It shall fail.");
		
		// the data
		String userIdSender = SpecialClientApplication.userId;
		String userIdRequested = "100100100";
		int epoch = 1;

		obtainReport(userIdSender, userIdRequested, epoch);
	}
	
	private void reportFromAnotherUser() {
		System.out.println("[Test case 2] It will try to obtain a valid report from a valid user that will not have access. It shall fail.");
		
		// the data
		String userIdSender = "1";
		String userIdRequested = "2";
		int epoch = 1;

		obtainReport(userIdSender, userIdRequested, epoch);
	}
	
	private void reportWithInvalidSignature() throws ApplicationException {
		System.out.println("[Test case 3] It will try to obtain a report but it will send a packet with an invalid signature. It shall fail.");
		
		// the data
		String userIdSender = SpecialClientApplication.userId;
		String userIdRequested = SpecialClientApplication.userId;
		int epoch = 1;

		// Prepare the body of the HTTP request
        RequestLocationDTO req = new RequestLocationDTO();
        req.setUserIDSender(userIdSender);
        req.setUserIDRequested(userIdRequested);
        req.setEpoch(epoch);

		// Build secure dtos
		byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
		ArrayList<SecureDTO> secureDTOS = new ArrayList<>();
		for (int serverId = 1; serverId <= ByzantineConfigurations.NUMBER_OF_SERVERS; serverId++) {
			// Create secureDTO that will be sent to respective servers
			SecureDTO secureDTO = CryptoService.generateNewSecureDTO(req, userIdSender, randomBytes, serverId + "");

			// Build the proof of work
			secureDTO.setProofOfWork(ProofOfWorkService.findSolution(secureDTO.getData()));

			// Sign the DTO with an invalid signature
			secureDTO.setDigitalSignature("Fake digital signature");

			secureDTOS.add(secureDTO);
		}

		ReportDTO report = this.userService.obtainInfo(secureDTOS, randomBytes, PathConfiguration.GET_REPORT_ENDPOINT);
        System.out.println("Obtained report: ");
		if (report != null)
			System.out.println(report.toString());
	}
	
	private void reportWithInvalidEpoch() {
		System.out.println("[Test case 4] It will try to obtain a report from a valid user but in an invalid epoch. It shall fail.");
		
		// the data
		String userIdSender = SpecialClientApplication.userId;
		String userIdRequested = "1";
		int epoch = 100100100;

		obtainReport(userIdSender, userIdRequested, epoch);		
	}
	
	private void packetNonceRepeated() throws ApplicationException {
		System.out.println("[Test case 5] It will obtain a report and then try to obtain it again with replay attack (same nonce). It shall fail for the 2nd one.");
		System.out.println("[Test case 5] NOTE: For this test case it will submit a test report and obtain it.");
		
		// the data
		String userIdSender = SpecialClientApplication.userId;
		String userIdRequested = SpecialClientApplication.userId;
		int epoch = 1;
		
		// Submitting the test report for this test case
		submitTestReport(epoch);
		
		// Prepare the body of the HTTP request
        RequestLocationDTO req = new RequestLocationDTO();
        req.setUserIDSender(userIdSender);
        req.setUserIDRequested(userIdRequested);
        req.setEpoch(epoch);

        System.out.println("[Special Client "+ SpecialClientApplication.userId+"] Trying to obtain the reports...");

		// Build secure dtos
		byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
		ArrayList<SecureDTO> secureDTOS = new ArrayList<>();
		for (int serverId = 1; serverId <= ByzantineConfigurations.NUMBER_OF_SERVERS; serverId++) {
			// Create secureDTO that will be sent to respective servers
			SecureDTO secureDTO = CryptoService.generateNewSecureDTO(req, userIdSender, randomBytes, serverId + "");

			// Build the proof of work
			secureDTO.setProofOfWork(ProofOfWorkService.findSolution(secureDTO.getData()));

			// Sign the DTO with an invalid signature
			CryptoService.signSecureDTO(secureDTO, CryptoUtils.getClientPrivateKey(SpecialClientApplication.userId));

			secureDTOS.add(secureDTO);
		}

		// Sends DTOs
        ReportDTO report1 = this.userService.obtainInfo(secureDTOS, randomBytes, PathConfiguration.GET_REPORT_ENDPOINT);
        System.out.println("Obtained report: ");
		if (report1 != null)
			System.out.println(report1.toString());


		ReportDTO report2 = this.userService.obtainInfo(secureDTOS, randomBytes, PathConfiguration.GET_REPORT_ENDPOINT);
        System.out.println("Obtained report: ");
		if (report2 != null) 
			System.out.println(report2.toString());
	}
	
	// AUXILIAR FUNCTIONS
	private void obtainReport(String userIdSender, String userIdRequested, int epoch) {
		try {
			ReportDTO report = this.userService.obtainLocationReport(userIdSender, userIdRequested, epoch);
			System.out.println("Obtained report: ");
			if (report != null)
				System.out.println(report.toString());
		} catch (ApplicationException e) {
			System.out.println("[Special Client "+ SpecialClientApplication.userId+"] Error obtaining report: " + e.getMessage());
		}
	}
	
	private void submitTestReport(int epoch) {
		RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(100100, 100100, epoch, SpecialClientApplication.userId, "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epoch, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epoch, "2", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO2);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epoch, "4", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO3);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));
		
        try {
            userService.submitLocationReport(SpecialClientApplication.userId, report);
        } catch (ApplicationException e) {
        	System.out.println("[Special Client "+ SpecialClientApplication.userId+"] Error submitting test report: " + e.getMessage());
        }
    }
	
}
