import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import com.google.zxing.WriterException;

import exceptions.GroupAleadyExistsException;
import exceptions.GroupNotFoundException;
import exceptions.IllegalArgumentNumberException;
import exceptions.InexistentGroupException;
import exceptions.InsuficientFundsException;
import exceptions.QRCodeNotFoundException;
import exceptions.RequestNotFoundException;
import exceptions.UserAlreadyInGroupException;
import exceptions.UserNotFoundException;
import exceptions.UserNotOwnerException;
import exceptions.UserNotRequesteeException;
import objects.Database;
import objects.Group;
import objects.QRCode;
import objects.Request;
import objects.User;

public class TrokoServer {

	public static void main(String[] args) throws IOException, IllegalArgumentNumberException {
		if (args.length > 1) {
			throw new IllegalArgumentNumberException("Demasiados args passados ao servidor!");
		}
		int serverPort = Integer.parseInt(args[0]);
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
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
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
					outStream.writeObject(new Boolean(true));
				}
				
				String input = (String)inStream.readObject();
				
				while (input != "quit" || input != "q" ) {
					
					input = (String)inStream.readObject();
					String [] data= input.split(" ");
					switch(data[0]) {
					case "b": case "balance":
						Application.viewBalance();
						break;
					case "p": case "makepay":
						Application.makePayment(user, Double.parseDouble(data[1]));
						break;
					case "r": case "requestpayment":
						Application.requestPayment(user, Double.parseDouble(data[1]));
						break;
					case "v": case "view":
						Application.viewRequests();
						break;
					case "o": case "obtain":
						Application.obtainQRcode(Double.parseDouble(data[1]));
						break;
					case "c": case "confirm":
						Application.confirmQRcode(Application.database.getQRCodeByID(Integer.parseInt(data[1])));
						break;
					case "n": case "newgroup":
						Application.newGroup(Integer.parseInt(data[1]));
						break;
					case "a": case "addu":
						Application.addUserToGroup(user, Integer.parseInt(data[1]));
						break;
					case "g": case "groups":
						Application.viewGroups();
						break;
					case "s": case "status":
						Application.statusPayments(Integer.parseInt(data[1]));
						break;
					case "h": case "history":
						Application.viewHistory(Integer.parseInt(data[1]));
						break;
					case "pay" : case "payrequest":
						Application.payRequest(Integer.parseInt(data[1]));
						break;	
					}
				}

				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} catch (UserNotFoundException e) {
				System.out.println("Utilizador  não existente!");
				e.printStackTrace();
			} catch (InsuficientFundsException e) {
				System.out.println("Saldo Insuficiente na conta !");
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (WriterException e) {
				e.printStackTrace();
			} catch (GroupAleadyExistsException e) {
				System.out.println("Grupo já existe!");
				e.printStackTrace();
			} catch (GroupNotFoundException e) {
				System.out.println("Grupo não encontrado!");
				e.printStackTrace();
			} catch (UserNotOwnerException e) {
				System.out.println("Utilizador logado não é dono do grupo!");
				e.printStackTrace();
			} catch (UserAlreadyInGroupException e) {
				System.out.println("Utilizador já no grupo!");
				e.printStackTrace();
			} catch (RequestNotFoundException e) {
				System.out.println("Pedido não encontrado!");
				e.printStackTrace();
			} catch (UserNotRequesteeException e) {
				System.out.println("identificador referente a um pagamento pedido a outro cliente");
				e.printStackTrace();
			} catch (QRCodeNotFoundException e) {
				System.out.println("QRCode não existente");
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
