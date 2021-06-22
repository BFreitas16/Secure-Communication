package pt.ulisboa.tecnico.sec.secureserver.services;

import pt.ulisboa.tecnico.sec.secureserver.ServerApplication;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.interfaces.ISpecialUserService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Service used by the operations: obtainUsersAtLocation & requestMyProofs
 */
public class ByzantineRegularRegisterService {
	
	private ByzantineRegularRegisterService() {}
	
    // Timestamp of the request, used by both server and client
    private static AtomicLong timestamp = new AtomicLong(0);

    /**
     * obtainUsersAtLocation
     *
     * Called by the client when it wants to read something from the register.
     *
     * It will return the saved value with a timestamp, the same rid and saved signature.
     *
     */
    public static SecureDTO receiveReadRequest(RequestLocationDTO req, SecureDTO sec, ISpecialUserService userService) throws ApplicationException {
        SpecialUserResponseDTO result = userService.obtainUsersAtLocation(req.getUserIDSender(), req.getX(), req.getY(), req.getEpoch());
        SecureDTO response = CryptoService.generateResponseSecureDTO(sec, result, ServerApplication.serverId);

        response.setTimestamp(timestamp.get());
        response.setRid(sec.getRid());
        CryptoService.signSecureDTO(response, CryptoUtils.getServerPrivateKey(ServerApplication.serverId));

        return response;
    }

    /**
     * requestMyProofs
     *
     * Called by the client when it wants to read something from the register.
     *
     * It will return the saved value with a timestamp, the same rid and saved signature.
     *
     */
    public static SecureDTO receiveReadRequest(RequestUserProofsDTO req, SecureDTO sec, ISpecialUserService userService) throws ApplicationException {
        ResponseUserProofsDTO result = userService.requestMyProofs(req.getUserIdSender(), req.getUserIdRequested(), req.getEpochs());
        SecureDTO response = CryptoService.generateResponseSecureDTO(sec, result, ServerApplication.serverId);

        response.setTimestamp(timestamp.get());
        response.setRid(sec.getRid());
        CryptoService.signSecureDTO(response, CryptoUtils.getServerPrivateKey(ServerApplication.serverId));

        return response;
    }
}
