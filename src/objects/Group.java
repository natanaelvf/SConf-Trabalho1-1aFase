package objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;

public class Group {

	private int groupID;
	private int ownerUser;
	private HashSet<Integer> userList;
	private HashSet<Request> requestList;
	private HashSet<Request> requestListHistory;

	public Group(int groupID, int ownerUser, HashSet<Integer> userList) {
		this.groupID = groupID;
		this.ownerUser = ownerUser;
		this.userList = userList;
	}

	public int getGroupID() {
		return groupID;
	}

	public int getOwnerUser() {
		return ownerUser;
	}

	public HashSet<Integer> getUserList() {
		return userList;
	}

	public HashSet<Request> getRequestList() {
		return requestList;
	}

	public HashSet<Request> getRequestListHistory() {
		return requestListHistory;
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

	public void addUser(int userToAdd) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(".\\src\\bds\\groups.txt"));

		StringBuilder sb = new StringBuilder();

		PrintWriter printout = new PrintWriter(".\\src\\bds\\groups.txt");

		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] splitLine = line.split(":");
			
			sb.append(line);

			int groupIDDB = Integer.parseInt(splitLine[0]);
			if (this.groupID == groupIDDB) {
				sb.append("-" + userToAdd);
			}
			sb.append("\r\n");
		}

		userList.add(userToAdd);
		
		printout.write(sb.toString());
		printout.close();
	}

	public void addRequest(Request request) throws FileNotFoundException {
		PrintWriter printout = new PrintWriter(".\\src\\bds\\groupsRequests.txt");
		Scanner sc = new Scanner(new File(".\\src\\bds\\groupsRequests.txt"));
		StringBuilder sb = new StringBuilder();

		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			sb.append(line);
			String[] splitLine = line.split(":");

			int groupId = Integer.parseInt(splitLine[0]);
			if (groupId == request.getToID()) {
				sb.append(request.getId() + "-" + request.getFromID() + "-" + request.getAmount() + ";\r\n");
			}
		}
		
		printout.write(sb.toString());
		printout.close();
		sc.close();
		
		printout = new PrintWriter(".\\src\\bds\\users.txt");
		sc = new Scanner(new File(".\\src\\bds\\users.txt"));
		sb = new StringBuilder();

		for(Integer userID : this.userList) {
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				sb.append(line);
				String[] splitLine = line.split(":");

				int userIDDB = Integer.parseInt(splitLine[0]);
				if (userID == userIDDB) {
					sb.append(request.getId() + "-" + request.getFromID() + "-" + request.getAmount() + ";");
				}
				sb.append("\r\n");
			}
		}

		printout.write(sb.toString());
		printout.close();
		sc.close();
	}

	public void addRequestListToHistory(HashSet<Request> requests) throws FileNotFoundException {
		PrintWriter printout = new PrintWriter(".\\src\\bds\\groupsRequestHistory.txt");
		Scanner sc = new Scanner(new File(".\\src\\bds\\groupsRequestHistory.txt"));
		StringBuilder sb = new StringBuilder();

		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			sb.append(line);
			String[] splitLine = line.split(":");
			
			int groupId = Integer.parseInt(splitLine[0]);
			for(Request request: requests) {
				if (groupId == request.getToID()) {
					sb.append(request.getId() + "-" + request.getFromID() + "-" + request.getAmount() + ";");
				}
			}
			sb.append("\r\n");
		}
		
		printout.write(sb.toString());
		printout.close();
		sc.close();
	}

	public void setRequestList(HashSet<Request> requestList) {
		this.requestList = requestList;
	}
}
