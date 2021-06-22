package pt.ulisboa.tecnico.sec.secureserver.business.domain.users;

import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

@Repository
public class UserCatalog {

	@PersistenceContext
	private EntityManager em;

	public UserCatalog() { }
	
	public User getUserById(String userId) throws ApplicationException {
		try {
			TypedQuery<User> query = em.createNamedQuery(User.FIND_BY_USER_ID, User.class);
			query.setParameter(User.FIND_BY_USER_ID_USERID, userId);
			return query.getSingleResult();
		} catch(Exception e) {
			throw new ApplicationException("UserId " + userId + " not found.");
		}
	}

	/**
	 *	Returns true if the nonce exists
	 */
	public boolean checkIfNonceExists(String userId, String nonce) throws ApplicationException {
		try {
			TypedQuery<Long> query = em.createNamedQuery(User.FIND_NONCE, Long.class);
			query.setParameter(User.FIND_NONCE_NONCE, nonce);
			query.setParameter(User.FIND_NONCE_USER_ID, userId);

			return query.getSingleResult() > 0;
		} catch(Exception e) {
			throw new ApplicationException("Error on checkIfNonceExists class UserCatalog");
		}
	}

	@Transactional
	public void updateUser(User u, String nonce) {
		u.addNonceReceived(nonce);
		em.persist(u);
	}
}
