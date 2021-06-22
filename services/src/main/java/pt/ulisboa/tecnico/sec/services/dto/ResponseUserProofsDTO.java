package pt.ulisboa.tecnico.sec.services.dto;

import java.util.List;

public class ResponseUserProofsDTO {
	
	private List<ProofDTO> proofs;

	public ResponseUserProofsDTO() {}

	public ResponseUserProofsDTO(List<ProofDTO> proofs) {
		this.proofs = proofs;
	}

	/**
	 * @return the proofs
	 */
	public List<ProofDTO> getProofs() {
		return proofs;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ProofDTO proof : proofs) {
			sb.append(proof.toString() + "\n");
		}
		return "ResponseUserProofsDTO: " + sb.toString();
	}

}
