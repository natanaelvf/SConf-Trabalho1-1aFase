package objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import javax.crypto.Cipher;

import java.util.Scanner;
import java.util.Set;

public class Database {
	private static final int MAX_ID = 999999999;
	private static final int MIN_ID = 100000000;
	private static Cipher ciRSA;
	private static final String UNICODE_FORMAT = "UTF-8";
	private HashMap<Integer,User> userBase = new HashMap<>();
	private HashMap<Integer,Request> requestBase = new HashMap<>();
	private HashMap<Integer,Group> groupBase = new HashMap<>();
	private HashMap<Integer,QRCode> qrCodeBase = new HashMap<>();

	Random r = new Random();

	public Request getRequestByID(int requestID) {
		return requestBase.get(requestID);
	}

	public QRCode getQRCodeByID(int qrCodeID){
		return qrCodeBase.get(qrCodeID);
	}

	public Request getRequestByQRCode(QRCode qrCode) {
		for (Request request : this.requestBase.values()) {
			if (request.getQRCode() == qrCode) {
				return request;
			}
		}
		return null;
	}

	public int getUniqueRequestID() {
		int id = r.nextInt(MAX_ID-MIN_ID)+MIN_ID;
		while(requestBase.keySet().contains(id)) {
			id = r.nextInt(MAX_ID-MIN_ID)+MIN_ID;
		}
		return id;
	}

	public int getUniqueQRCodeID() {
		int id = r.nextInt(MAX_ID-MIN_ID)+MIN_ID;
		while(qrCodeBase.keySet().contains(id)) {
			id = r.nextInt(MAX_ID-MIN_ID)+MIN_ID;
		}
		return id;
	}

	public User getUserByID(int userID) {
		return userBase.get(userID);
	}

	public void addQRCode (QRCode qrCode){
		this.qrCodeBase.put(qrCode.getID(), qrCode);
	}

	public void addRequest(Request request) throws IOException {
		Scanner sc = new Scanner(new File(".\\src\\bds\\users.txt"));

		StringBuilder sb = new StringBuilder();

		PrintWriter printout = new PrintWriter(".\\src\\bds\\users.txt");

		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] splitLine = line.split(":");

			int userId = Integer.parseInt(splitLine[0]);
			if(request.getToID() == userId) {
				sb.append(line + request.getId() +"-"+request.getAmount() + request.getFromID() + "\r\n");
			} else {
				sb.append(line + "\r\n");
			}
		}

		this.requestBase.put(request.getFromID(), request);
		printout.write(sb.toString());

		printout.close();
	}

	public void removeRequest(Request request) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(".\\src\\bds\\users.txt"));

		StringBuilder sb = new StringBuilder();

		PrintWriter printout = new PrintWriter(".\\src\\bds\\users.txt");

		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] splitLine = line.split(":");

			int userId = Integer.parseInt(splitLine[0]);
			if(request.getToID() == userId) {
				line.replaceAll(request.getId() + "-" + request.getAmount() + "-" + request.getFromID(), "");
			} 
			sb.append(line);
		}

		this.requestBase.put(request.getFromID(), request);
		printout.write(sb.toString());

		printout.close();

		this.requestBase.remove(request.getFromID(), request);
	}

	public Group getGroupByID(int groupID) {
		return this.groupBase.get(groupID);
	}

	public void addGroup(Group group) {
		this.groupBase.put(group.getGroupID(), group);
	}


	public Set<Group> getGroupsByOwner(User user) {
		HashSet<Group> result = new HashSet<>();
		for (Entry<Integer,Group> group : this.groupBase.entrySet()) {
			if (isOwner(group, user)) {
				result.add(this.getGroupByID(group.getKey()));
			}
		}
		return result;
	}

	public Set<Group> getGroupsByClient(User user) {
		Set<Group> result = getGroupsByOwner(user);
		for (Entry<Integer,Group> group : this.groupBase.entrySet()) {
			for (Integer user2 : group.getValue().getUserList()) {
				if (user2 == user.getID() && !result.contains(group.getValue())) {
					result.add(group.getValue());
				}
			}
		}
		return result;
	}

	private boolean isOwner(Entry<Integer, Group> group, User user) {
		return group.getValue().getOwnerUser() == user.getID();
	}

	public void removeRequestFromGroup(Request request) {
		for (Group group : this.groupBase.values()) {
			HashSet<Request> requests = group.getRequestList();
			if (requests.contains(request)) {
				requests.remove(request);
				group.setRequestList(requests);
			}
			if (group.getRequestList().isEmpty()) {
				group.addRequestListToHistory(requests);
			}
		}
	}

	public void addUser(User user) {
		this.userBase.put(user.getID(), user);
	}

	public void getUsersFromDB() throws FileNotFoundException {
		Scanner sc = new Scanner(new File(".\\src\\bds\\users.txt"));

		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] splitLine = line.split(":");

			int userId = Integer.parseInt(splitLine[0]);
			int userBalance = Integer.parseInt(splitLine[1]);
			HashSet<Request> userRequests = new HashSet<>();

			String[] requests = splitLine[2].split(";");
			for (String request : requests) {
				if (request!="") {
					String[] splitRequest = request.split("-");
					Request newRequest = new Request(Integer.parseInt(splitRequest[0]), 
							Double.parseDouble(splitRequest[1]),
							userId, 
							Integer.parseInt(splitRequest[2]));
					userRequests.add(newRequest);
				}
			}
			User user = new User(userId,userBalance, userRequests);
			this.addUser(user);
		}
	}

	public void getGroupsFromDB() throws FileNotFoundException {
		try (Scanner sc = new Scanner(new File(".\\src\\bds\\auth.txt"));) {
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] splitLine = line.split(":");

				int groupId = Integer.parseInt(splitLine[0]);
				User loggedUser = this.getUserByID(Integer.parseInt(splitLine[1]));

				String[] userIds = splitLine[2].split("-");
				HashSet<Integer> users = new HashSet<>();

				for(String userId : userIds) {
					users.add(Integer.parseInt(userId));
				}

				Group group = new Group(groupId, loggedUser.getID(), users);
				this.addGroup(group);
			}
		}
	}

	public void getGroupRequests() throws FileNotFoundException {
		try(Scanner sc = new Scanner(new File(".\\src\\bds\\groupsRequests.txt"));) {
			while(sc.hasNextLine()) {

				String line = sc.nextLine();
				String[] splitLine = line.split(":");

				Group group = this.getGroupByID(Integer.parseInt(splitLine[0]));

				String[] requestsString = splitLine[1].split(";");

				for(String requestString : requestsString) {
					if (!requestString.isEmpty()) {
						String[] requestSplit = requestString.split("-");

						int requestID = Integer.parseInt(requestSplit[0]);
						int amount = Integer.parseInt(requestSplit[1]);
						int fromID = Integer.parseInt(requestSplit[2]);

						for(Integer toID: group.getUserList()) {
							group.addRequest(new Request(requestID, amount, fromID, toID));
						}
					}
				}
			}
		}
	}

	public void getGroupRequestHistory() throws FileNotFoundException {
		try(Scanner sc = new Scanner(new File(".\\src\\bds\\groupsRequestHistory.txt"));) {
			while(sc.hasNextLine()) {

				String line = sc.nextLine();
				String[] splitLine = line.split(":");

				Group group = this.getGroupByID(Integer.parseInt(splitLine[0]));

				String[] requestsString = splitLine[1].split(";");

				HashSet<Request> requests = new HashSet<>(); 

				for(String requestString : requestsString) {

					String[] requestSplit = requestString.split("-");

					int requestID = Integer.parseInt(requestSplit[0]);
					int amount = Integer.parseInt(requestSplit[1]);
					int fromID = Integer.parseInt(requestSplit[2]);

					for(Integer toID: group.getUserList()) {
						requests.add(new Request(requestID, amount, fromID, toID));
					}
				}
				group.addRequestListToHistory(requests);
			}
		}
	}
	
	public byte[] encryptString(String dataToEncrypt, Key myKey) {

		try {

			byte[] text = dataToEncrypt.getBytes(UNICODE_FORMAT);
			ciRSA.init(Cipher.ENCRYPT_MODE, myKey);
			byte[] textEncrypted = ciRSA.doFinal(text);

			return textEncrypted;

		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}
}