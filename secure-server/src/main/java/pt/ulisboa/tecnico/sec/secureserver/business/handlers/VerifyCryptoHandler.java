package pt.ulisboa.tecnico.sec.secureserver.business.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.User;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.UserCatalog;
import pt.ulisboa.tecnico.sec.services.dto.ProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.ReportDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.RepeatedNonceException;
import pt.ulisboa.tecnico.sec.services.exceptions.SignatureCheckFailedException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class VerifyCryptoHandler {

    @Autowired
    private UserCatalog userCatalog;

    /**
     *  This function must verify all cryptographic material from the report and its associated proofs.
     *  Verified Material:
     *  0º Verify Nonce
     *  1º Digital signature of the ReportDTO
     *  2º Digital signature of all proofs
     *  3º Check for duplicated proofs ( 1 witness can only issue one proof for the associated report)
     *  4º Check if the proofs belong to the submitted report.
     */
    public ReportDTO verifyAllCryptoConditions(ReportDTO reportDTO) throws ApplicationException {
        String userId = reportDTO.getRequestProofDTO().getUserID();
        RequestProofDTO reqProof = reportDTO.getRequestProofDTO();

        if (!verifyDigitalSignature(userId, reqProof, true))
            throw new SignatureCheckFailedException("Signature check failed of request proof.");

        List<ProofDTO> validProofs = checkDuplicatedProofsAndOwnership(reqProof, reportDTO.getProofsList());
        return new ReportDTO(reqProof, validProofs);
    }

    // Verifies the digital signature of the passed object
    private <T> boolean verifyDigitalSignature(String userId, T toVerify, boolean isRequestProof) throws ApplicationException {
        if(isRequestProof) {
            RequestProofDTO reqProof = (RequestProofDTO) toVerify;

            return CryptoService.checkDigitalSignature(
                    CryptoService.buildRequestProofMessage(reqProof),
                    reqProof.getDigitalSignature(),
                    CryptoUtils.getClientPublicKey(userId)
            );
        } else {
            ProofDTO proof = (ProofDTO) toVerify;

            return CryptoService.checkDigitalSignature(
                    CryptoService.buildProofMessage(proof),
                    proof.getDigitalSignature(),
                    CryptoUtils.getClientPublicKey(userId)
            );
        }
    }

    // Verifies ownership and duplicated proofs
    private List<ProofDTO> checkDuplicatedProofsAndOwnership(RequestProofDTO reqProof, List<ProofDTO> proofsList) throws ApplicationException {
        List<String> userIdsVerified = new ArrayList<>();
        List<ProofDTO> validProofs = new ArrayList<>();

        // Add the id associated with the request proof to verified users, otherwise the prover
        // could issue a proof for himself.
        userIdsVerified.add(reqProof.getUserID());

        for (ProofDTO proofDTO : proofsList) {
            // Check if the proof is for the same epoch as the requestProof
            if(reqProof.getEpoch() != proofDTO.getEpoch())
                continue;

            String proofUserId = proofDTO.getUserID();
            // Validate digital signature of each proof
            if(verifyDigitalSignature(proofUserId, proofDTO, false)) {
                // If digital signature is valid, verify if the user is already in the list, if not add the current proof as valid
                if(!userIdsVerified.contains(proofUserId)) {
                    // Verify ownership
                    RequestProofDTO proofAssociatedRequestProof = proofDTO.getRequestProofDTO();
                    if(reqProof.getDigitalSignature().equals(proofAssociatedRequestProof.getDigitalSignature())) {
                        // If everything is valid add to the valid proofs list
                        userIdsVerified.add(proofDTO.getUserID());
                        validProofs.add(proofDTO);
                    }
                }
            }
        }

        return validProofs;
    }

    public void verifyNonce(String userId, String nonce) throws ApplicationException {
        // If nonce doesn't exist add it
        if(userCatalog.checkIfNonceExists(userId, nonce))
            throw new RepeatedNonceException("Nonce repeated.");

        User user = userCatalog.getUserById(userId);
        //user.addNonceReceived(nonce);
        userCatalog.updateUser(user, nonce);
    }
}
