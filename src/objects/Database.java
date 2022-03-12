package objects;

import java.util.HashMap;

public class Database {
	

	private final int MAX_ID = 999999999;
	private final int MIN_ID = 100000000;
	
	private HashMap<Integer,User> userBase;
	private HashMap<Integer,Request> requestBase;

	public Request getRequestById(int requestId) {
		return requestBase.get(requestId);
	}

	public int getUniqueRequestId() {
		int id = (int)Math.floor(Math.random()*(MAX_ID-MIN_ID+1)+MIN_ID);
		while(requestBase.containsKey(id)) {
			id = (int)Math.floor(Math.random()*(MAX_ID-MIN_ID+1)+MIN_ID);
		}
		return id;
	}

	public User getUserById(int userID) {
		return userBase.get(userID);
	}

}
