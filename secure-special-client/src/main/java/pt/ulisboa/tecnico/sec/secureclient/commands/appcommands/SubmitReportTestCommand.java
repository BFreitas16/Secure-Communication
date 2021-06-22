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
import pt.ulisboa.tecnico.sec.services.interfaces.ISpecialUserService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubmitReportTestCommand extends Command {
    private ISpecialUserService userService = new SpecialUserServiceWithRegisters();

    public static final int EXPECTED_ARGUMENTS = 1;

    private int epochInReport = -1;

    @Override
    public void execute(List<String> arguments) throws ApplicationException {
        verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);
        System.out.println("[Tests] All submitted reports will be sent by the SpecialUserClient but simulating as it was sent by a byzantine user. The reports sent are mock reports created manually.");
        System.out.println("[Tests] To avoid epoch collisions, we are using negative epochs in the reports. (DO NOT PANIC THIS IS ALL ACCORDING TO THE PLAN)");
        switch (arguments.get(0)) {
            case "1":   validReport(); break;
            case "2":   reportDuplicatedProofs(); break;
            case "3":   reportDuplicatedSameEpoch(); break;
            case "4":   packetNonceRepeated(); break;
            case "5":   reportWithMessageStealing(); break;
            case "6":   reportWithDigitalSignatureInvalid(); break;
            case "7":   reportWithLessProofsThanNecessary(); break;
            case "8":   reportProofsInDifferentEpoch(); break;
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

    /**
     *  Creates a valid report
     */
    private void validReport() throws ApplicationException {
        System.out.println("[Test case 1] It will submit a valid report. It shall succeed.");

        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(10, 2, epochInReport, SpecialClientApplication.userId, "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epochInReport, "2", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO2);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epochInReport, "4", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO3);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

        epochInReport--;
        sendReport(report);
    }

    private void reportDuplicatedProofs() throws ApplicationException {
        System.out.println("[Test case 2] It will submit an invalid report. It shall fail as the repeated proofs will be filtered and there will be less proofs than necessary to be approved.");
        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(10, 2, epochInReport, SpecialClientApplication.userId, "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO2);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO3);

        ProofDTO proofDTO4 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO4);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

        epochInReport--;
        sendReport(report);
    }

    private void reportDuplicatedSameEpoch() throws ApplicationException {
        System.out.println("[Test case 3] It will submit 2 valid reports but for the same epoch. The 2nd submission will fail as there is already a report for the same epoch.");
        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(10, 2, epochInReport, SpecialClientApplication.userId, "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epochInReport, "2", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO2);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epochInReport, "3", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO3);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

        epochInReport--;
        //Submits the first time
        sendReport(report);
        //It will fail the second time
        sendReport(report);
    }

    private void packetNonceRepeated() throws ApplicationException {
        System.out.println("[Test case 4] It will submit 2 valid reports but with a repeated nonce (Replay Attack). The 2nd submission will fail as the 2nd packet nonce already was seen.");
        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(10, 2, epochInReport, SpecialClientApplication.userId, "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epochInReport, "2", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO2);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epochInReport, "3", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO3);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

        // Build secure dtos
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        ArrayList<SecureDTO> secureDTOS = new ArrayList<>();
        for (int serverId = 1; serverId <= ByzantineConfigurations.NUMBER_OF_SERVERS; serverId++) {
            // Create secureDTO that will be sent to respective servers
            SecureDTO secureDTO = CryptoService.generateNewSecureDTO(report, SpecialClientApplication.userId, randomBytes, serverId + "");

            // Build the proof of work
            secureDTO.setProofOfWork(ProofOfWorkService.findSolution(secureDTO.getData()));

            // Sign the DTO with an invalid signature
            CryptoService.signSecureDTO(secureDTO, CryptoUtils.getClientPrivateKey(SpecialClientApplication.userId));

            secureDTOS.add(secureDTO);
        }

        epochInReport--;
        ((SpecialUserServiceWithRegisters) userService).sendInfo(secureDTOS, randomBytes, PathConfiguration.SUBMIT_REPORT_ENDPOINT);
        ((SpecialUserServiceWithRegisters) userService).sendInfo(secureDTOS, randomBytes, PathConfiguration.SUBMIT_REPORT_ENDPOINT);
    }

    private void reportWithMessageStealing() throws ApplicationException {
        System.out.println("[Test case 5] It will submit a report that was originated by a valid user, but an attacker changed the userId and digital signature (message stolen). It will fail because each proof has the request proof that the witness originally received. When comparing that request proof with the top level request proof, the digital signatures will be different.");
        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(10, 2, epochInReport, "1", "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport, "3", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epochInReport, "2", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO2);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epochInReport, "4", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO3);

        // Request proof stealing, the request was originally generated by userId 1 and digitally signed by him
        requestProofDTO.setUserID(SpecialClientApplication.userId);
        CryptoService.signRequestProofDTO(requestProofDTO);


        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

        epochInReport--;
        sendReport(report);
    }

    private void reportWithDigitalSignatureInvalid() throws ApplicationException {
        System.out.println("[Test case 6] It will submit a report that has an invalid digital signature. It will fail the digital signature check.");
        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(
                10,
                2,
                epochInReport,
                SpecialClientApplication.userId,
                "invalidDigitalSignature");

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epochInReport, "2", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO2);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epochInReport, "4", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO3);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

        epochInReport--;
        sendReport(report);
    }

    private void reportWithLessProofsThanNecessary() throws ApplicationException {
        System.out.println("[Test case 7] It will submit a report that has less proofs than necessary (Byzantine Rule). It will fail because there are less proofs that the minimum necessary to have a valid report.");
        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(10, 2, epochInReport, SpecialClientApplication.userId, "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1));

        epochInReport--;
        sendReport(report);
    }

    private void reportProofsInDifferentEpoch() throws ApplicationException {
        System.out.println("[Test case 8] It will submit a report that has less proofs than necessary, because they are in different epochs than the request proof (the server will filter invalid proofs). It will fail.");
        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(10, 2, epochInReport, SpecialClientApplication.userId, "");
        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proofDTO1 = DTOFactory.makeProofDTO(epochInReport+1, "1", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO2 = DTOFactory.makeProofDTO(epochInReport+2, "2", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ProofDTO proofDTO3 = DTOFactory.makeProofDTO(epochInReport+3, "4", requestProofDTO, "");
        CryptoService.signProofDTO(proofDTO1);

        ReportDTO report = DTOFactory.makeReportDTO(requestProofDTO, Arrays.asList(proofDTO1,proofDTO2,proofDTO3));

        epochInReport--;
        sendReport(report);
    }
}
