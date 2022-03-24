import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import exceptions.IllegalArgumentNumberException;
import exceptions.InvalidPasswordException;
import exceptions.InvalidUserIdException;
import objects.User;

public class Trokos {

	static ObjectInputStream in;
	static ObjectOutputStream out;
	static Socket clientSocket;

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
					+ " dos quais: um digito, uma letra maiuscula, uma minuscula e um caracter especial!");
		}
		
		User user = Application.database.getUserByID(userId);
		
		if (user != null) {
			Application.setLoggedUser(user);
		} else {
			User newUser = new User(Application.database.getUniqueQRCodeID(), userId, null);
		}

		clientSocket = new Socket(serverAdress, 45678);

		in = new ObjectInputStream(clientSocket.getInputStream());
		out = new ObjectOutputStream(clientSocket.getOutputStream());

		out.writeObject(userId);
		out.writeObject(password);

		Trokos client = new Trokos();
		client.startClient(serverAdress);
	}


	private void startClient(String serverAdress) throws IOException, ClassNotFoundException {


		Scanner sc = new Scanner(System.in);
		String userInput = sc.nextLine();

		while (userInput != "quit" || userInput != "q") {
			out.writeObject(userInput);
			String fromServer = (String) in.readObject();
			System.out.println(fromServer);
			userInput = sc.nextLine();
		}
		sc.close();

		out.close();
		in.close();

		clientSocket.close();
	}


	public static boolean isValidPassowrd(String password) {
		return password.matches("/^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?!.*\\s).{6,20}$/");
	}
}
