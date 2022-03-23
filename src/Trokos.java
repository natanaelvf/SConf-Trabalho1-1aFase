import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import exceptions.IllegalArgumentNumberException;
import exceptions.InvalidPasswordException;
import exceptions.InvalidUserIdException;

public class Trokos {
	
	final int PASSWORD_LENGTH = 8;

	/*2. Alterar o cliente e o servidor de modo a, após a autenticação, enviar um ficheiro do cliente para o servidor.
	Sugestões:
	a) usar os mesmos ObjectOutputStream e ObjectInputStream
	b) usar os métodos write(byte[] buf, int off, int len) e read(byte[] buf, int off, int len)
	c) enviar previamente a dimensão do ficheiro
	*/
	
	public static void main(String[] args) throws IllegalArgumentNumberException, NumberFormatException, InvalidUserIdException, InvalidPasswordException {
		if (args.length > 2) {
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
					+ " dos quais: um digito, uma letra maiuscula, uma minuscula e um caracter especial (");
		}
		ObjectOutputStream outputStream;
		ObjectInputStream inputStream;
	}
	
	public static boolean isValidPassowrd(String password) {
		return password.matches("/^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?!.*\\s).{6,20}$/");
	}
}
