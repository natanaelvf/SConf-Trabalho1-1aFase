import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import objects.Database;
import objects.User;

public class Trokos {

	static Application app = TrokoServer.app;
	static ObjectOutputStream outStream;
	static ObjectInputStream  inStream;
	static Socket clientSocket;

	private static String keyStore;
	private static String keyStorePass;
	private static Cipher ciRSA;
	private static Cipher ciAES;
	private static Certificate cert;
	private static KeyStore ks;
	private static String userID;

	private static PrivateKey userPK;
	private static Database database;
	private static final String UNICODE_FORMAT = "UTF-8";

	public static void main(String[] args) throws NumberFormatException,
	IOException, ClassNotFoundException {

		if (args.length != 5) {    //Se nao tiver os argumentos obrigatorios

			System.out.println("Usage format: Trokos <serverAddress> <truststore> <keystore> <keystore-password> <clientID>");
			System.exit(-1);
		} 
		Scanner sc= new Scanner(System.in);
		String[] serverAddress = args[0].split(":");
		int portServer = Integer.parseInt(serverAddress[1]); 				//Port do servidor
		try {

			//SocketFactory socFac = SSLSocketFactory.getDefault(); //SSL
			//SSLSocket clientSocket = (SSLSocket) socFac.createSocket(serverAddress[0], portServer); //Socket a conectar  //SSL
			Socket clientSocket = new Socket(serverAddress[0], portServer);
			//clientSocket.startHandshake();	//SSL
			System.out.println("Connected to server.");

			inStream = new ObjectInputStream(clientSocket.getInputStream());	//Input
			outStream = new ObjectOutputStream(clientSocket.getOutputStream());	//Output
			//Scanner

			init(args[2], args[3], args[4]);

			outStream.writeObject(userID);     			//Enviar 1
			System.out.println("Id sent.");

			long nonce = (long) inStream.readObject();    			//Receber 1
			System.out.println("Nonce recieved.");
			boolean flag = (boolean) inStream.readObject();			//Receber 2
			System.out.println("Flag recieved.");

			if (!flag) { 											//Utilizador por registar

				System.out.println("Registering User.");
				System.out.print("Insert Name:");
				String userName = sc.nextLine(); 					//Nome do utilizador
				outStream.writeObject(userName);											//Enviar 2

				outStream.writeObject(nonce);												//Enviar 3
				outStream.writeObject(signNonce(userID, nonce, keyStore, keyStorePass));	//Enviar 4
				outStream.writeObject(getUserCertificate(userID));						    //Enviar 5

				boolean aut = (boolean) inStream.readObject();	//Receber 3
				if(!aut) {

					System.out.println("Error creating user."); 
					sc.close();
					clientSocket.close();
					System.exit(-1);
				}
				System.out.println("User created and authenticated.");

			} else {									//Utilizador ja esta registado

				outStream.writeObject(signNonce(userID, nonce, keyStore, keyStorePass));	//Enviar 2	
				System.out.println("Signed Nonce sent.");

				boolean aut = (boolean) inStream.readObject();	//Receber 3
				if(!aut) {

					System.out.println("Error athenticating user.");
					sc.close();
					clientSocket.close();
					System.exit(-1);
				}
			}
			String answer = (String)inStream.readObject(); //
			System.out.println(answer);

			User loggedUser = app.database.getUserByID(Integer.parseInt(userID));

			app.setLoggedUser(loggedUser);


			Trokos client = new Trokos();
			client.startClient(serverAddress[1]);

		} catch (IOException e) {

			System.out.println("Error: Connection closed.");
			System.exit(-1);			
		}
		sc.close();
		clientSocket.close();
	}
	private static byte[] signNonce(String id, long nonce, String ks, String ksPass) throws IOException {
		Scanner sc= new Scanner(System.in);
		try (FileInputStream kfile = new FileInputStream(ks))
		{
			KeyStore kstore = KeyStore.getInstance("JCEKS");
			kstore.load(kfile, ksPass.toCharArray());

			System.out.print("Insert private key password:");
			userPK = (PrivateKey)kstore.getKey(id+"RSA", sc.nextLine().toCharArray());

			Signature signature = Signature.getInstance("MD5withRSA");
			signature.initSign(userPK);
			byte[] buf = bytefy(nonce);
			signature.update(buf);

			return signature.sign();
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException | UnrecoverableKeyException | InvalidKeyException | SignatureException e) {

			e.printStackTrace();
		} finally {
			sc.close();
		}
		sc.close();
		return new byte[0];
	}
	private static byte[] bytefy(long nonce) {
		return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(nonce).array();
	}


	private static void init(String keystore, String keystorePassword, String id) {

		try {

			ciRSA = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {

			System.out.println("Error with RSA Cipher.");
			System.exit(-1);
		}

		try {

			ciAES = Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {

			System.out.println("Error with RSA Cipher.");
			System.exit(-1);
		}

		userID = id;
		keyStore = keystore;
		keyStorePass = keystorePassword;

		try {

			ks = KeyStore.getInstance("JCEKS");
			try (FileInputStream fis = new FileInputStream(keyStore)) {

				ks.load(fis, keyStorePass.toCharArray());
				cert = ks.getCertificate(userID + "RSA");
			} catch (IOException | NoSuchAlgorithmException | CertificateException e) {

				System.out.println("Error loading KeyStore. Wrong Password?");
				System.exit(-1);
			}
		} catch (KeyStoreException e) {

			System.out.println("Error getting KeyStore instance.");
			System.exit(-1);
		}
	}


	private void startClient(String serverAdress) throws IOException, ClassNotFoundException {
		Scanner sc = new Scanner(System.in);
		printHelp();
		String userInput = sc.nextLine();

		while (userInput.equals("quit")) {
			if (userInput.equals("help")) {
				outStream.writeObject(userInput);
				String fromServer = (String) inStream.readObject();
				System.out.println(fromServer);
			} else {
				printHelp();
			}
			userInput = sc.nextLine();
		}
		System.out.println("Thank you for playing!");
		sc.close();
	}

	private void printHelp() {
		System.out.println("(b)alance - obtem valor atual do saldo da sua conta");
		System.out.println("(m)akepayment <userID> <amount> - transfere o valor amount da sua conta de clientID para a"
				+ "conta de userID");
		System.out.println("(r)equestpayment <userID> <amount> - envia um pedido de pagamento ao utilizador"
				+ "userID, de valor amount");
		System.out.println("(v)iewrequests - obtem do servidor a sua lista de pedidos de pagamentos pendentes");
		System.out.println("(p)ayrequest <reqID> - autoriza o pagamento do pedido com identificador reqID, "
				+ " removendo o pedido da lista de pagamentos pendentes");
		System.out.println("(o)btainQRcode <amount> - cria um pedido de pagamento no servidor e coloca-o numa"
				+ " lista de pagamentos identificados por QR code");
		System.out.println("(c)onfirmQRcode <QRcode> - confirma e autoriza o pagamento identificado por QR code, "
				+ "removendo o pedido da lista mantida pelo servidor");
		System.out.println("(n)ewgroup <groupID> - cria um grupo para pagamentos partilhados, cujo dono (owner)"
				+ "sera o cliente que o criou");
		System.out.println("(a)ddu <userID> <groupID> - adiciona o utilizador userID como membro do grupo indicado.");
		System.out.println("(g)roups - mostra uma lista dos grupos de que o cliente eh dono, e uma lista dos grupos a "
				+ "que pertence");
		System.out.println("(d)ividepayment <groupID> <amount> - cria um pedido de pagamento de grupo, cujo valor "
				+ " amount deve ser dividido pelos membros do grupo groupID");
		System.out.println("(s)tatuspayments <groupID> - mostra o estado de cada pedido de pagamento de grupo, ou"
				+ "seja, que membros de grupo ainda nao pagaram esse pedido");
		System.out.println("(h)istory <groupID> - mostra o historico dos pagamentos do grupo groupID ja concluidos");
		System.out.println("Insira um comando!");
	}



	private static Certificate getUserCertificate(String name) {

		String getCert = "PubKeys\\" + name + "RSApub.cer";

		CertificateFactory fact;
		try {

			fact = CertificateFactory.getInstance("X509");

			FileInputStream fis;
			try {
				fis = new FileInputStream(getCert);
				try {
					return fact.generateCertificate(fis);
				} catch (CertificateException e) {
					System.out.println("Error generating Certificate.");
				}
			} catch (FileNotFoundException e) {

				System.out.println("Error finding Certificate.");
			}
		} catch (CertificateException e1) {

			System.out.println("Error getting Certificate.");
		}
		System.out.println("Error getting Certificate. Returning null");
		return null;
	}

	public static Key unwrapKey(byte[] symKey) throws InvalidKeyException {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.UNWRAP_MODE, userPK);
			return cipher.unwrap(symKey, "RSA", Cipher.SECRET_KEY);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {

			System.out.println("Error decrypting Group Key. Returning null.");
			return null;
		}
	}

	public static byte[] wrapKey(PublicKey pubKey, SecretKey symKey) throws InvalidKeyException, IllegalBlockSizeException {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.WRAP_MODE, pubKey);
			return cipher.wrap(symKey);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.out.println("Error encrypting Group Key. Returning null.");
			return new byte[0];
		}
	}

	public static String decryptMsg(byte[] dataToDecrypt, Key myKey) {

		try {

			ciAES.init(Cipher.DECRYPT_MODE, myKey);
			byte[] textDecrypted = ciAES.doFinal(dataToDecrypt);
			return new String(textDecrypted);

		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}

	public static byte[] encryptMsg(String dataToEncrypt, Key myKey) {

		try {
			byte[] text = dataToEncrypt.getBytes(UNICODE_FORMAT);
			ciAES.init(Cipher.ENCRYPT_MODE, myKey);
			return ciAES.doFinal(text);
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
}
