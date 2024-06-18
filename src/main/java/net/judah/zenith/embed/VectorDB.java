package net.judah.zenith.embed;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.judah.zenith.settings.AutoSave;
import net.judah.zenith.settings.Props;
import net.judah.zenith.swing.Common;
import reactor.core.publisher.Flux;

/** Retrieval, Augmentation, Generation, JSON de/serializer and JTable model */
@Component
public class VectorDB extends DefaultTableModel {

	public static final int DEFAULT_DIMENSIONS = 1536;
	public static final EmbedType DEFAULT_MODEL = EmbedType.SMALL;
    private static final String[] COL_NAMES = {"Source", "Embed Date", "Filesize"};
	public static final String EMBEDDED = "embedded";
	public static final String FILESIZE = "filesize";
	private static final String SYS_PREFIX = System.lineSeparator() + System.lineSeparator();
	private static final String SYS_SUFFIX = SYS_PREFIX + "{data}" + SYS_PREFIX;

	@Autowired private StreamingChatModel chatModel;
	@Autowired private OpenAiApi openAiApi;

	private final List<UserMessage> history = new ArrayList<>();
	private final List<DocData> documents = new ArrayList<>();
	private final TokenTextSplitter textSplitter = new TokenTextSplitter(); // TODO GptBytePairEncodingParams, overlap
	private final ObjectMapper objectMapper = new ObjectMapper();
	@Getter private boolean dirty;
	@Getter private OpenAiEmbeddingOptions options;
	private VectorStore vectors;

    @PostConstruct
    public void init() {
    	createVectorStore("", DEFAULT_MODEL, DEFAULT_DIMENSIONS);
    }

	public OpenAiEmbeddingOptions createVectorStore(String name, EmbedType model, int dimensions) {
		if (checkDirty() == false)
			return null;
        options = OpenAiEmbeddingOptions.builder().withModel(model.getModel()).withDimensions(dimensions).withUser(name).build();
        EmbeddingModel client = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
        vectors = new SimpleVectorStore(client);
    	documents.clear();
    	fireTableDataChanged();
        return options;
	}

    //@GetMapping("/rag") public ChatResponse query(@RequestParam String question) @return ResponseEntity<String>.ok()
	public Flux<ChatResponse> query(EmbedRequest request) {
        List<Document> similarData = vectors.similaritySearch(request.searchRequest());
        String data = similarData.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
        var userMessage = new PromptTemplate("{query}").createMessage(Map.of("query", request.query()));
        var systemMessage = new SystemPromptTemplate(SYS_PREFIX + request.sysPrompt() + SYS_SUFFIX)
        		.createMessage(Map.of("data", data));
        int size = history.size();
        List<Message> messages = size <= Props.HISTORY_SEND ?
        		new ArrayList<>(history) :
        		new ArrayList<>(history.subList(size - Props.HISTORY_SEND, size));
        messages.add(systemMessage);
        messages.add(userMessage);
        var prompt = new Prompt(messages);
        history.add(new UserMessage(request.query()));
        return chatModel.stream(prompt);
	}

	public void saveSession() throws StreamWriteException, DatabindException, IOException {
		saveSession(options.getUser());
	}

	public void saveSession(String sessionName) throws StreamWriteException, DatabindException, IOException {
		options.setUser(sessionName);
		if (false == vectors instanceof SimpleVectorStore)
			throw new UnsupportedOperationException(
					"I don't know how to save " + vectors.getClass().getSimpleName());
		Common.Dual files = Props.getVectors(sessionName);
		VectorData session = new VectorData(options, documents);
		objectMapper.writeValue(files.json(), session);
		((SimpleVectorStore)vectors).save(files.db());
		dirty = false;
	}

	public boolean loadSession(File file) {
		if (checkDirty() == false)
			return false;
		if (file == null || !file.isFile())
			throw new UnsupportedOperationException("No File.");
		String fileName = file.getAbsolutePath();
		int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1)
            fileName = fileName.substring(0, lastDotIndex);
        else throw new UnsupportedOperationException(
				"I don't know how to load " + fileName);
        File json = new File(fileName + Props.DOTJSON);
        if (json.isFile() == false)
        	throw new UnsupportedOperationException("Missing " + json.getName() + " database description.");
		if (false == vectors instanceof SimpleVectorStore)
			throw new UnsupportedOperationException(
				"I don't know how to load " + vectors.getClass().getSimpleName());
        try {
        	VectorData db = objectMapper.readValue(json, VectorData.class);
        	createVectorStore(db.options().getUser(),
        			EmbedType.match(db.options().getModel()), db.options().getDimensions());
        	((SimpleVectorStore)vectors).load(file);
        	documents.clear();
        	options = db.options();
        	documents.addAll(db.documents());
        	dirty = false;
        	fireTableDataChanged();
        	return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
	}

	/** @return false if cancelled */
	private boolean checkDirty() {
        if (!dirty)
        	return true;
		int response = JOptionPane.showConfirmDialog(null, "Save current session?", "Confirm",
        		JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.CANCEL_OPTION)
        	return false;
        if (response == JOptionPane.YES_OPTION) {
        	try {
            	saveSession();
        	} catch (Exception e) {
        		e.printStackTrace();
        		return false;
        	}
        }
        // not cancelled
        dirty = false;
        return true;
	}

	private void checkAutoSave() {
		if (Props.autoSave(AutoSave.Settings))
			try {
				saveSession();
			} catch (Exception e) { e.printStackTrace(); }
		else
			dirty = true;
	}

	public void addDoc(File file) {
		if (file == null || !file.isFile())
			throw new InvalidParameterException("" + file);
		List<Document> docs = textSplitter.apply(new TextReader("file://" + file.getAbsolutePath()).get());
		Document result = docs.getFirst();
		// TextReader applies "source" metadata
		result.getMetadata().put(EMBEDDED, System.currentTimeMillis());
		result.getMetadata().put(FILESIZE, file.length());
		vectors.add(docs);
		// TableModel
        documents.add(new DocData(result));
    	int row = getRowCount();
        fireTableRowsInserted(row, row);
        checkAutoSave();
	}

	public void removeDoc(DocData doc) {
		vectors.delete(List.of(doc.id()));
    	int row = documents.indexOf(doc);
    	documents.remove(row);
        fireTableRowsDeleted(row, row);
        checkAutoSave();
	}

	public DocData getDocument(int i) {
		return documents.get(i);
	}

	public String getSession() {
		if (options == null)
			return "new sesh";
		return options.getUser();
	}

	public DocData getRow(int row) {
		return getDocument(row);
	}

    @Override
    public int getRowCount() {
        return documents == null ? 0 : documents.size();
    }

    @Override
    public int getColumnCount() {
        return COL_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COL_NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Map<String, Object> meta = documents.get(rowIndex).metadata();
        switch (columnIndex) {
            case 0:
                return meta.get(TextReader.SOURCE_METADATA);
            case 1:
                return Common.datetime(Long.parseLong("" + meta.get(EMBEDDED)));
            case 2:
                return meta.get(FILESIZE);
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return String.class;
            case 1: return String.class;
            case 2: return Long.class;
            default: return Object.class;
        }
    }

}
