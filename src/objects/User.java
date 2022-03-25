package objects;

import java.util.HashSet;

public class User {
	private int userID;
	private double balance;
	private HashSet<Request> requests;

	public User(int userID, double balance, HashSet<Request> requests) {
		this.userID = userID;
		this.balance = balance;
		this.requests = requests;
	}

	public void addRequest(Request request) {
		this.requests.add(request);
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public int getID() {
		return userID;
	}

	public void setID(int id) {
		this.userID = id;
	}

	public HashSet<Request> getRequests() {
		return requests;
	}

	public void removeRequest(Request request) {
		this.requests.remove(request);
	}

}
