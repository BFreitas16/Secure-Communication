package pt.ulisboa.tecnico.sec.secureclient.commands.appcommands;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.commands.Command;
import pt.ulisboa.tecnico.sec.secureclient.services.ProofOfWorkService;
import pt.ulisboa.tecnico.sec.secureclient.services.SpecialUserServiceWithRegisters;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.Arrays;
import java.util.List;

public class ByzantineRegisterTestCommand extends Command {
    private SpecialUserServiceWithRegisters userService = new SpecialUserServiceWithRegisters();

    public static final int EXPECTED_ARGUMENTS = 1;
    public static final int epochInReport = -1;

    @Override
    public void execute(List<String> arguments) throws ApplicationException {
        verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);

        switch (arguments.get(0)) {
            case "1":   sendFakeSpontaneousRead(); break;
        }
    }

    private void sendFakeSpontaneousRead() throws ApplicationException {
        System.out.println("[Test case 1] Sending a fake spontaneous read to the client. The client should detect and throw an invalid digital signature exception.");
        int impersonatedServerId = 2;
        int victim = 1;

        String url = PathConfiguration.getSpontaneousReadURL(1,impersonatedServerId);
        System.out.println(url);

        ReportDTO reportDTO = buildReport(10,2);
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        SecureDTO sec = CryptoService.generateNewSecureDTO(reportDTO, SpecialClientApplication.userId, randomBytes, victim + "");
        sec.setProofOfWork(ProofOfWorkService.findSolution(sec.getData()));

        CryptoService.signSecureDTO(sec, CryptoUtils.getClientPrivateKey(SpecialClientApplication.userId));

        userService.sendSecureDtoToServerOrClient(url, sec);
        System.out.println("Done, check client 1 logs.");
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
