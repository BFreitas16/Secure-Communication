package pt.ulisboa.tecnico.sec.secureserver.services;

import pt.ulisboa.tecnico.sec.services.dto.SecureDTO;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;


public class ProofOfWorkService {
	
	private ProofOfWorkService() {}
	
    public static boolean verifySolution(SecureDTO sec) {
        String solution = sec.getProofOfWork();
        String messageSecureDto = sec.getData() + solution;

        try {
            byte[] hashSecureDto = CryptoUtils.computeSHA256Hash(messageSecureDto);
            return hashSecureDto[0] == 0;
        } catch(Exception e) {
            return false;
        }
    }
}
