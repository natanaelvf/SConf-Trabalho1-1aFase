package objects;

public class Request {
	private int id;
	private double amount;
	private int fromID;
	private int toID;
	private QRCode qrCode;
	
	public Request(int id, double amount, int fromID, int toID) {
		this.id = id;
		this.amount = amount;
		this.fromID = fromID;
		this.toID = toID;
		this.qrCode = null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getFromID() {
		return fromID;
	}

	public void setFromID(int fromID) {
		this.fromID = fromID;
	}

	public int getToID() {
		return toID;
	}

	public void setToID(int toID) {
		this.toID = toID;
	}

	public QRCode getQRCode() {
		return qrCode;
	}

	public void setQRCode(QRCode qrCode) {
		this.qrCode = qrCode;
	}
	
}
