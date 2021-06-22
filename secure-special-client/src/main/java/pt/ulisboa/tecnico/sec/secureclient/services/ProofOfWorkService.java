package pt.ulisboa.tecnico.sec.secureclient.services;

import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

public class ProofOfWorkService {
	
	private ProofOfWorkService() {}

    public static String findSolution(String data) throws ApplicationException {
        int solution = 0;
        byte[] hash;
        try {
            do {
                String message = data + solution;
                hash = CryptoUtils.computeSHA256Hash(message);
                solution++;
            } while (hash[0] != 0);

            return String.valueOf(--solution);
        } catch (Exception e) {
            throw new ApplicationException("Wasn't able to find a solution");
        }
    }
}
