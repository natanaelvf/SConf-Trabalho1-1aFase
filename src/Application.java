import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.google.zxing.WriterException;

import exceptions.GroupAleadyExistsException;
import exceptions.GroupNotFoundException;
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

public class Application {

	public Database database;
	private User loggedUser;

	/**
	 * Obtem valor atual do saldo da sua conta.
	 * 
	 * @return balance o valor atual do saldo da conta
	 */
	public double viewBalance() {
		return this.loggedUser.getBalance();
	}

	/**
	 * Transfere o valor amount da conta de clientID para a conta de userID
	 * 
	 * @param userID conta para a qual enviar o amount
	 * @param amount valor a transferir
	 * 
	 * @throws InsuficientFundsException se o utilizador nao tiver dinheiro
	 *                                   suficiente na conta
	 * @throws UserNotFoundException     se o utilizador userID nao existir
	 * @throws FileNotFoundException 
	 */
	public void makePayment(int userID, double amount) throws UserNotFoundException, InsuficientFundsException, FileNotFoundException {
		if (amount > this.loggedUser.getBalance()) {
			throw new InsuficientFundsException(
					"Erro ao transferir: Saldo Insuficiente na conta " + this.loggedUser.getID() + "!");
		}
		User user = this.database.getUserByID(userID);
		if (user == null) {
			throw new UserNotFoundException("Erro ao transferir: Utilizador" + userID + " nao existente!");
		}
		transfer(this.loggedUser, user, amount);
	}

	private void transfer(User from, User to, double amount) throws FileNotFoundException {
		double fromNewBalance = from.getBalance() - amount;
		double toNewBalance = to.getBalance() + amount;

		Scanner sc = new Scanner(new File(".\\src\\bds\\users.txt"));
		PrintWriter printout = new PrintWriter(".\\src\\bds\\users.txt");
		StringBuilder sb = new StringBuilder();

		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] splitLine = line.split(":");

			int userId = Integer.parseInt(splitLine[0]);

			if(userId == from.getID()) {
				splitLine[1] = Double.toString(fromNewBalance);
				sb.append(String.join(":", splitLine) + "\r\n");
			} else if(userId == to.getID()) {
				splitLine[1] = Double.toString(toNewBalance);
				sb.append(String.join(":", splitLine) + "\r\n");
			} else {
				sb.append(line + "\r\n");
			}
		}
		
		printout.write(sb.toString());
		printout.close();
		from.setBalance(fromNewBalance);
		to.setBalance(toNewBalance);
		
		sc.close();
	}

	/**
	 * Envia um pedido de pagamento ao utilizador userID, de valor amount
	 * 
	 * @param userID o id a quem enviar o pedido
	 * @param amount o valor a pedir
	 * 
	 * @throws UserNotFoundException se o utilizador userID nao existir
	 * @throws IOException 
	 */
	public void requestPayment(int toID, double amount, int fromID) throws UserNotFoundException, IOException {
		User user = this.database.getUserByID(toID);
		if (user == null) {
			throw new UserNotFoundException(
					"Erro ao fazer um pedido de pagamento: Utilizador " + toID + " nao existente!");
		}
		Request request = new Request(this.database.getUniqueRequestID(), amount, fromID, toID);
		this.database.addRequest(request);
		user.addRequest(request);
	}

	/**
	 * Obtem do servidor lista de pedidos de pagamentos pendentes do utilizador.
	 * 
	 * @return a lista de pedidos de pagamentos pendentes
	 */

	public HashSet<Request> viewRequests() {
		return this.loggedUser.getRequests();
	}

	/**
	 * Autoriza o pagamento do pedido com identificador reqID, removendo o pedido da
	 * lista de pagamentos pendentes
	 * 
	 * @param requestID o ID do Request a pagar
	 * 
	 * @throws RequestNotFoundException  se o request com requestID nao existir
	 * @throws InsuficientFundsException o utilizador logado nao tem dinheiro
	 *                                   suficiente
	 * @throws UserNotRequesteeException o utilizador nao e a quem foi pedido o
	 *                                   pedido com id requestID
	 * @throws FileNotFoundException 
	 */
	public void payRequest(int requestID)
			throws RequestNotFoundException, InsuficientFundsException, UserNotRequesteeException, FileNotFoundException {
		Request request = this.database.getRequestByID(requestID);
		if (request == null) {
			throw new RequestNotFoundException(
					"Erro ao autorizar o pagamento : Pedido " + requestID + " nao existente!");
		}
		if (request.getAmount() > loggedUser.getBalance()) {
			throw new InsuficientFundsException(
					"Erro ao autorizar o pagamento: Saldo Insuficiente na conta " + loggedUser.getID() + "!");
		}
		if (request.getFromID() != loggedUser.getID()) {
			throw new UserNotRequesteeException(
					"Erro ao autorizar o pagamento: identificador referente" + "a um pagamento pedido a outro cliente");
		}
		User from = this.database.getUserByID(request.getFromID());
		double fromNewBalance = from.getBalance() - request.getAmount();
		from.setBalance(fromNewBalance);
		
		User to = this.database.getUserByID(request.getToID());
		double toNewBalance = to.getBalance() + request.getAmount();
		to.setBalance(toNewBalance);
		
		this.database.removeRequest(request);
		this.loggedUser.removeRequest(request);
	}

	/**
	 * Cria um pedido de pagamento no servidor e coloca o numa lista de pagamentos
	 * identificados por QR code.
	 * 
	 * @param amount o amount a pagar
	 * 
	 * @throws IOException
	 * @throws WriterException
	 */

	public void obtainQRcode(double amount, int toId) throws WriterException, IOException {
		QRCode qrCode = new QRCode(this.database.getUniqueQRCodeID(), amount, this.getLoggedUser().getID());
		Request request = new Request(this.database.getUniqueRequestID(), amount, this.getLoggedUser().getID(), toId);
		request.setQRCode(qrCode);
		this.loggedUser.addRequest(request);
		int id = qrCode.getID();
		String str = "" + id;
		String path = ".\\src\\qrcodes\\" + qrCode.getID() + ".png";
		String charset = "UTF-8";
		QRCode.generateQRcode(str, Paths.get(path), charset, 200, 200);
		System.out.println("QR Code created successfully.");
	}

	/**
	 * Confirma e autoriza o pagamento identificado por QR code, removendo o pedido
	 * da lista mantida pelo servidor. Se o cliente nao tiver saldo suficiente na
	 * conta, deve ser retornado um erro (mas o pedido continua a ser removido da
	 * lista). Se o pedido identificado por QR code nao existir tambem deve retornar
	 * um erro. "
	 * 
	 * @throws UserNotRequesteeException
	 * @throws RequestNotFoundException
	 * @throws FileNotFoundException 
	 */
	public void confirmQRcode(QRCode qrCode) throws InsuficientFundsException, QRCodeNotFoundException,
	RequestNotFoundException, UserNotRequesteeException, FileNotFoundException {
		this.database.getQRCodeByID(qrCode.getID());
		HashSet<Request> requests = loggedUser.getRequests();
		for (Request request : requests) {
			if (request.getQRCode().getID() == qrCode.getID()) {
				int userID = request.getToID();
				User user = database.getUserByID(userID);
				if (user.getBalance() < qrCode.getAmount()) {
					throw new InsuficientFundsException(
							"O utilizador n�o tem saldo suficiente para esta transi��o");
				}
				payRequest(request.getToID());
			}
		}
		throw new QRCodeNotFoundException("QRCode n�o existente");
	}

	/**
	 * Cria um grupo para pagamentos partilhados, cujo dono (owner) e o cliente que
	 * o criou
	 * 
	 * @param groupID o id do grupo a criar
	 * @throws GroupAleadyExistsException se o grupo com id groupID ja existir
	 * @throws FileNotFoundException 
	 */
	public void newGroup(int groupID) throws GroupAleadyExistsException, FileNotFoundException {
		if (this.database.getGroupByID(groupID) != null) {
			throw new GroupAleadyExistsException(
					"Erro ao criar o grupo com id " + groupID + " : um grupo com esse id ja existe!");
		}
		Group group = new Group(groupID, this.loggedUser.getID(), new HashSet<Integer>());
		this.database.addGroup(group);
	}

	/**
	 * Adiciona o utilizador userID como membro do grupo indicado
	 * 
	 * @param userID  id do utilizador a adicionar como membro do grupo
	 * @param groupID id do grupo a que adicionar o utilizador
	 * 
	 * @throws UserNotFoundException       se o utilizador userID nao existir
	 * @throws GroupNotFoundException      se o grupo groupID nao existir
	 * @throws UserNotOwnerException       se o utilizador loggado nao for dono do
	 *                                     grupo
	 * @throws UserAlreadyInGroupException se o utilizador userID ja estiver no
	 *                                     grupo
	 * @throws FileNotFoundException 
	 */
	public void addUserToGroup(int userID, int groupID)
			throws UserNotFoundException, GroupNotFoundException, UserNotOwnerException, UserAlreadyInGroupException, FileNotFoundException {
		User user = this.database.getUserByID(userID);
		if (user == null) {
			throw new UserNotFoundException(
					"Erro ao adicionar utilizador ao grupo: Utilizador " + userID + " nao encontrado!");
		}
		Group group = this.database.getGroupByID(groupID);
		if (group == null) {
			throw new GroupNotFoundException(
					"Erro ao adicionar utilizador ao grupo: Grupo " + groupID + " nao encontrado!");
		}
		if (group.getUserList().contains(userID)) {
			throw new UserAlreadyInGroupException(
					"Erro ao adicionar utilizador ao grupo: Utilizador " + userID + " ja no grupo!");
		}
		if (this.loggedUser.getID() != group.getOwnerUser()) {
			throw new UserNotOwnerException(
					"Erro ao adicionar utilizador ao grupo: Utilizador logado nao e dono do grupo!");
		}
		group.addUser(userID);
	}

	/**
	 * Mostra uma lista dos grupos de que o cliente e dono, e uma lista dos grupos a
	 * que pertence
	 *
	 * @return uma lista com dois elmentos: result[0] tem os elementos de que o
	 *         utilizador e dono result[1] tem os elementos a que o utilizador
	 *         pertence
	 */
	@SuppressWarnings("unchecked")
	public List<Set<Group>> viewGroups() {
		List<Set<Group>> result = new ArrayList<>();
		Set<Group> groupsUserOwns = this.database.getGroupsByOwner(this.loggedUser);

		if (groupsUserOwns.isEmpty()) {
			System.out.println("Utilizador logado nao e dono de nehum grupo!");
		}

		result.add(groupsUserOwns);
		Set<Group> groupsUserBelongs = this.database.getGroupsByClient(this.loggedUser);

		if (groupsUserBelongs.isEmpty()) {
			System.out.println("Utilizador logado nao e membro de nehum grupo!");
		}

		result.add(groupsUserBelongs);
		return result;
	}

	/**
	 * Divide o pagamento da quantia pelos membros do grupo com id groupOD
	 * 
	 * @param groupID o id do grupo a quem pedir dinheiro
	 * @param amount  a quantidade de dinheiro a dividir
	 * 
	 * @throws InexistentGroupException se o grupo nao existir
	 * @throws UserNotOwnerException    se o utilizador logado nao for dono do grupo
	 * @throws FileNotFoundException 
	 */
	public void dividePayment(int groupID, int amount)
			throws InexistentGroupException, InexistentGroupException, UserNotOwnerException, FileNotFoundException {
		Group group = this.database.getGroupByID(groupID);
		if (group == null) {
			throw new InexistentGroupException(
					"Erro ao criar um pedido de pagamento de grupo: grupo " + groupID + " nao existente!");
		}
		if (group.getOwnerUser() != this.loggedUser.getID()) {
			throw new UserNotOwnerException(
					"Erro ao criar um pedido de pagamento de grupo: o utilizador nao e dono do grupo " + groupID + "!");
		}

		HashSet<Integer> usersInGroup = group.getUserList();
		DecimalFormat df = new DecimalFormat("0.00");
		double amountPerMember = amount / usersInGroup.size();
		double roundedAmount = Double.parseDouble(df.format(amountPerMember));

		for (Integer userId : usersInGroup) {
			Request request = new Request(this.database.getUniqueRequestID(), roundedAmount, this.loggedUser.getID(), userId);
			database.getUserByID(userId).addRequest(request);
			group.addRequest(request);
		}
	}

	/**
	 * Mostra o estado de cada pedido de pagamento de grupo, ou seja, que membros de
	 * grupo ainda nao pagaram esse pedido
	 * 
	 * @param groupID id do grupo a verificar
	 * 
	 * @throws GroupNotFoundException se o grupo nao existir
	 * @throws UserNotOwner           se o utilizador logado nao for dono do grupo
	 */
	public String statusPayments(int groupID) throws GroupNotFoundException, UserNotOwnerException {
		StringBuilder requests = new StringBuilder();
		Group group = this.database.getGroupByID(groupID);
		if (group == null) {
			throw new GroupNotFoundException(
					"Erro ao mostrar um pedido de pagamento de grupo: grupo " + groupID + " nao existente!");
		}
		if (group.getOwnerUser() != this.loggedUser.getID()) {
			throw new UserNotOwnerException(
					"Erro ao mostrar um pedido de pagamento de grupo: o utilizador nao e dono do grupo " + groupID
					+ "!");
		}
		for (Request request : group.getRequestList()) {
			requests.append(request.toString() + "\n");
		}
		return requests.toString();
	}

	/**
	 * Mostra o historico dos pagamentos do grupo groupID ja concluidos
	 * 
	 * @param groupID o id do grupo a mostrar o historico
	 * 
	 * @throws GroupNotFoundException se o grupo nao existir
	 * @throws UserNotOwnerException  se o utilizador logado nao for dono do grupo
	 * 
	 */
	public String viewHistory(int groupID) throws GroupNotFoundException, UserNotOwnerException {
		Group group = this.database.getGroupByID(groupID);
		StringBuilder result = new StringBuilder();
		if (group == null) {
			throw new GroupNotFoundException(
					"Erro ao mostrar o historico dos pagamentos do grupo: grupo " + groupID + " nao existente!");
		}
		if (group.getOwnerUser() != this.loggedUser.getID()) {
			throw new UserNotOwnerException("Erro ao mostrar o historico dos pagamentos do grupo: o utilizador "
					+ " nao e dono do grupo " + groupID + "!");
		}

		HashSet<Request> history = group.getRequestListHistory();
		for (Request request : history) {
			result.append(request.toString() + "\n");
		}
		return result.toString();
	}

	public void setLoggedUser(User user) {
		this.loggedUser = user;
	}

	public User getLoggedUser() {
		return this.loggedUser;
	}

}
