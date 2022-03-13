package objects;

import java.util.ArrayList;
import java.util.HashSet;

public class Group {

	private int groupID;
	private User owner;
	private HashSet<User> userList;
	private HashSet<Request> requestList;
	private ArrayList<HashSet<Request>> requestListHistory;

	public Group(int groupID, User loggedUser) {
		this.setGroupID(groupID);
		this.setOwner(loggedUser);
		this.setUserList(new HashSet<User>());
	}

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public HashSet<User> getUserList() {
		return userList;
	}

	public void setUserList(HashSet<User> userList) {
		this.userList = userList;
	}

	public void addUser(User user) {
		this.userList.add(user);
	}

	public void addRequest(Request request) {
		this.requestList.add(request);
	}

	public HashSet<Request> getRequestList() {
		return requestList;
	}

	public void setRequestList(HashSet<Request> requestList) {
		this.requestList = requestList;
	}

	public void addRequestListToHistory(HashSet<Request> requests) {
		this.requestListHistory.add(requests);
		
	}

	public ArrayList<HashSet<Request>> getHistory() {
		return this.requestListHistory;
	}

}
