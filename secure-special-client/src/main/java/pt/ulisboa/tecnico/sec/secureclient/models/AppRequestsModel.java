package pt.ulisboa.tecnico.sec.secureclient.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AppRequestsModel {
	// the user requested
	private String userId;
	// the epoch requested
	private int epoch = 1;
	// the x location requested
	private int x;
	// the y location requested
	private int y;
	// the list of epochs asked (in format <epoch>,<epoch>,... or  <epoch>, <epoch> ,...  <epoch> <epoch> ...)
	private String epochs;
	// the answer from the server
	private String result;

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the epoch
	 */
	public int getEpoch() {
		return epoch;
	}

	/**
	 * @param epoch the epoch to set
	 */
	public void setEpoch(int epoch) {
		this.epoch = epoch;
	}
	
	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * @return the list of epochs
	 */
	public String getEpochs() {
		return epochs;
	}
	
	/**
	 * @return the list of epochs
	 */
	public List<Integer> getEpochsAsList() {
		// split by: have zero or more than 1 ',' and spaces
		return Arrays.stream(epochs.split("[,* *]")).map(Integer::parseInt).collect(Collectors.toList());
	}
	
	/**
	 * @param epochs the list of epochs
	 */
	public void setEpochs(String epochs) {
		this.epochs = epochs;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

}
