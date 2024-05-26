package net.judah.zenith.embed;

import static net.judah.zenith.swing.Common.NL;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.judah.zenith.model.Interaction;

/** Retrieval, Augmentation, Generation */
@Component @RequiredArgsConstructor
public class Embedder {
	public static final String EXT = ".rag";
	static final String PREFIX = NL + NL;
	static final String SUFFIX = PREFIX + "{data}" + PREFIX;
	
	@Autowired private StreamingChatModel ai;
	@Autowired private SimpleVectorStore vectors;
	private final TokenTextSplitter textSplitter = new TokenTextSplitter();
	
	public boolean vectorize(File file) {
		if (file == null || !file.isFile()) 
			return false;
		List<Document> docs = new TextReader("file://" + file.getAbsolutePath()).get();
		vectors.add(textSplitter.apply(docs));
		return true;
	}
	
	public void saveSession(File file) {
	  	vectors.save(file);
	}
	
	public void loadSession(File file) {
		if (file == null || !file.isFile()) return;
		vectors.load(file);
	}
	
    //@GetMapping("/rag") public ChatResponse query(@RequestParam String question) @return ResponseEntity<String>.ok()
	public Interaction query(EmbedRequest request) {
        List<Document> similarData = vectors.similaritySearch(request.query());
        String data = similarData.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
        var userMessage = new PromptTemplate("{query}").createMessage(Map.of("query", request.query()));
        var systemMessage = new SystemPromptTemplate(PREFIX + request.sysPrompt() + SUFFIX)
        		.createMessage(Map.of("data", data));
        var prompt = new Prompt(List.of(systemMessage, userMessage));
        return new Interaction(request, ai.stream(prompt));
	}
	
}
