package pt.ulisboa.tecnico.sec.secureclient.services;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.sec.secureclient.ClientApplication;
import pt.ulisboa.tecnico.sec.services.dto.ClientResponseDTO;
import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;
import pt.ulisboa.tecnico.sec.services.interfaces.ILocationProofService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

@Service
public class LocationProofService implements ILocationProofService {
	
	private static List<String> nonces = new ArrayList<>();

	@Override
	public ProofDTO requestLocationProof(String url, RequestProofDTO request) {
		
		ClientResponseDTO clientResponse = NetworkService.sendMessageToClient(request, url);

		if(clientResponse != null && clientResponse.getErr() != null) {
			System.out.println("[Client " + ClientApplication.userId + "] Error occurred asking for proof: " + clientResponse.getErr().getDescription());
			return null;
		}

		// Replay attack verification
		if(nonces.contains(clientResponse.getNonce())) {
			System.out.println("[Client " + ClientApplication.userId + "] Error occurred asking for proof: Replay Attack - nonce repeated.");
			return null;
		}
		nonces.add(clientResponse.getNonce());
		
		// Verify check digital signature
		if (!CryptoService.checkDigitalSignature(CryptoService.buildClientResponseMessage(clientResponse), clientResponse.getDigitalSignature(), CryptoUtils.getClientPublicKey(clientResponse.getProof().getUserID()))) {
			System.out.println("Can't validate request proof signature.");
			return null;
		}
		
		return clientResponse.getProof();
	}

}
