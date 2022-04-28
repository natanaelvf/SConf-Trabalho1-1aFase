package objects;

import java.util.ArrayList;
import java.util.HashSet;

public class Group {

	private int groupID;
	private int ownerUser;
	private HashSet<Integer> userList;
	private HashSet<Request> requestList;
	private ArrayList<HashSet<Request>> requestListHistory;

	public Group(int groupID, int ownerUser, HashSet<Integer> userList) {
		this.groupID = groupID;
		this.ownerUser = ownerUser;
		this.userList = userList;
	}

	public int getGroupID() {
		return groupID;
	}


	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}


	public int getOwnerUser() {
		return ownerUser;
	}


	public void setOwnerUser(int ownerUser) {
		this.ownerUser = ownerUser;
	}


	public HashSet<Integer> getUserList() {
		return userList;
	}


	public void setUserList(HashSet<Integer> userList) {
		this.userList = userList;
	}


	public HashSet<Request> getRequestList() {
		return requestList;
	}


	public void setRequestList(HashSet<Request> requestList) {
		this.requestList = requestList;
	}


	public ArrayList<HashSet<Request>> getRequestListHistory() {
		return requestListHistory;
	}


	public void setRequestListHistory(ArrayList<HashSet<Request>> requestListHistory) {
		this.requestListHistory = requestListHistory;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Group ID: " + this.groupID + "\n");
		sb.append("Group Owner: " + ownerUser + "\n");
		sb.append("Request List: " + "\n");
		for (Request request: this.requestList) {
			sb.append(request.toString() + "\n");
		}
		return sb.toString();
	}

	public void addUser(User userByID) {
		// TODO Auto-generated method stub
		
	}

	public void addRequest(Request request) {
		// TODO Auto-generated method stub
		
	}

	public void addRequestListToHistory(HashSet<Request> requests) {
		// TODO Auto-generated method stub
	}
}
