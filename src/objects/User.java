package objects;

import java.util.ArrayList;
import java.util.HashMap;

public class User {	
	private double balance;
	private ArrayList<HashMap<User, Double>> requestList;

	public User(String string) {
		// TODO Auto-generated constructor stub
	}

	public void addRequest(HashMap<User, Double> request) {
		this.requestList.add(request);
	}

	public void transfer(User userID, double amount) {
		// TODO Auto-generated method stub
	}

	public double getBalance() {
		return this.balance;
	}

}
