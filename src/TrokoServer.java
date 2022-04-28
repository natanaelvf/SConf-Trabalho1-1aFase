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
	public static Database database = new Database();

	public static void main(String[] args) throws IOException, IllegalArgumentNumberException {

		if (args.length > 1) {
			throw new IllegalArgumentNumberException("Demasiados args passados ao servidor!");
		}
		int serverPort = 45678;
		if (args[0] != null) {
			serverPort = Integer.parseInt(args[0]);
		}
		app = new Application();

		database.getUsersFromDB();

		database.getGroupsFromDB();

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
		ObjectOutputStream outStream;
		ObjectInputStream inStream;

		ServerThread(Socket inSoc) {
			socket = inSoc;
		}

		public void run() {
			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());

				int userId = 0;
				String passwd = null;
				String user = (String) inStream.readObject();
				userId = Integer.parseInt(user); // 100000000
				passwd = (String) inStream.readObject();

				System.out.println(user + " " + passwd);
				if (authenticateUser(user, passwd)) {
					outStream.writeObject("LOGGED");
				}

				User loggedUser = app.database.getUserByID(userId);

				app.setLoggedUser(loggedUser);

				String input = (String) inStream.readObject();

				try {
					while (input != "quit" || input != "q") {
						System.out.println(input);
						String[] data = input.split(" ");
						double amount;
						switch (data[0]) {
						case "b":
						case "balance":
							String b = "Your balance is: " + app.viewBalance();
							outStream.writeObject(b);
							System.out.println(b);
							break;
						case "m":
						case "makepayment":
							int userToPay = Integer.parseInt(data[1]);
							amount = Double.parseDouble(data[2]);
							app.makePayment(userToPay, amount);
							String p = "Paid " + amount + " to user" + user + "\n";
							outStream.writeObject(p);
							break;
						case "r":
						case "requestpayment" :
							int requestee = Integer.parseInt(data[1]);
							amount = Double.parseDouble(data[2]);
							app.requestPayment(requestee, amount, app.getLoggedUser().getID());
						case "v":
						case "view":
							System.out.println("Your requests are: ");
							outStream.writeObject("Your requests are: ");
							for (Request request : app.viewRequests()) {
								outStream.writeObject(request.toString() + "\n");
								System.out.println(request.toString() + "\n");
							}
							break;
						case "o":
						case "obtain":
							double code = Double.parseDouble(data[1]);
							String o = "Your code has ID: " + code;
							System.out.println(o);
							outStream.writeObject(o);
							app.obtainQRcode(code, loggedUser.getID());
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
							outStream.writeObject("New group created with ID: " + groupID);
							break;
						case "a":
						case "addu":
							int groupToAddUser = Integer.parseInt(data[1]);
							int userToAdd = Integer.parseInt(data[2]);
							app.addUserToGroup(groupToAddUser, userToAdd);
							System.out.println("Added user: " + userToAdd + " to group" + groupToAddUser);
							outStream.writeObject("Added user: " + userToAdd + " to group" + groupToAddUser);
							break;
						case "g":
						case "groups":
							HashSet<Group>[] groups = app.viewGroups();
							outStream.writeObject("User owns the following groups: ");
							for (Group group : groups[0]) {
								System.out.println(group.toString());
								outStream.writeObject(group.toString());
							}
							outStream.writeObject("User is in the following groups: ");
							for (Group group : groups[1]) {
								System.out.println(group.toString());
								outStream.writeObject(group.toString());
							}
							break;
						case "s":
						case "status":
							int groupToCheck = Integer.parseInt(data[1]);
							System.out.println(
									"The following requests for the group" + groupToCheck + " are still to be paid: ");
							outStream.writeObject(
									"The following requests for the group" + groupToCheck + " are still to be paid: ");
							System.out.println(app.statusPayments(groupToCheck));
							outStream.writeObject(app.statusPayments(groupToCheck));
							break;
						case "h":
						case "history":
							System.out.println(app.viewHistory(Integer.parseInt(data[1])));
							outStream.writeObject(app.viewHistory(Integer.parseInt(data[1])));
							break;
						case "pay":
						case "payrequest":
							int requestId = Integer.parseInt(data[1]);
							System.out.println("The request with ID: " + requestId + " will be paid");
							outStream.writeObject("The request with ID: " + requestId + " will be paid");
							app.payRequest(requestId);
							break;
						default:
							outStream.writeObject("Invalid command!");
						}
						input = (String) inStream.readObject();
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (UserNotFoundException e) {
					try {
						outStream.writeObject("Utilizador nao existente!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (InsuficientFundsException e) {
					try {
						outStream.writeObject("Saldo Insuficiente na conta !");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (NumberFormatException | WriterException e) {
					e.printStackTrace();
				} catch (GroupAleadyExistsException e) {
					try {
						outStream.writeObject("Grupo ja existe!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (GroupNotFoundException e) {
					try {
						outStream.writeObject("Grupo nao encontrado");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (UserNotOwnerException e) {
					try {
						outStream.writeObject("Utilizador nao e dono do grupo!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (UserAlreadyInGroupException e) {
					try {
						outStream.writeObject("Utilizador ja pertence ao grupo!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (RequestNotFoundException e) {
					try {
						outStream.writeObject("Pedido nao encontrado!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (UserNotRequesteeException e) {
					try {
						outStream.writeObject("identificador referente a um pagamento pedido a outro cliente!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (QRCodeNotFoundException e) {
					try {
						outStream.writeObject("QRCode nao existente!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			} catch (IOException | ClassNotFoundException e2) {
				e2.printStackTrace();
			} finally {
				try {
					outStream.close();
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private boolean authenticateUser(String user, String passwd) {
			try {
				Scanner myReader = new Scanner(new File(".\\src\\bds\\auth.txt"));
				while (myReader.hasNextLine()) {
					String[] data = myReader.nextLine().split(":");
					if (data[0] == user && data[1] == passwd) {
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
}

