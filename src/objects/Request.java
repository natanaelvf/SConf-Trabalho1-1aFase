package objects;

public class Request {
	private int id;
	private double amount;
	private int userID;
	private QrCode qrCode;
	private int qrCodeID;
	
	public Request(int id, double amount, int userID, int qrCodeID) {
		this.id = id;
		this.amount = amount;
		this.userID = userID;
		this.qrCode = new QrCode(qrCodeID, userID, amount, id);
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
