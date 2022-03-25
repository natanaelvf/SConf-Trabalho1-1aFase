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

	public static Application app;
	
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
        
        User user1 = new User(100000001, 1000.00, new HashSet<Request>());
        User user2 = new User(100000002, 2000.00, new HashSet<Request>());
        User user3 = new User(100000003, 3000.00, new HashSet<Request>());
        
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

        while(true) {
            try {
                Socket inSoc = sSoc.accept();
                ServerThread newServerThread = new ServerThread(inSoc);
                newServerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sSoc.close();
            }
        }
    }


	private class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
		}

		public void run() {
			ObjectOutputStream outStream =  null;
			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				int user = 0;
				String passwd = null;

				try {
					user = (int)inStream.readObject();
					passwd = (String)inStream.readObject();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				if (authenticateUser(user,passwd)){
					outStream.writeUTF("LOGGED");
				}
				//if (database.Users exists)
		        User user2 = new User(user, 2000.00, new HashSet<Request>());
				
				String input = (String)inStream.readObject();
				
				while (input != "quit" || input != "q" ) {
					
					input = (String)inStream.readObject();
					String [] data= input.split(" ");
					switch(data[0]) {
					case "b": case "balance":
						outStream.writeDouble(app.viewBalance());
						break;
					case "p": case "makepay":
						app.makePayment(user, Double.parseDouble(data[1]));
						break;
					case "r": case "requestpayment":
						app.requestPayment(user, Double.parseDouble(data[1]));
						break;
					case "v": case "view":
						app.viewRequests();
						break;
					case "o": case "obtain":
						app.obtainQRcode(Double.parseDouble(data[1]));
						break;
					case "c": case "confirm":
						app.confirmQRcode(app.database.getQRCodeByID(Integer.parseInt(data[1])));
						break;
					case "n": case "newgroup":
						app.newGroup(Integer.parseInt(data[1]));
						break;
					case "a": case "addu":
						app.addUserToGroup(user, Integer.parseInt(data[1]));
						break;
					case "g": case "groups":
						app.viewGroups();
						break;
					case "s": case "status":
						app.statusPayments(Integer.parseInt(data[1]));
						break;
					case "h": case "history":
						app.viewHistory(Integer.parseInt(data[1]));
						break;
					case "pay" : case "payrequest":
						app.payRequest(Integer.parseInt(data[1]));
						break;
					default :
						
					}
				}

				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException | ClassNotFoundException e) {
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
				if (data1==user && data[1] == passwd) {
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
