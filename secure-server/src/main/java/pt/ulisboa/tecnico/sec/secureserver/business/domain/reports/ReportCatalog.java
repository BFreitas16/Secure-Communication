package pt.ulisboa.tecnico.sec.secureserver.business.domain.reports;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.User;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

@Repository
public class ReportCatalog {

	@PersistenceContext
	private EntityManager em;

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void saveReport(Report report) {
		em.persist(report);
	}

	/**
	 *	Special user asks for all reports at certain epoch and location
	 */
	public List<Report> getReportsOfLocationAt(int x, int y, int epoch) throws ApplicationException {
		try {
			TypedQuery<Report> query = em.createNamedQuery(Report.FIND_REPORT_BY_EPOCH_AND_LOCATION, Report.class);
			query.setParameter(Report.FIND_REPORT_BY_EPOCH_AND_LOCATION_EPOCH, epoch);
			query.setParameter(Report.FIND_REPORT_BY_EPOCH_AND_LOCATION_LOCATION_X, x);
			query.setParameter(Report.FIND_REPORT_BY_EPOCH_AND_LOCATION_LOCATION_Y, y);

			return query.getResultList();
		} catch(Exception e) {
			throw new ApplicationException("Error obtaining report at location x:" + x + " y:" + y + " on epoch:" + epoch);
		}
	}

	/**
	 * Retrieve report from a user at a certain epoch
	 */
	public Report getReportOfUserIdAtEpoch(String userId, int epoch) throws ApplicationException {
		try {
			TypedQuery<Report> query = em.createNamedQuery(Report.FIND_REPORT_BY_USER_ID_AT_EPOCH, Report.class);
			query.setParameter(Report.FIND_REPORT_BY_USER_ID_AT_EPOCH_EPOCH, epoch);
			query.setParameter(Report.FIND_REPORT_BY_USER_ID_AT_EPOCH_USER_ID, userId);

			return query.getSingleResult();
		} catch(NoResultException e) {
			return null;
		} catch(Exception e) {
			throw new ApplicationException("Error obtaining report of userId:" + userId + " at epoch:" + epoch);
		}
	}

	public List<ReportProof> getProofsWrittenByUserAtEpochs(User userRequest, List<Integer> epochs) throws ApplicationException {
		List<ReportProof> proofs = new ArrayList<>();
		for (int epoch : epochs) {
			try {
				TypedQuery<ReportProof> query = em.createNamedQuery(ReportProof.FIND_PROOFS_BY_USER_ID_AT_EPOCH, ReportProof.class);
				query.setParameter(ReportProof.FIND_PROOFS_BY_USER_ID_AT_EPOCH_EPOCH, epoch);
				query.setParameter(ReportProof.FIND_PROOFS_BY_USER_ID_AT_EPOCH_USER_ID, userRequest.getUserId());
				
				List<ReportProof> proofsResultList = query.getResultList();
				proofs.addAll(proofsResultList);
			} catch(Exception e) {
				throw new ApplicationException("Error obtaining proofs of userId: " + userRequest.getUserId() + " at epoch " + epoch);
			}
		}
		return proofs;
	}
}
