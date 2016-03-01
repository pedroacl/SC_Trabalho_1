package factories;

import entities.ChatMessage;
import network.MessageType;

public class ChatMessageFactory {
	private Long id;
	

	private ChatMessageFactory messageFactory = new ChatMessageFactory();
	
	private ChatMessageFactory() {
		id = 1L;
	}
	

	public ChatMessageFactory getInstance() {
		return messageFactory;
	}
	
	
	public ChatMessage build(String fromUser, String toUser, String message, MessageType messageType) {
		ChatMessage chatMessage = new ChatMessage(fromUser, toUser, message, messageType);
		chatMessage.setId(id);
		id++;
		
		return chatMessage;
	}
}
