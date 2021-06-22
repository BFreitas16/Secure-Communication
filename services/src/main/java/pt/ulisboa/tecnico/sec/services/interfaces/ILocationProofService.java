package pt.ulisboa.tecnico.sec.services.interfaces;

import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;

public interface ILocationProofService {
	
	public ProofDTO requestLocationProof(String url, RequestProofDTO request);

}
