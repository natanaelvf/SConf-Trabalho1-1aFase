import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

import exceptions.IllegalArgumentNumberException;
import exceptions.InvalidPasswordException;
import exceptions.InvalidUserIdException;
import objects.Request;
import objects.User;

public class Trokos {

	 static DataInputStream in;
	 static DataOutputStream out;
	 static ServerSocket clientSocket;
	 static Application app = TrokoServer.app;

	public static void main(String[] args) throws IllegalArgumentNumberException, NumberFormatException, InvalidUserIdException, InvalidPasswordException, IOException, ClassNotFoundException {

		if (args.length > 3) {
			throw new IllegalArgumentNumberException("Demasiados args passados ao cliente!");
		}
		String serverAdress = args[0];
		int userId = Integer.parseInt(args[1]);
		String password = args[2];

		if (userId < 100000000 || userId > 999999999) {
			throw new InvalidUserIdException("Numero de cliente invalido!");
		}

		if (!isValidPassowrd(password)) {
			throw new InvalidPasswordException("Uma password deve ter entre 6 e 20 characteres,"
					+ " dos quais: dois digitos, duas letras maiusculas!");
		}
		
		User user = TrokoServer.app.database.getUserByID(userId);
		
		if (user != null) {
			app.setLoggedUser(user);
		} else {
			User newUser = new User(app.database.getUniqueQRCodeID(), 100.00, new HashSet<Request>());
			app.setLoggedUser(newUser);
		}

		clientSocket =new ServerSocket(9090, 0, InetAddress.getByName("localhost"));
		Socket socket = clientSocket.accept();
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());

		out.writeInt(userId);
		out.writeUTF(password);

		Trokos client = new Trokos();
		client.startClient(serverAdress);
	}


	private void startClient(String serverAdress) throws IOException, ClassNotFoundException {


		Scanner sc = new Scanner(System.in);
		String userInput = sc.nextLine();

		while (userInput != "quit" || userInput != "q") {
			out.writeUTF("imprima o que desejar");
			out.writeUTF(userInput);
			String fromServer = (String) in.readUTF();
			System.out.println(fromServer);
			userInput = sc.nextLine();
		}
		sc.close();

		out.close();
		in.close();

		clientSocket.close();
	}


	public static boolean isValidPassowrd(String password) {

        if (password.length() > 16 || password.length() < 6) return false;

        int charCount = 0;
        int numCount = 0;
        for (int i = 0; i < password.length(); i++) {

            char ch = password.charAt(i);

            if (ch >= '0' && ch <= '9')  numCount++;
            else if (Character.toUpperCase(ch) >= 'A' && Character.toUpperCase(ch) <= 'Z') charCount++;
            else return false;
        }


        return (charCount >= 2 && numCount >= 2);
    }
}
