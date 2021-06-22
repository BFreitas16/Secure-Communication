package pt.ulisboa.tecnico.sec.secureserver.business.handlers;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pt.ulisboa.tecnico.sec.secureserver.ServerApplication;
import pt.ulisboa.tecnico.sec.services.dto.ErrorMessageResponse;
import pt.ulisboa.tecnico.sec.services.dto.SecureDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.*;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles exceptions that are thrown during the server execution
 * and answers to the client accordingly with a customized ErrorMessageResponse object.
 */
@RestControllerAdvice
public class ExceptionHandlerControl {

    @ExceptionHandler(value = ApplicationException.class)
    private SecureDTO handleApplicationException(ApplicationException e, HttpServletRequest req) {
        System.out.println("Invalid application exception occurred.");
        System.out.println("Error: " + e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("Application Exception", e.getLocalizedMessage());
        return CryptoService.createSecureDTO(err, e.getSessionKey(), "", CryptoUtils.getServerPrivateKey(ServerApplication.serverId));
    }

    @ExceptionHandler(value = InvalidReportException.class)
    private SecureDTO handleInvalidReportException(InvalidReportException e, HttpServletRequest req) {
        System.out.println("Invalid report exception occurred.");
        System.out.println("Error: " + e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("Invalid report exception", e.getLocalizedMessage());
        return CryptoService.createSecureDTO(err, e.getSessionKey(), "", CryptoUtils.getServerPrivateKey(ServerApplication.serverId));
    }

    @ExceptionHandler(value = SignatureCheckFailedException.class)
    private SecureDTO handleSignatureCheckException(SignatureCheckFailedException e, HttpServletRequest req) {
        System.out.println("Signature check fail exception occurred.");
        System.out.println("Error: " + e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("Signature check fail exception.", e.getLocalizedMessage());
        return CryptoService.createSecureDTO(err, e.getSessionKey(), "", CryptoUtils.getServerPrivateKey(ServerApplication.serverId));
    }

    @ExceptionHandler(value = RepeatedNonceException.class)
    private SecureDTO handleRepeatedNonceException(RepeatedNonceException e, HttpServletRequest req) {
        System.out.println("Repeated Nonce exception occurred.");
        System.out.println("Error: " + e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("Repeated nonce exception", e.getLocalizedMessage());
        return CryptoService.createSecureDTO(err, e.getSessionKey(), "", CryptoUtils.getServerPrivateKey(ServerApplication.serverId));
    }

    @ExceptionHandler(value = InvalidRequestException.class)
    private SecureDTO handleInvalidRequestException(InvalidRequestException e, HttpServletRequest req) {
        System.out.println("Invalid Request exception occurred.");
        System.out.println("Error: " + e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("Invalid Request Exception", e.getLocalizedMessage());
        return CryptoService.createSecureDTO(err, e.getSessionKey(), "", CryptoUtils.getServerPrivateKey(ServerApplication.serverId));
    }

    @ExceptionHandler(value = NoRequiredPrivilegesException.class)
    private SecureDTO handleNoRequiredPrivilegeException(NoRequiredPrivilegesException e, HttpServletRequest req) {
        System.out.println("No required Privilege exception occurred.");
        System.out.println("Error: " + e.getLocalizedMessage());

        ErrorMessageResponse err = new ErrorMessageResponse("No required privilege Exception", e.getLocalizedMessage());
        return CryptoService.createSecureDTO(err, e.getSessionKey(), "", CryptoUtils.getServerPrivateKey(ServerApplication.serverId));
    }

}
