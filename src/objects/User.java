package objects;

import java.util.Arrays;
import java.util.Set;

public class User {
	private int userID;
	private double balance;
	private Set<Request> requests;

	public User(int userID, double balance, Set<Request> requests) {
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
	
	public Set<Request> getRequests() {
		return requests;
	}

	public void removeRequest(Request request) {
		this.requests.remove(request);
	}

	@Override
	public String toString() {
		return "User [userID=" + userID + ", balance=" + balance + ", requests=" + Arrays.toString(requests.toArray()) + "]";
	}

}
