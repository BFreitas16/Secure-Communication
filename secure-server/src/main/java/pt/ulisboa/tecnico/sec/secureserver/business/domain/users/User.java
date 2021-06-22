package pt.ulisboa.tecnico.sec.secureserver.business.domain.users;

import java.util.ArrayList;
import java.util.List;
import pt.ulisboa.tecnico.sec.secureserver.business.domain.reports.Report;

import javax.persistence.*;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "Client")
@NamedQueries({
		@NamedQuery(name = User.FIND_BY_USER_ID, query = "SELECT c FROM User c WHERE c.userId =:" + User.FIND_BY_USER_ID_USERID),
		@NamedQuery(name = User.FIND_NONCE, query = "SELECT Count(*) FROM User u INNER JOIN u.nonces n WHERE n in(:"+User.FIND_NONCE_NONCE+")" +
				"AND u.userId =:"+ User.FIND_NONCE_USER_ID)
})
public class User {

	public static final String FIND_BY_USER_ID = "User.findByUserId";
	public static final String FIND_BY_USER_ID_USERID = "userId";

	public static final String FIND_NONCE = "User.findNonce";
	public static final String FIND_NONCE_NONCE = "nonce";
	public static final String FIND_NONCE_USER_ID = "userId";

	public User() {}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long identifier;

	@Column(unique = true, nullable = false, name = "user_id")
	private String userId;
	
	@Column(nullable = false, name = "is_special_user")
	private int isSpecialUser = 0; // 0 == not special : 1 == special

	@OneToMany(cascade = ALL, mappedBy = "user")
	private List<Report> reports = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "nonces", joinColumns = @JoinColumn)
	@Column(name = "nonce_value")
	private List<String> nonces = new ArrayList<>();

	public User(String userId) {
		this.userId = userId;
	}
	
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	public void addNonceReceived(String nonce) {
		nonces.add(nonce);
	}

	/**
	 * @return the reports
	 */
	public List<Report> getReports() {
		return reports;
	}
	
	public Report createAndSaveReport(String userId, int epoch, int x, int y, String digitalSignature) {
		Report newReport = new Report(this, epoch, x, y, digitalSignature);
		
		this.reports.add(newReport);
		return newReport;
	}
	
	public void setAsSpecialUser() {
		this.isSpecialUser = 1;
	}
	
	public boolean isSpecialUser() {
		return this.isSpecialUser != 0;
	}
	
	@Override
	public String toString() {
		// building report list String
		StringBuilder sb = new StringBuilder();
		for (Report report : reports)
			sb.append(report.toString() + "\n");
		
		return "I am " + userId + " and i have the following Reports:\n" + sb.toString();
	}
	
}
