package pt.ulisboa.tecnico.sec.secureclient.commands.appcommands;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.commands.Command;
import pt.ulisboa.tecnico.sec.secureclient.services.SpecialUserServiceWithRegisters;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.DTOFactory;
import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.List;

public class GatherProofsTestCommand extends Command {
    private SpecialUserServiceWithRegisters userService = new SpecialUserServiceWithRegisters();

    public static final int EXPECTED_ARGUMENTS = 1;

    @Override
    public void execute(List<String> arguments) throws ApplicationException {
        verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);
        System.out.println("[Tests] All proof request sent are simulated as being the byzantine user id 3.");

        switch (arguments.get(0)) {
            case "1":   proofOutOfRange(); break;
            case "2":   proofRequestReplayed(); break;
            case "3":   proofRequestInvalidSignature(); break;
        }
    }

    private void proofOutOfRange() {
        System.out.println("[Test case 1] Asking for proof to a client which is not in range. Client should return 'Not in range' error.");

        String url = PathConfiguration.getClientURL(1);

        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(-10, -10,
                1, SpecialClientApplication.userId, "");
        requestProofDTO.setNonce(CryptoUtils.generateNonce());

        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proof = userService.requestLocationProof(url, requestProofDTO);
        if(proof != null)
            System.out.println("Proof response:" + proof.toString());
    }

    private void proofRequestReplayed() {
        System.out.println("[Test case 2] Asking for proof with repeated nonce.");

        String url = PathConfiguration.getClientURL(1);

        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(-10, -10,
                1, "3", "");
        requestProofDTO.setNonce(CryptoUtils.generateNonce());

        CryptoService.signRequestProofDTO(requestProofDTO);

        ProofDTO proof = userService.requestLocationProof(url, requestProofDTO);
        if(proof != null)
            System.out.println("Proof response:" + proof.toString());

        ProofDTO proof2 = userService.requestLocationProof(url, requestProofDTO);
        if(proof2 != null)
            System.out.println("Proof response:" + proof2.toString());
    }

    private void proofRequestInvalidSignature() {
        System.out.println("[Test case 3] Asking for proof to a client with an invalid digital signature.");

        String url = PathConfiguration.getClientURL(1);

        RequestProofDTO requestProofDTO = DTOFactory.makeRequestProofDTO(-10, -10,
                1, "3", "InvalidDigitalSignature");
        requestProofDTO.setNonce(CryptoUtils.generateNonce());

        ProofDTO proof = userService.requestLocationProof(url, requestProofDTO);
        if(proof != null)
            System.out.println("Proof response:" + proof.toString());
    }
}
