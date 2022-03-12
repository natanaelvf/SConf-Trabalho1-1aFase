package objects;

public class Request {
	private int id;
	private double amount;
	private int userID;
	
	public Request(int id, double amount, int userID) {
		this.id = id;
		this.amount = amount;
		this.userID = userID;
	}
	
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

}
