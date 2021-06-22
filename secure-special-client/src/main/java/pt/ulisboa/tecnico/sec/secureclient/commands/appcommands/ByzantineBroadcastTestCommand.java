package pt.ulisboa.tecnico.sec.secureclient.commands.appcommands;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.commands.Command;
import pt.ulisboa.tecnico.sec.secureclient.exceptions.NotSufficientArgumentsException;
import pt.ulisboa.tecnico.sec.secureclient.services.ProofOfWorkService;
import pt.ulisboa.tecnico.sec.secureclient.services.SpecialUserServiceWithRegisters;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService.generateSecretKey;

public class ByzantineBroadcastTestCommand extends Command {
    private SpecialUserServiceWithRegisters userService = new SpecialUserServiceWithRegisters();

    public static final int EXPECTED_ARGUMENTS = 1;

    private int epochInReport = -1;

    @Override
    public void execute(List<String> arguments) throws ApplicationException {
        verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);
        System.out.println("[Tests] All submitted reports will be sent by the SpecialUserClient but simulating as it was sent by a byzantine user. The reports sent are mock reports created manually.");
        System.out.println("[Tests] To avoid epoch collisions, we are using negative epochs in the reports. (DO NOT PANIC THIS IS ALL ACCORDING TO THE PLAN)");
        switch (arguments.get(0)) {
            case "1":   sendMessageToOnlyOneServer(); break;
            case "2":   sendFakeEchoAndReadyMessages(); break;
            case "3":   sendDifferentMessagesToServers(); break;
        }
    }

    private void sendReport(ReportDTO report) throws ApplicationException {
        try {
            userService.submitLocationReport(SpecialClientApplication.userId, report);
        } catch (NumberFormatException e) {
            throw new NotSufficientArgumentsException("[Special Client\"" + SpecialClientApplication.userId +
                    "\"] You must specify a test case. Check the possible tests with command \"help\"");
        }
    }

    private void sendMessageToOnlyOneServer() throws ApplicationException {
        System.out.println("[Test case 1] It will send a request to only 1 server (correct server), this will prevent the Broadcast Algorithm of working and an exception will be thrown saying that the quorum wasn't met.");
        int serverId = 1;
        String clientId = "1";
        String url = PathConfiguration.getSubmitReportURL(serverId);

        // Build some random object
        ReportDTO report = buildReport(10,2);

        // Create the secure DTO for the random object above
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        SecureDTO sec = CryptoService.generateNewSecureDTO(report, SpecialClientApplication.userId, randomBytes, serverId + "");
        sec.setProofOfWork(ProofOfWorkService.findSolution(sec.getData()));
        CryptoService.signSecureDTO(sec, CryptoUtils.getClientPrivateKey(SpecialClientApplication.userId));

        // Send to the server
        SecureDTO response = userService.sendSecureDtoToServerOrClient(url, sec);

        // The message should be the thrown exception
        CryptoService.extractEncryptedData(response, ReportDTO.class, generateSecretKey(randomBytes));
    }

    private void sendFakeEchoAndReadyMessages() throws ApplicationException {
        System.out.println("[Test case 2] It will send invalid echos and ready messages. It should fail with an invalid digital signature exception.");
        int serverId = 1;
        String clientId = "1";
        String url = PathConfiguration.getServerUrl(1) + PathConfiguration.SERVER_ECHO + serverId;
        System.out.println(url);

        // Build some random object
        RequestDTO req = new RequestDTO();
        req.setClientId("1");
        req.setReportDTO(new ReportDTO());
        req.setServerId("1");
        req.setRequestLocationDTO(new RequestLocationDTO());
        req.setRequestUserProofsDTO(new RequestUserProofsDTO());

        // Create the secure DTO for the random object above
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        SecureDTO sec = CryptoService.generateNewSecureDTO(req, SpecialClientApplication.userId, randomBytes, serverId + "");
        sec.setProofOfWork(ProofOfWorkService.findSolution(sec.getData()));
        CryptoService.signSecureDTO(sec, CryptoUtils.getClientPrivateKey(clientId));

        // Send to the server
        SecureDTO response = userService.sendSecureDtoToServerOrClient(url, sec);

        System.out.println("Done, check server logs.");
    }

    private void sendDifferentMessagesToServers() throws ApplicationException {
        System.out.println("[Test case 3] It will submit different values to simulate a possible byzantine client. The broadcast algorithm should fail as a quorum won't be met.");
        ReportDTO report1 = buildReport(10,2);
        ReportDTO report2 = buildReport(5,7);

        // Build secure dtos
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        ArrayList<SecureDTO> secureDTOS = new ArrayList<>();
        for (int serverId = 1; serverId <= ByzantineConfigurations.NUMBER_OF_SERVERS; serverId++) {
            // Create secureDTO that will be sent to respective servers
            SecureDTO secureDTO;

            if(serverId % 2 == 0)
                secureDTO = CryptoService.generateNewSecureDTO(report1, SpecialClientApplication.userId, randomBytes, serverId + "");
            else
                secureDTO = CryptoService.generateNewSecureDTO(report2, SpecialClientApplication.userId, randomBytes, serverId + "");

            // Build the proof of work
            secureDTO.setProofOfWork(ProofOfWorkService.findSolution(secureDTO.getData()));

            // Sign the DTO with an invalid signature
            CryptoService.signSecureDTO(secureDTO, CryptoUtils.getClientPrivateKey(SpecialClientApplication.userId));

            secureDTOS.add(secureDTO);
        }

        epochInReport--;
        userService.sendInfo(secureDTOS, randomBytes, PathConfiguration.SUBMIT_REPORT_ENDPOINT);
    }

    private ReportDTO buildReport(int x, int y) {
        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(x, y, epochInReport, SpecialClientApplication.userId, "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epochInReport, "2", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO2);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epochInReport, "4", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO3);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

        return report;
    }
}
