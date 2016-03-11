package domain;

import interfaces.ServerThreadInterface;
import network.ClientMessage;
import network.ServerMessage;
import network.ServerSocketNetwork;
import parsers.ClientMessageParser;

public class ServerThread extends Thread implements ServerThreadInterface {
	
	private ServerThreadContext serverThreadContext;
	
	
	public ServerThread(ServerThreadContext serverThreadContext) {
		this.serverThreadContext = serverThreadContext;
	}

	
	public void run() {
		ServerSocketNetwork serverSocketNetwork = serverThreadContext.getServerSocketNetwork();
		
		//recebe Mensagem do cliente
		ClientMessage clientMessage = serverSocketNetwork.getClientMessage();
		
		//processa a mensagem do cliente e cria mensagem de resposta
		ClientMessageParser clientMessageParser = new ClientMessageParser(clientMessage, serverSocketNetwork);
		ServerMessage serverMessage = clientMessageParser.processRequest();
		
		//envia resposta ao cliente
		serverSocketNetwork.sendMessage(serverMessage);
	
		/*
		if(clientMessage.getMessageType().equals(MessageType.FILE))
			serverSocketNetwork.sendMessage(new ServerMessage(MessageType.OK));
		File b = serverSocketNetwork.receiveFile(clientMessage.getFileSize(), "teste1.jpeg");

		System.out.println("Mensagem: " + clientMessage);
		System.out.println("Thread terminada.");
		 */
	}

	
}
