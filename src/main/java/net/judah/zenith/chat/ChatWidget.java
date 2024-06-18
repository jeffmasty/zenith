package net.judah.zenith.chat;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk;

import lombok.Getter;
import net.judah.zenith.model.Contact;
import net.judah.zenith.settings.Folder;
import net.judah.zenith.settings.Props;
import net.judah.zenith.swing.Widget;

public class ChatWidget extends Widget {

	@Getter private final Contact chat;
	private final AiText answer;
	private long end;

	@SuppressWarnings("unchecked")
	public ChatWidget(Contact transaction, TextScroll parent) {
		super(parent, transaction.query());
		this.chat = transaction;
		answer = new AiText(parent);
		add(answer, BorderLayout.CENTER);

		chat.flux().subscribe(
			chunk -> processChunk(chunk),
            error -> { System.err.println(error); },
            ()-> endStream());


	}

	private void processChunk(Object obj) {
		String content = null;
		if (obj instanceof ChatResponse) {
			content = ((ChatResponse)obj).getResult().getOutput().getContent();
		}
		else if (obj instanceof ChatCompletionChunk) {
			content = ((ChatCompletionChunk)obj).choices().get(0).delta().content();
		}
		if (content != null && !content.isEmpty()) // complete?
			append(content);

	}

	private void endStream() {
		end = System.currentTimeMillis();
		System.out.println();
	}

	public void append(String chunk) {
		answer.append(chunk);
		System.out.print(chunk);
	}

	@Override
	protected void open() {
		File saved = save();
		if (saved == null)
			return;
		try {
			Desktop.getDesktop().open(saved);
		} catch (IOException e) { e.printStackTrace(); }
	}

	@Override
	protected File save() {
		File file = new File(Props.getFolder(Folder.TEXTS), chat.start() + ".txt");
		try (PrintWriter printWriter = new PrintWriter(new FileWriter(file.getAbsolutePath()))) {
            printWriter.println(getAnswer());
            return file;
        } catch (IOException e) { e.printStackTrace();}
		return null;
	}

	public String getAnswer() {
		return answer.getText();
	}

	@Override
	public void copy() {
		if (answer.getSelectedText() == null || answer.getSelectedText().isBlank())
			answer.selectAll();
		answer.copy();
	}

	@Override
	protected void info() {
		JOptionPane.showMessageDialog(null, chat.info(), "Chat", JOptionPane.INFORMATION_MESSAGE);
	}

	public long millis() {
		return (end - chat.start());
	}

	public String seconds() {
		return String.format("%2.03f", millis() * 0.001f);
	}

}
