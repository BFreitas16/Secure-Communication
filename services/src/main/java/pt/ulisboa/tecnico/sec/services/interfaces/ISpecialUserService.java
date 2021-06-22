package pt.ulisboa.tecnico.sec.services.interfaces;

import pt.ulisboa.tecnico.sec.services.dto.SpecialUserResponseDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

public interface ISpecialUserService extends IUserService {
	
	public SpecialUserResponseDTO obtainUsersAtLocation(String userId, int x, int y, int epoch) throws ApplicationException;

}
