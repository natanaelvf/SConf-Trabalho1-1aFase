package objects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class Database {
	

	private final int MAX_ID = 999999999;
	private final int MIN_ID = 100000000;
	
	private HashMap<Integer,User> userBase;
	private HashMap<Integer,Request> requestBase;
	private HashMap<Integer,Group> groupBase;
	private HashMap<Integer,QRCode> qrcodeBase;

	public Request getRequestByID(int requestID) {
		return requestBase.get(requestID);
	}

	public QRCode getQRCodeByID(int qrCodeID){
		return qrcodeBase.get(qrCodeID);
	}

	public int getUniqueRequestID() {
		int id = (int)Math.floor(Math.random()*(MAX_ID-MIN_ID+1)+MIN_ID);
		while(requestBase.keySet().contains(id)) {
			id = (int)Math.floor(Math.random()*(MAX_ID-MIN_ID+1)+MIN_ID);
		}
		return id;
	}

	public int getUniqueQRCodeID() {
		int id = (int)Math.floor(Math.random()*(MAX_ID-MIN_ID+1)+MIN_ID);
		while(qrcodeBase.keySet().contains(id)) {
			id = (int)Math.floor(Math.random()*(MAX_ID-MIN_ID+1)+MIN_ID);
		}
		return id;
	}

	public User getUserByID(int userID) {
		return userBase.get(userID);
	}

	public void addRequest(Request request) {
		this.requestBase.put(request.getID(), request);
	}

	public void removeRequest(Request request) {
		this.requestBase.remove(request.getID(), request);
	}

	public Group getGroupByID(int groupID) {
		return this.groupBase.get(groupID);
	}

	public void addGroup(Group group) {
		this.groupBase.put(group.getGroupID(), group);
	}

	public HashSet<Group> getGroupsByOwner(User user) {
		HashSet<Group> result = new HashSet<>();
		for (Entry<Integer,Group> group : this.groupBase.entrySet()) {
			if (isOwner(group, user)) {
				result.add(this.getGroupByID(group.getKey()));
			}
		}
		return result;
	}
	
	public HashSet<Group> getGroupsByClient(User user) {
		HashSet<Group> result = getGroupsByOwner(user);
		for (Entry<Integer,Group> group : this.groupBase.entrySet()) {
			for (User user2 : group.getValue().getUserList()) {
				if (user2.getID() == user.getID() && !result.contains(group.getValue())) {
					result.add(group.getValue());
				}
			}
		}
		return result;
	}
	
	private boolean isOwner(Entry<Integer, Group> group, User user) {
		return group.getValue().getOwner().getID() == user.getID();
	}

	public HashSet<Group> getGroupBase() {
		Group[] groups = (Group[]) this.groupBase.values().toArray();
		return new HashSet<Group>(Arrays.asList(groups));
	}

}
