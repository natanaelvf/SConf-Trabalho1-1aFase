import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

import exceptions.IllegalArgumentNumberException;
import exceptions.InvalidPasswordException;
import exceptions.InvalidUserIdException;
import objects.Request;
import objects.User;

public class Trokos {

	static Application app = TrokoServer.app;
	static ObjectOutputStream outStream;
	static ObjectInputStream  inStream;
	static Socket clientSocket;

	public static void main(String[] args) throws IllegalArgumentNumberException, NumberFormatException,
			InvalidUserIdException, InvalidPasswordException, IOException, ClassNotFoundException {

		if (args.length > 3) {
			throw new IllegalArgumentNumberException("Demasiados args passados ao cliente!");
		}
		String serverAdress = args[0].split(":")[0];
		int serverPort = Integer.parseInt(args[0].split(":")[1]);
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
		System.out.println("Trying to write to port: " + serverPort);
		clientSocket = new Socket(serverAdress, serverPort);
		outStream = new ObjectOutputStream(clientSocket.getOutputStream());
		inStream = new ObjectInputStream(clientSocket.getInputStream());
		
		System.out.println("Writing userId" + userId);
		outStream.writeUTF(userId+"");
		System.out.println("Writing password" + password);
		outStream.writeUTF(password);

		Trokos client = new Trokos();
		client.startClient(serverAdress);
	}

	private void startClient(String serverAdress) throws IOException, ClassNotFoundException {

		Scanner sc = new Scanner(System.in);
		printHelp();
		String userInput = sc.nextLine();


		while (userInput != "quit" || userInput != "q") {
			if (userInput != "help") {
				outStream.writeUTF(userInput);
				String fromServer = inStream.readUTF();
				System.out.println(fromServer);
			} else {
				printHelp();
			}
			userInput = sc.nextLine();
		}
		
		clientSocket.close();
		sc.close();
		outStream.close();
	}

	private void printHelp() {
		System.out.println("Insira um comando!");
		System.out.println("(b)alance – obtem valor atual do saldo da sua conta");
		System.out.println("(m)akepayment <userID> <amount> – transfere o valor amount da sua conta de clientID para a"
				+ "conta de userID");
		System.out.println("(r)equestpayment <userID> <amount> – envia um pedido de pagamento ao utilizador"
				+ "userID, de valor amount");
		System.out.println("(v)iewrequests – obtem do servidor a sua lista de pedidos de pagamentos pendentes");
		System.out.println("(p)ayrequest <reqID> – autoriza o pagamento do pedido com identificador reqID, "
				+ " removendo o pedido da lista de pagamentos pendentes");
		System.out.println("(o)btainQRcode <amount> – cria um pedido de pagamento no servidor e coloca-o numa"
				+ " lista de pagamentos identificados por QR code");
		System.out.println("(c)onfirmQRcode <QRcode> – confirma e autoriza o pagamento identificado por QR code, "
				+ "removendo o pedido da lista mantida pelo servidor");
		System.out.println("(n)ewgroup <groupID> – cria um grupo para pagamentos partilhados, cujo dono (owner)"
				+ "será o cliente que o criou");
		System.out.println("(a)ddu <userID> <groupID> – adiciona o utilizador userID como membro do grupo indicado.");
		System.out.println("(g)roups – mostra uma lista dos grupos de que o cliente é dono, e uma lista dos grupos a "
				+ "que pertence");
		System.out.println("(d)ividepayment <groupID> <amount> – cria um pedido de pagamento de grupo, cujo valor "
				+ "total amount deve ser dividido pelos membros do grupo groupID");
		System.out.println("(s)tatuspayments <groupID> – mostra o estado de cada pedido de pagamento de grupo, ou\r\n"
				+ "seja, que membros de grupo ainda não pagaram esse pedido");
		System.out.println("(h)istory <groupID> – mostra o histórico dos pagamentos do grupo groupID já concluídos");
	}

	public static boolean isValidPassowrd(String password) {

		if (password.length() > 16 || password.length() < 6)
			return false;

		int charCount = 0;
		int numCount = 0;
		for (int i = 0; i < password.length(); i++) {

			char ch = password.charAt(i);

			if (ch >= '0' && ch <= '9')
				numCount++;
			else if (Character.toUpperCase(ch) >= 'A' && Character.toUpperCase(ch) <= 'Z')
				charCount++;
			else
				return false;
		}

		return (charCount >= 2 && numCount >= 2);
	}
}
