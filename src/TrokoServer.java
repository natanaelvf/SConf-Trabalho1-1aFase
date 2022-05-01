import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.google.zxing.WriterException;

import exceptions.GroupAleadyExistsException;
import exceptions.GroupNotFoundException;
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


	private Application app;
	private Database database;

	private static Key serverPublicKey;

	private static Cipher ciRSA;
	private static Cipher ciAES;
	private static KeyStore ks;
	private static final String SERVER_RSA = "serverRSA";
	private static final String RSAPASS = "123.Asp1rin2.";

	public static void main(String[] args) throws IOException {
		if (args.length != 4) {

			System.out.println("Usage format: TrokoServer <port> <keystore> <keystore-password>");
			System.exit(-1);
		} 
		int port = Integer.parseInt(args[1]);
		System.out.println("Starting server on port: " + port);
		TrokoServer server = new TrokoServer();

		init(args[2],args[3]);

		server.startServer(port);
	}

	private static void init (String keyStore, String keyStorePassword) {
		try {
			ks = KeyStore.getInstance("JCEKS");
			FileInputStream fis = new FileInputStream(keyStore);
			ks.load(fis, keyStorePassword.toCharArray());
			Certificate cert = ks.getCertificate(SERVER_RSA);
			ciRSA = Cipher.getInstance("RSA");

			serverPublicKey = cert.getPublicKey();
		} catch (KeyStoreException e) {
			System.out.println("Error getting KeyStore instance.");
			System.exit(-1);
		} catch (CertificateException e) {
			System.out.println("Error loading KeyStore. Wrong Password?");
			System.exit(-1);
		} catch (NoSuchPaddingException e) {
			System.out.println("Error with Cipher.");
			System.exit(-1);
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
			System.exit(-1);
		} catch (IOException e) {
			System.out.println(e + ": File not found");
			System.exit(-1);
		}
	}


	public void startServer(int port) throws IOException {
		try (ServerSocket sSoc = new ServerSocket(port)){

			app = new Application();
			app.setDatabase(new Database());
			database = new Database();
			database.setKey(serverPublicKey);

			app.getDatabase().getUsersFromDB();
			app.getDatabase().getGroupsFromDB();
			app.getDatabase().getGroupRequestsFromDB();
			app.getDatabase().getGroupRequestHistoryFromDB();

			while (true) {
				try {
					Socket inSoc = sSoc.accept();
					ServerThread newServerThread = new ServerThread(inSoc);
					newServerThread.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	private class ServerThread extends Thread {

		private Socket socket = null;
		ObjectOutputStream outStream;
		ObjectInputStream inStream;

		Random random = new Random();

		ServerThread(Socket inSoc) {
			socket = inSoc;
		}

		@Override
		public void run() {

			int userID = 0;
			User user;
			String userName = "";

			long nonce;
			boolean flag;

			Key pk;
			try {

				pk = ks.getKey(SERVER_RSA, RSAPASS.toCharArray());
				database.setKey(pk);

			} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {

				System.out.println("Error getting Server Private Key");
				System.exit(-1);
			}

			try {

				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());

				try {
					userName = (String)inStream.readObject();	
					userID = Integer.parseInt(userName); //Receber 1
					user= database.getUserByID(userID);

				} catch (IOException e) {
					System.out.println("Error recieving Client ID.");

					outStream.close();
					inStream.close();
					socket.close();
					return;
				}

				byte[] signature;
				long userNonce;
				Certificate userCert;
				nonce = random.nextLong();

				if (!userExists(userID)) {	//Registar utilizador

					System.out.println("Creating new User.");

					flag = false;
					outStream.writeObject(nonce); 	//Enviar 1
					outStream.writeObject(flag);	//Enviar 2
					userName = (String)inStream.readObject();		//Receber 2

					try {

						userNonce = (long) inStream.readObject();    	//Receber 3
						signature = (byte[]) inStream.readObject();		//Receber 4
						userCert = (Certificate) inStream.readObject();	//Receber 5

						PublicKey pubKey = userCert.getPublicKey();
						Signature sig = Signature.getInstance("SHA256withRSA");       	//assinatura
						sig.initVerify(pubKey);           							//inicializa a assinatura
						sig.update(bytefy(userNonce));   							//faz o update dos dados a ser assinados

						if (nonce == userNonce && sig.verify(signature)) {       	//se a assinatura for valida
							database.addUser(user);
							System.out.println("User successfully Created.");
							outStream.writeObject(true);	//Enviar 3
						} else {

							System.out.println("Error creating User.");
							outStream.writeObject(false);	//Enviar 3
							return;
						}
					} catch (IOException | InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {

						System.out.println("Error recieving Authentication Information.");
						System.exit(-1);
					}

					authenticatedRun(userID);

				} else { 														//Utilizador Registado

					System.out.println("Authenticating User.");

					flag = true;
					outStream.writeObject(nonce); 								//Enviar 1
					outStream.writeObject(flag);								//Enviar 2

					signature = (byte[]) inStream.readObject(); 				//Receber 2
					userCert = getUserCertificate(userName);

					if(userCert != null) {
						PublicKey pubKey = userCert.getPublicKey();
						Signature sig = Signature.getInstance("SHA256withRSA");       	//Assinatura
						sig.initVerify(pubKey);           							//Inicializar Assinatura
						sig.update(bytefy(nonce));   								//Update Dados Para Assinar

						if (sig.verify(signature)) {       							//Assinatura Valida

							System.out.println("User Authenticated.");
							outStream.writeObject(true);							//Enviar 3
							authenticatedRun(userID);
						} else {

							outStream.writeObject(false);							//Enviar 3
							System.out.println("Error authenticating User.");
						}
					}
				}

				outStream.close();
				inStream.close();
				socket.close();

			} catch (IOException | ClassNotFoundException | SignatureException | NoSuchAlgorithmException | InvalidKeyException | CertificateException e) {
				System.out.println("Error registering/authenticating User.");
			}
		}

		private byte[] bytefy(long nonce) {
			return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(nonce).array();
		}

		private Certificate getUserCertificate(String name) throws CertificateException, IOException {
			String getCert = "PubKeys\\" + name + "RSApub.cer";
			CertificateFactory fact;
			try {
				fact = CertificateFactory.getInstance("X509");

				try (FileInputStream fis  = new FileInputStream(getCert);){

					Certificate cer = fact.generateCertificate(fis);
					if (cer == null) {

						System.out.println("Error getting Certificate");
					}
					return cer;
				} catch (CertificateException e) {

					System.out.println("Error generating Certificate.");
				}
			} catch (FileNotFoundException e) {
				System.out.println("Error finding Certificate.");
			}
			System.out.println("Error getting Certificate. Returning null");
			return null;
		}

		public void authenticatedRun(int id) {
			int userId = id;
			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());
				User loggedUser = app.getDatabase().getUserByID(userId);
				app.setLoggedUser(loggedUser);

				String input = (String) inStream.readObject();

				try {
					while (input.equals("quit") || input.equals("q")) {
						System.out.println(input);
						String[] data = input.split(" ");
						double amount;
						switch (data[0]) {
						case "b", "balance":
							String b = "Your balance is: " + app.viewBalance();
						outStream.writeObject(b);
						System.out.println(b);
						break;
						case "m", "makepayment":
							int userToPay = Integer.parseInt(data[1]);
						amount = Double.parseDouble(data[2]);
						app.makePayment(userToPay, amount);
						String p = "Paid " + amount + " to user" + loggedUser.getID() + "\n";
						outStream.writeObject(p);
						break;
						case "r", "requestpayment" :
							int requestee = Integer.parseInt(data[1]);
						amount = Double.parseDouble(data[2]);
						app.requestPayment(requestee, amount, app.getLoggedUser().getID());
						break;
						case "v", "view":
							Set<Request> requests = app.viewRequests();
						System.out.println("Your requests are: ");
						outStream.writeObject("Your requests are: ");
						for (Request request : requests) {
							outStream.writeObject(request.toString() + "\n");
							System.out.println(request.toString() + "\n");
						}
						break;
						case "o", "obtain":
							double code = Double.parseDouble(data[1]);
						String o = "Your code has ID: " + code;
						System.out.println(o);
						outStream.writeObject(o);
						app.obtainQRcode(code, loggedUser.getID());
						break;
						case "c", "confirm":
							System.out.println(app.getDatabase().getQRCodeByID(Integer.parseInt(data[1])));
						app.confirmQRcode(app.getDatabase().getQRCodeByID(Integer.parseInt(data[1])));
						break;
						case "n", "newgroup":
							int groupID = Integer.parseInt(data[1]);
						app.newGroup(groupID);
						System.out.println("New group created with ID: " + groupID);
						outStream.writeObject("New group created with ID: " + groupID);
						break;
						case "a", "addu":
							int groupToAddUser = Integer.parseInt(data[1]);
						int userToAdd = Integer.parseInt(data[2]);
						app.addUserToGroup(groupToAddUser, userToAdd);
						System.out.println("Added user: " + userToAdd + " to group" + groupToAddUser);
						outStream.writeObject("Added user: " + userToAdd + " to group" + groupToAddUser);
						break;
						case "g", "groups":
							List<Set<Group>> groups = app.viewGroups();
						outStream.writeObject("User owns the following groups: ");
						for (Group group : groups.get(0)) {
							System.out.println(group.toString());
							outStream.writeObject(group.toString());
						}
						outStream.writeObject("User is in the following groups: ");
						for (Group group :  groups.get(1)) {
							System.out.println(group.toString());
							outStream.writeObject(group.toString());
						}
						break;
						case "s", "status":
							int groupToCheck = Integer.parseInt(data[1]);
						System.out.println(
								"The following requests for the group" + groupToCheck + " are still to be paid: ");
						outStream.writeObject(
								"The following requests for the group" + groupToCheck + " are still to be paid: ");
						System.out.println(app.statusPayments(groupToCheck));
						outStream.writeObject(app.statusPayments(groupToCheck));
						break;
						case "h", "history":
							System.out.println(app.viewHistory(Integer.parseInt(data[1])));
						outStream.writeObject(app.viewHistory(Integer.parseInt(data[1])));
						break;
						case "pay", "payrequest":
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

				} catch (IOException | NumberFormatException | WriterException e) {
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

		private boolean userExists(int userID) {
			User u = database.getUserByID(userID);
			return u != null && u.getID() == userID;
		}
	}
	public Application getApplication() {
		return this.app;
	}

	public Database getDatabase() {
		return this.database;
	}
}

