package domain.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import network.managers.ClientNetworkManager;
import network.messages.ClientMessage;
import network.messages.ClientPGPMessage;
import network.messages.MessageType;
import network.messages.ServerContactTypeMessage;
import network.messages.ServerMessage;
import security.Security;
import util.UserUtil;

/**
 * Classe que representa um cliente, isto é responsavel por contactar o servidor
 * 
 * @author Pedro, José e Antonio
 *
 */
public class Client {

	private static ClientNetworkManager clientNetwork;

	/**
	 * Funçao principal
	 * 
	 * @param args
	 *            Argumentos com o pedido do utilizador
	 * 
	 */
	public static void main(String[] args) {
		ArgsParser argsParser = new ArgsParser(args);
		UserUtil userInterface = new UserUtil();

		// validar input
		if (!argsParser.validateInput()) {
			userInterface.printArgsUsage();
			System.exit(0);
		}

		// verifica se o utlizador preencheu password
		if (!argsParser.passwordFilled()) {
			argsParser.setPassword(userInterface.askForPassword());
		}

		// Cria Classe de comunicação entre Cliente e servidor
		int port = Integer.parseInt(argsParser.getServerPort());
		Socket socket = null;

		try {
			socket = new Socket(argsParser.getServerIP(), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		clientNetwork = new ClientNetworkManager(socket);
		System.out.println("Cliente ligado ao servidor " + argsParser.getServerIP() + ":" + argsParser.getServerPort());

		// gerar chave assimétrica
		KeyPair keyPair = Security.getKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();

		// Cria mensagem de comunicaçao com o pedido do cliente
		ClientMessage clientMessage = argsParser.getMessage();

		// Verificar tipo de mensagem
		switch (clientMessage.getMessageType()) {
		// client quer enviar uma mensagem
		case MESSAGE:
			// enviar mensagem a perguntar o tipo do destinatario (contacto?
			// grupo?)
			ClientMessage aux_message = new ClientMessage(clientMessage.getUsername(), clientMessage.getPassword(),
					MessageType.MESSAGE);

			aux_message.setDestination(clientMessage.getDestination());
			clientNetwork.sendMessage(aux_message);

			// obter resposta do servidor
			ServerContactTypeMessage serverContactTypeMessage = (ServerContactTypeMessage) clientNetwork
					.receiveMessage();

			switch (serverContactTypeMessage.getMessageType()) {
			case CONTACT:
				ClientPGPMessage clientPGPMessage = new ClientPGPMessage();
				
				// gerar assinatura e enviar ao servidor
				byte[] clientSignature = Security.signMessage(clientMessage.getContent(), privateKey);
				clientPGPMessage.setSignature(clientSignature);

				// obter chave secreta
				SecretKey secretKey = Security.getSecretKey();

				// cifrar mensagem com chave secreta
				byte[] encryptedMessage = Security.cipherWithSecretKey(clientMessage.getContent().getBytes(),
						secretKey);
				clientPGPMessage.setMessage(encryptedMessage);

				ArrayList<String> groupMembers = (ArrayList<String>) serverContactTypeMessage.getGroupMembers();

				// cifrar chave privada, usada para cifrar mensagem anterior
				for (String username: groupMembers) {
					byte[] wrappedSecretKey = Security.wrapSecretKey(username, secretKey);
					clientPGPMessage.addWrappedSecretKey(wrappedSecretKey);
				}
				
				clientNetwork.sendMessage(clientPGPMessage);

				break;
			default:
				break;
			}

			// obter key publica do utilizador
			// Key myKey = new SecretKeySpec(serverMsg.getContent().getBytes(),
			// "AES");

			clientNetwork.sendMessage(aux_message);

			break;

		default:
			break;
		}

		// envia a mensagem
		Boolean sended = clientNetwork.sendMessage(clientMessage);

		if (sended) {
			// recebe a resposta
			ServerMessage serverMsg = (ServerMessage) clientNetwork.receiveMessage();

			// passa resposta ao parser para ser processada
			ServerResponseParser srp = new ServerResponseParser(userInterface, clientNetwork, argsParser.getUsername());

			srp.ProcessMessage(serverMsg);

		}
		// fecha a ligaçao ao servidor
		clientNetwork.close();

	}
}
