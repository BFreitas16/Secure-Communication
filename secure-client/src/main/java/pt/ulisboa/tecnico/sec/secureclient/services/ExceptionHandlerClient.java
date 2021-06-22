package pt.ulisboa.tecnico.sec.secureclient.services;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pt.ulisboa.tecnico.sec.secureclient.ClientApplication;
import pt.ulisboa.tecnico.sec.services.dto.ClientResponseDTO;
import pt.ulisboa.tecnico.sec.services.dto.ErrorMessageResponse;
import pt.ulisboa.tecnico.sec.services.exceptions.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles exceptions that are thrown during the server execution
 * and answers to the client accordingly with a customized ErrorMessageResponse object.
 */
@RestControllerAdvice
public class ExceptionHandlerClient {

    @ExceptionHandler(value = ProverOutOfRangeException.class)
    private ClientResponseDTO handleProverOutOfRangeException(ProverOutOfRangeException e, HttpServletRequest req) {
        System.out.println("\nProver Out of Range exception occurred.");
        System.out.println("Error: " + e.getLocalizedMessage());
        ErrorMessageResponse err = new ErrorMessageResponse("Prover Out of Range Exception", e.getLocalizedMessage());

        ClientResponseDTO clientResp = new ClientResponseDTO();
        clientResp.setErr(err);

        return clientResp;
    }

    @ExceptionHandler(OutOfEpochException.class)
    private ClientResponseDTO handleOutOfEpochException(OutOfEpochException e, HttpServletRequest req) {
        System.out.println("\nOut of epoch exception occurred.");
        System.out.println("[Client" + ClientApplication.userId + "] User " + ClientApplication.userId + " has no grid associated with epoch " + ClientApplication.epoch);


        ErrorMessageResponse err = new ErrorMessageResponse("Out of Epoch Exception", e.getLocalizedMessage());
        ClientResponseDTO clientResp = new ClientResponseDTO();
        clientResp.setErr(err);

        return clientResp;
    }

    @ExceptionHandler(UnreachableClientException.class)
    private ClientResponseDTO handleUnreachableClientException(UnreachableClientException e, HttpServletRequest req) {
        System.out.println("\nOut of epoch exception occurred.");
        System.out.println(e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("Out of Epoch Exception", e.getLocalizedMessage());
        ClientResponseDTO clientResp = new ClientResponseDTO();
        clientResp.setErr(err);

        return clientResp;
    }

    @ExceptionHandler(RepeatedNonceException.class)
    private ClientResponseDTO handleRepeatedNonceException(RepeatedNonceException e, HttpServletRequest req) {
        System.out.println("\nReplay Attack Detected");
        System.out.println(e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("Repeated Nonce Exception", e.getLocalizedMessage());
        ClientResponseDTO clientResp = new ClientResponseDTO();
        clientResp.setErr(err);

        return clientResp;
    }

    @ExceptionHandler(SignatureCheckFailedException.class)
    private ClientResponseDTO handleSignatureCheckFailedException(SignatureCheckFailedException e, HttpServletRequest req) {
        System.out.println("\nSignature Check Failed Exception");
        System.out.println(e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("Signature Check Failed Exception", e.getLocalizedMessage());
        ClientResponseDTO clientResp = new ClientResponseDTO();
        clientResp.setErr(err);

        return clientResp;
    }

    @ExceptionHandler(ApplicationException.class)
    private void handleApplicationException(ApplicationException e, HttpServletRequest req) {
        System.out.println("Application Exception error: " + e.getLocalizedMessage());
    }
}
