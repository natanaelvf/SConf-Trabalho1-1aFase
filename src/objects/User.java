package objects;

import java.util.ArrayList;
import java.util.HashSet;

public class User {	
	private int id;
	private double balance;
	private HashSet<Request> requests;

	public User(int userID, double balance, ArrayList<Request> requests) {
		// TODO Auto-generated constructor stub
	}

	public void addRequest(Request request) {
		this.requests.add(request);
	}

	public void transfer(int userID, double amount) {
		// TODO Auto-generated method stub
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HashSet<Request> getRequests() {
		return requests;
	}

	public void removeRequest(Request request) {
		this.requests.remove(request);
	}

}
