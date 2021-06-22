package pt.ulisboa.tecnico.sec.secureserver.business.domain.reports;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Type;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.users.User;

import javax.persistence.*;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "Report")
@NamedQueries({
		@NamedQuery(name = Report.FIND_REPORT_BY_EPOCH_AND_LOCATION, query = "SELECT r FROM Report r WHERE r.epoch =:" +
				Report.FIND_REPORT_BY_EPOCH_AND_LOCATION_EPOCH + " AND r.x =:" +
				Report.FIND_REPORT_BY_EPOCH_AND_LOCATION_LOCATION_X + " AND r.y =:" +
				Report.FIND_REPORT_BY_EPOCH_AND_LOCATION_LOCATION_Y
		),
		@NamedQuery(name = Report.FIND_REPORT_BY_USER_ID_AT_EPOCH, query = "SELECT r FROM Report r WHERE r.epoch =:" +
				Report.FIND_REPORT_BY_USER_ID_AT_EPOCH_EPOCH + " AND r.user.userId = (SELECT u.userId FROM User u WHERE u.userId =:" +
				Report.FIND_REPORT_BY_USER_ID_AT_EPOCH_USER_ID + ")"
		)
})
public class Report {

	public static final String FIND_REPORT_BY_EPOCH_AND_LOCATION = "Report.findReportByEpochAndLocation";
	public static final String FIND_REPORT_BY_EPOCH_AND_LOCATION_EPOCH = "epoch";
	public static final String FIND_REPORT_BY_EPOCH_AND_LOCATION_LOCATION_X = "x";
	public static final String FIND_REPORT_BY_EPOCH_AND_LOCATION_LOCATION_Y = "y";

	public static final String FIND_REPORT_BY_USER_ID_AT_EPOCH = "Report.findReportByUserIdAtEpoch";
	public static final String FIND_REPORT_BY_USER_ID_AT_EPOCH_EPOCH = "epoch";
	public static final String FIND_REPORT_BY_USER_ID_AT_EPOCH_USER_ID = "userId";

	public Report(){}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long identifier;

	@ManyToOne
	@JoinColumn
	private User user;

	@Column(nullable = false, name = "epoch")
	private int epoch;

	@Column(nullable = false, name = "x")
	private int x;

	@Column(nullable = false, name = "y")
	private int y;

	@Column(nullable = false, name = "digital_signature")
	@Type(type = "text")
	private String digitalSignature;

	@OneToMany(cascade = ALL, mappedBy = "report")
	private List<ReportProof> reportProofList = new ArrayList<>();

	public Report(User user, int epoch, int x, int y, String digitalSignature) {
		this.user = user;
		this.epoch = epoch;
		this.x = x;
		this.y = y;
		this.digitalSignature = digitalSignature;

	}

	/**
	 * @return the x of the location
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y of the location
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the userId of the report's owner
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @return the epoch of the report
	 */
	public int getEpoch() {
		return epoch;
	}

	
	/**
	 * @return the reportProofList
	 */
	public List<ReportProof> getReportProofList() {
		return reportProofList;
	}

	/**
	 * @return the digitalSignature
	 */
	public String getDigitalSignature() {
		return digitalSignature;
	}

	public void setReportProofList(List<ReportProof> reportProofList) {
		this.reportProofList = reportProofList;
	}

	@Override
	public String toString() {
		return "Report of " + user.getUserId() + " at " + epoch + " epoch. Location: " + x + "," + y;
	}

}
