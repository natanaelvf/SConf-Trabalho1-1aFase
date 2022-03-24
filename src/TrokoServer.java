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

				String user = null;
				String passwd = null;

				try {
					user = (String)inStream.readObject();
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
					switch(input) {
					case "b": case "balace":
						
					}
				}

				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


	private boolean authenticateUser(String user, String passwd) {
		try {
			File myObj = new File("../auth.txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String[] data = myReader.nextLine().split(".");
				if (data[0] == user && data[1] == passwd) {
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
