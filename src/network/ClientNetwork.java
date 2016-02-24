package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import entities.ChatMessage;
import factories.MessageFactory;

public class ClientNetwork {

	private Socket socket;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private MessageFactory requestFactory = null;
	
	public ClientNetwork() {
		socket = null;
		requestFactory = new MessageFactory();
	}
	

	public boolean connect(String serverIP, String serverPort) {
		socket = getSocket(serverIP, serverPort);
		
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return socket != null && out != null;
	}
	
	
	public void disconnetc() {
		try {
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static Socket getSocket(String serverIP, String serverPort) {
		Socket socket = null;
		int port = Integer.parseInt(serverPort);

		for (int i = 0; i < 50; i++) {
			try {
				socket = new Socket(serverIP, port);
				return socket;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				//tentar proximo porto
				System.out.println("Porto " + port + " nao disponivel");
				port++;
			}
		}
		
		return null;
	}
	
	
	public void sendMessage(ClientMessage message) {
		//ClientMessage clientMessage = requestFactory().
		try {
			out.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public ServerMessage getServerMessage() {
		ServerMessage serverMessage = null;

		try {
			serverMessage = (ServerMessage) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return serverMessage;
	}
}
