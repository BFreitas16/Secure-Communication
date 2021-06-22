package pt.ulisboa.tecnico.sec.secureserver.business.domain.reports;

import org.hibernate.annotations.Type;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.User;

import javax.persistence.*;

@Entity
@Table(name = "Proof")
@NamedQueries({
		@NamedQuery(name = ReportProof.FIND_PROOFS_BY_USER_ID_AT_EPOCH, query="SELECT p FROM ReportProof p WHERE p.epoch =:" +
				ReportProof.FIND_PROOFS_BY_USER_ID_AT_EPOCH_EPOCH + " AND p.user.userId = (SELECT u.userId FROM User u WHERE u.userId =:" +
				ReportProof.FIND_PROOFS_BY_USER_ID_AT_EPOCH_USER_ID + ")"
		)
})
public class ReportProof {
	
	public static final String FIND_PROOFS_BY_USER_ID_AT_EPOCH = "ReportProof.findProofsByUserIdAtEpoch";
	public static final String FIND_PROOFS_BY_USER_ID_AT_EPOCH_EPOCH = "epoch";
	public static final String FIND_PROOFS_BY_USER_ID_AT_EPOCH_USER_ID = "userId";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long identifier;

	@ManyToOne
	@JoinColumn
	private User user;

	@Column(nullable = false, name = "epoch")
	private int epoch;

	@ManyToOne
	@JoinColumn
	private Report report;

	@Column(nullable = false, name = "digital_signature")
	@Type(type = "text")
	private String digitalSignature;

	public ReportProof() {}

	public ReportProof(User user, int epoch, Report report, String digitalSignature) {
		this.user = user;
		this.epoch = epoch;
		this.report = report;
		this.digitalSignature = digitalSignature;
	}

	/**
	 * @return the userId
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @return the epoch
	 */
	public int getEpoch() {
		return epoch;
	}

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * @return the digitalSignature
	 */
	public String getDigitalSignature() {
		return digitalSignature;
	}

}
