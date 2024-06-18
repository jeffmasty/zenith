package net.judah.zenith.model;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.Role;

import net.judah.zenith.settings.StaticProperties;

/**Prepares a SystemPromt with personalized user's name and chat history(super).
 * saved in zenith.properties file as:
 * username=User Name
 */
public class UserName extends Memory {

	private final String sysText;

	public UserName(String username) {
		super(new InMemoryChatMemory());
		StringBuffer sb = new StringBuffer("User's name is ").append(StaticProperties.getInstance().getUsername());
		sb.append(NL);
		sysText = sb.append(MEMORY_PREAMBLE).toString();
	}

	@Override
	public ChatCompletionMessage createSysMsg(String query) {
		List<Message> memoryMessages = getChatMemoryStore().get(defaultConversationId, defaultChatMemoryRetrieveSize);

		String memory = (memoryMessages != null) ? memoryMessages.stream()
				.filter(m -> m.getMessageType() != MessageType.SYSTEM)
				.map(m -> m.getMessageType() + ":" + m.getContent())
				.collect(Collectors.joining(System.lineSeparator())) : "";
		String msg = new StringBuilder(sysText).append(memory).append(HR).append(NL).toString();
		getChatMemoryStore().add(defaultConversationId, List.of(new UserMessage(query)));
		return new ChatCompletionMessage(msg, Role.SYSTEM);
	}

}
