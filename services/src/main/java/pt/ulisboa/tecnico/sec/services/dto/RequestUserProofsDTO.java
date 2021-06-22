package pt.ulisboa.tecnico.sec.services.dto;

import java.util.List;
import java.util.Objects;

public class RequestUserProofsDTO {

	private String userIdSender;
	private String userIdRequested;
	private List<Integer> epochs;

	public RequestUserProofsDTO() {}

	/**
	 * @return the userIdSender
	 */
	public String getUserIdSender() {
		return userIdSender;
	}
	/**
	 * @param userIdSender the userIdSender to set
	 */
	public void setUserIdSender(String userIdSender) {
		this.userIdSender = userIdSender;
	}
	/**
	 * @return the userIdRequested
	 */
	public String getUserIdRequested() {
		return userIdRequested;
	}
	/**
	 * @param userIdRequested the userIdRequested to set
	 */
	public void setUserIdRequested(String userIdRequested) {
		this.userIdRequested = userIdRequested;
	}
	/**
	 * @return the epochs
	 */
	public List<Integer> getEpochs() {
		return epochs;
	}
	/**
	 * @param epochs the epochs to set
	 */
	public void setEpochs(List<Integer> epochs) {
		this.epochs = epochs;
	}

	@Override
	public String toString() {
		return "Request User Proofs sent by " + userIdSender + " for user " + userIdRequested + " at epochs " + epochs.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestUserProofsDTO other = (RequestUserProofsDTO) obj;
		if (epochs == null) {
			if (other.epochs != null)
				return false;
		} else if (!epochs.equals(other.epochs))
			return false;
		if (userIdRequested == null) {
			if (other.userIdRequested != null)
				return false;
		} else if (!userIdRequested.equals(other.userIdRequested))
			return false;
		if (userIdSender == null) {
			if (other.userIdSender != null)
				return false;
		} else if (!userIdSender.equals(other.userIdSender))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(userIdSender, userIdRequested, epochs);
	}
}
