package objects;

public class Request {
	private int id;
	private double amount;
	private int userID;
	private QrCode qrCode;

	
	public Request(int id, double amount, int userID, QrCode qrCode) {
		this.id = id;
		this.amount = amount;
		this.userID = userID;
		this.qrCode = qrCode;
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
	public int getID() {
		return id;
	}
	public void setID(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "Amount: " + this.amount + " User that owes you: " + this.userID;
	}

}
