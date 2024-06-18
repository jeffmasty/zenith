package net.judah.zenith.model;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.Role;

public class Memory extends PromptChatMemoryAdvisor {
	public static final int DEFAULT_CHAT_MEMORY_RESPONSE_SIZE = 100;
	public static final String DEFAULT_CHAT_MEMORY_CONVERSATION_ID = "default";
	protected static final String NL = System.lineSeparator();
	protected static final String HR = NL + "---------------------" + NL;
	protected static final String MEMORY_SUBSTITUTION = "{memory}";
	//	Use the conversation history from the MEMORY section to provide accurate answers.
	//	---------------------
	//	MEMORY:
	//	{memory}
	//	---------------------
	protected static final String MEMORY_PREAMBLE = NL +
			"Use the conversation memory from the MEMORY section to provide accurate answers."
			+ NL + HR + "MEMORY:" + NL;
	public static final String DEFAULT_ADVISED_SYS_TEXT = MEMORY_PREAMBLE + MEMORY_SUBSTITUTION + HR + NL;
	public Memory() {
		this(new InMemoryChatMemory());
	}

	public Memory(ChatMemory chatMemory) {
		super(chatMemory, DEFAULT_ADVISED_SYS_TEXT);
	}

	public ChatCompletionMessage createSysMsg(String query) {
		List<Message> memoryMessages = getChatMemoryStore().get(defaultConversationId, defaultChatMemoryRetrieveSize);

		String memory = (memoryMessages != null) ? memoryMessages.stream()
				.filter(m -> m.getMessageType() != MessageType.SYSTEM)
				.map(m -> m.getMessageType() + ":" + m.getContent())
				.collect(Collectors.joining(System.lineSeparator())) : "";

		String msg = MEMORY_PREAMBLE + memory + HR + NL;


		getChatMemoryStore().add(defaultConversationId, List.of(new UserMessage(query)));

		return new ChatCompletionMessage(msg, Role.SYSTEM);
	}
}
