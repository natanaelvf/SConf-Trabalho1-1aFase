import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

import com.google.zxing.WriterException;

import exceptions.GroupAleadyExistsException;
import exceptions.GroupNotFoundException;
import exceptions.IllegalArgumentNumberException;
import exceptions.InsuficientFundsException;
import exceptions.QRCodeNotFoundException;
import exceptions.RequestNotFoundException;
import exceptions.UserAlreadyInGroupException;
import exceptions.UserNotFoundException;
import exceptions.UserNotOwnerException;
import exceptions.UserNotRequesteeException;
import objects.Database;
import objects.Group;
import objects.Request;
import objects.User;

public class TrokoServer {

	public static Application app = new Application();

	public static void main(String[] args) throws IOException, IllegalArgumentNumberException {

		if (args.length > 1) {
			throw new IllegalArgumentNumberException("Demasiados args passados ao servidor!");
		}
		int serverPort = 45678;
		if (args[0] != null) {
			serverPort = Integer.parseInt(args[0]);
		}
		app = new Application();
		Database database = new Database();

		User user1 = new User(100000001, 1000.00, new HashSet<>());
		User user2 = new User(100000002, 2000.00, new HashSet<>());
		User user3 = new User(100000003, 3000.00, new HashSet<>());

		Request request = new Request(100000001, 125.00, 100000003);
		user1.addRequest(request);
		request = new Request(100000002, 250.00, 100000002);
		user1.addRequest(request);
		request = new Request(100000003, 250.00, 100000002);
		user3.addRequest(request);
		database.addUser(user1);
		database.addUser(user2);
		database.addUser(user3);

		HashSet<User> users = new HashSet<>();
		users.add(user1);
		users.add(user2);
		users.add(user3);

		Group group = new Group(100000001, user1, users);

		database.addGroup(group);

		app.database = database;

		System.out.println("Server listening on port: " + serverPort);

		TrokoServer server = new TrokoServer();
		server.startServer(serverPort);
	}

	public void startServer(int port) throws IOException {
		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
	}

	private class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
		}

		public void run() {
			ObjectOutputStream outStream = null;
			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				int user = 0;
				String passwd = null;
				System.out.println("reading user " + user);
				user = Integer.parseInt(inStream.readUTF());
				System.out.println("reading passwd" + passwd);
				passwd = inStream.readUTF();

				System.out.println(user + " " + passwd);
				if (authenticateUser(user, passwd)) {
					outStream.writeUTF("LOGGED");
				}

				User loggedUser;

				if (app.database.getUserByID(user) != null) {
					loggedUser = app.database.getUserByID(user);
				} else {
					loggedUser = new User(user, 2000.00, new HashSet<Request>());
				}

				app.setLoggedUser(loggedUser);

				String input = inStream.readUTF();
				System.out.println(input);

				while (input != "quit" || input != "q") {

					input = inStream.readUTF();
					String[] data = input.split(" ");
					double amount;
					switch (data[0]) {
					case "b":
					case "balance":
						String b = "Your balance is: " + app.viewBalance();
						outStream.writeUTF(b);
						System.out.println(b);
						break;
					case "p":
					case "makepay":
						amount = Double.parseDouble(data[1]);
						app.makePayment(user, amount);
						String p = "Paid " + amount + " to user" + user + "\n"; 
						outStream.writeUTF(p);
						break;
					case "v":
					case "view":
						System.out.println("Your requests are: ");
						outStream.writeUTF("Your requests are: ");
						for (Request request : app.viewRequests()) {
							outStream.writeUTF(request.toString() + "\n");
							System.out.println(request.toString() + "\n");
						}
						break;
					case "o":
					case "obtain":
						double code = Double.parseDouble(data[1]);
						String o = "Your code has ID: " + code;
						System.out.println(o);
						outStream.writeUTF(o);
						app.obtainQRcode(code);
						break;
					case "c":
					case "confirm":
						System.out.println(app.database.getQRCodeByID(Integer.parseInt(data[1])));
						app.confirmQRcode(app.database.getQRCodeByID(Integer.parseInt(data[1])));
						break;
					case "n":
					case "newgroup":
						int groupID = Integer.parseInt(data[1]);
						app.newGroup(groupID);
						System.out.println("New group created with ID: " + groupID);
						outStream.writeUTF("New group created with ID: " + groupID);
						break;
					case "a":
					case "addu":
						int groupToAddUser = Integer.parseInt(data[1]);
						int userToAdd = Integer.parseInt(data[2]);
						app.addUserToGroup(groupToAddUser, userToAdd);
						System.out.println("Added user: " + userToAdd + " to group" + groupToAddUser);
						outStream.writeUTF("Added user: " + userToAdd + " to group" + groupToAddUser);
						break;
					case "g":
					case "groups":
						HashSet<Group>[] groups = app.viewGroups();
						outStream.writeUTF("User owns the following groups: ");
						for (Group group : groups[0]) {
							System.out.println(group.toString());
							outStream.writeUTF(group.toString());
						}
						outStream.writeUTF("User is in the following groups: ");
						for (Group group : groups[1]) {
							System.out.println(group.toString());
							outStream.writeUTF(group.toString());
						}
						break;
					case "s":
					case "status":
						int groupToCheck = Integer.parseInt(data[1]);
						System.out.println("The following requests for the group" + groupToCheck + " are still to be paid: ");
						outStream.writeUTF(
								"The following requests for the group" + groupToCheck + " are still to be paid: ");
						System.out.println(app.statusPayments(groupToCheck));
						outStream.writeUTF(app.statusPayments(groupToCheck));
						break;
					case "h":
					case "history":
						System.out.println(app.viewHistory(Integer.parseInt(data[1])));
						outStream.writeUTF(app.viewHistory(Integer.parseInt(data[1])));
						break;
					case "pay":
					case "payrequest":
						int requestId = Integer.parseInt(data[1]);
						System.out.println("The request with ID: " + requestId + " will be paid");
						outStream.writeUTF("The request with ID: " + requestId + " will be paid");
						app.payRequest(requestId);
						break;
					default:
						outStream.writeUTF("Invalid request!");
					}
				}

				outStream.close();
				inStream.close();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (UserNotFoundException e) {
				try {
					outStream.writeUTF("Utilizador  nao existente!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (InsuficientFundsException e) {
				try {
					outStream.writeUTF("Saldo Insuficiente na conta !");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (NumberFormatException | WriterException e) {
				e.printStackTrace();
			} catch (GroupAleadyExistsException e) {
				try {
					outStream.writeUTF("Grupo ja existe!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (GroupNotFoundException e) {
				try {
					outStream.writeUTF("Grupo nao encontrado");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (UserNotOwnerException e) {
				try {
					outStream.writeUTF("Utilizador nao e dono do grupo!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (UserAlreadyInGroupException e) {
				try {
					outStream.writeUTF("Utilizador ja pertence ao grupo!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (RequestNotFoundException e) {
				try {
					outStream.writeUTF("Pedido nao encontrado!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (UserNotRequesteeException e) {
				try {
					outStream.writeUTF("identificador referente a um pagamento pedido a outro cliente!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (QRCodeNotFoundException e) {
				try {
					outStream.writeUTF("QRCode nao existente!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}

	private boolean authenticateUser(int user, String passwd) {
		try {
			File myObj = new File("../auth.txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				int data1 = myReader.nextInt();
				String[] data = myReader.nextLine().split(".");
				if (data1 == user && data[1] == passwd) {
					myReader.close();
					return true;
				}
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return false;
	}
}
