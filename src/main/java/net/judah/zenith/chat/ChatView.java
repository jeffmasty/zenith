package net.judah.zenith.chat;

import static net.judah.zenith.swing.Icons.*;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.Role;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.judah.zenith.audio.TextToVoice;
import net.judah.zenith.embed.FileChooser;
import net.judah.zenith.model.Chat;
import net.judah.zenith.model.Contact;
import net.judah.zenith.model.Memory;
import net.judah.zenith.model.UserName;
import net.judah.zenith.settings.AutoSave;
import net.judah.zenith.settings.Props;
import net.judah.zenith.settings.StaticProperties;
import net.judah.zenith.swing.Common;
import net.judah.zenith.swing.HistoryText;
import net.judah.zenith.swing.Scroller;
import net.judah.zenith.wav.MicCheck;
import net.judah.zenith.wav.Microphone;
import net.judah.zenith.wav.WavPlayer;
import reactor.core.publisher.Flux;

@Component
public class ChatView extends JPanel implements Common, MicCheck {

	public static final String[] MODELS = {"gpt-4o", "gpt-3.5-turbo", "gpt-4-turbo"};
	static final int DEFAULT_TEMP = 7; // = 0.7
	public static final String PROMPT = "Inquire: ";

	@Autowired private OpenAiApi openAiApi;
	@Autowired private TextToVoice textToVoice;
	@Autowired private Microphone mic;

	private final ChatClient  chatClient;
	private final Memory mem;
	private final WavPlayer player = WavPlayer.getInstance();
	@Getter private final HistoryText input = new HistoryText();
	private final TextScroll scroll = new TextScroll();
	private final JComboBox<String> model = new JComboBox<>(MODELS);
	private final JComboBox<Integer> temp = new JComboBox<>();
	private final JButton searchBtn = new Btn("", e->exec());
	private final JButton micBtn = new Btn("", e->mic());
	private final JButton audio = new Btn("", e->toggleAudio());
	private final JButton attachments = new Btn("", e->toggleImage());

    private Chat current;
	private File image;

	public ChatView(ChatClient.Builder builder) {

		String user = StaticProperties.getInstance().getUsername();
		mem = (user == null) ? new Memory() : new UserName(user);
		chatClient = builder.defaultAdvisors(mem).build();
    	input.addActionListener(e -> exec());

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JComponent query = Box.createHorizontalBox();
		query.add(Box.createHorizontalStrut(3));
		query.add(new JLabel(PROMPT));
		query.add(input);
		query.add(searchBtn);
		query.add(Box.createHorizontalStrut(3));
		query.setMaximumSize(Common.ONE_LINER);
		add(query);
		add(new Scroller(scroll));
		add(Box.createVerticalGlue());
	}

	@PostConstruct
	public void init() {
		add(footer());
        EventQueue.invokeLater(()->input.requestFocusInWindow());
	}

	private JComponent footer() {
        searchBtn.setIcon(SEND);
		micBtn.setIcon(MIC);
		audio.setIcon(SPEAKERS);
		attachments.setIcon(PAPERCLIP);
		model.setSelectedIndex(0);
		for (int i = 1; i < 20; i++)
			temp.addItem(i);
		temp.setSelectedItem(DEFAULT_TEMP);
		temp.setToolTipText("Temperature setting (1 to 20)");

		JComponent center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
		center.add(Common.wrap(
				new JLabel(" Model "), model, new JLabel(" @ "), temp, new JLabel("° ")));

		JComponent result = Box.createHorizontalBox();
		result.add(Box.createHorizontalGlue());
		result.add(center);
        result.add(Box.createHorizontalGlue());

        result.add(attachments);
        result.add(audio);
		result.add(micBtn);

        result.add(new Btn(SEND, e->exec()));
        result.add(Box.createHorizontalStrut(STRUT));
		return result;
	}

	private void clearImage() {
		if (image == null)
			return;
		image = null;
		attachments.setIcon(PAPERCLIP);
	}

	private void toggleImage() {
		if (image != null)
			image = null;
		else
			image = FileChooser.choose(JFileChooser.FILES_AND_DIRECTORIES, Props.DOTPNG, "PNG images");
		attachments.setIcon(image == null ? PAPERCLIP : ATTACHED);
	}

	private void exec() {
		String query = input.acquire();
		if (query.isBlank() || !input.isEnabled())
			return;

		OpenAiChatOptions options = options();
		Flux<?> flux = (image == null) ?
			stream(query, options) :
			visionStream(query, options, image);
		flux.subscribe(ignore-> {},
				err-> endStream(), ()-> endStream());
		current = new Chat(query, options, flux);
    	scroll.show(current);
    	input.setText("Communicating...");
    	input.setEnabled(false);
	}

	private Flux<ChatResponse> visionStream(String query, OpenAiChatOptions options, File attach) {
		UserMessage userMessage = new UserMessage(query, List.of(new Media(MimeTypeUtils.IMAGE_PNG, new FileSystemResource(attach))));
		clearImage();
		return chatClient.prompt(new Prompt(List.of(userMessage), options)).stream().chatResponse();
	}

	private Flux<ChatCompletionChunk> stream(String query, OpenAiChatOptions options) {
		ArrayList<ChatCompletionMessage> msgs = new ArrayList<ChatCompletionMessage>();
		msgs.add(mem.createSysMsg(query));
		msgs.add(new ChatCompletionMessage(query, Role.USER));
		ChatCompletionRequest request = new ChatCompletionRequest(msgs, model(), temp(), true);
		return openAiApi.chatCompletionStream(request);
	}

	private float temp() {
		return temp == null ? 1 : (Integer)temp.getSelectedItem() * 0.1f;
	}

	private String model() {
		return "" + model.getSelectedItem();
	}

	public void clear() {
		input.setText("");
		input.setEnabled(true);
		input.grabFocus();
	}

	public void endStream() {
		if (TextToVoice.isAutoPlay())
			play(current);
		if (Props.autoSave(AutoSave.Texts)) {
			scroll.getChatWidget(current).save();
		}
		clear();
	}

	public void mic() {
		boolean recording = mic.toggle(this);
		if (recording)
			input.setText("Recording started...");
		else
			input.setText("Processing audio...");
		micBtn.setIcon(recording ? RECORD : MIC);
	}

	private OpenAiChatOptions options() {
		return OpenAiChatOptions.builder()
			.withModel("" + model.getSelectedItem())
			.withTemperature(temp()).build();
	}

	public void play(Contact interaction) {
		File location = textToVoice.location(interaction.start());
		if (location.isFile() == false)
			textToVoice.callAiAndWrite(scroll.getChatWidget(interaction).getAnswer(), location);
		if (location.isFile()) try {
			player.play(location, audio);
		} catch (Exception e) {e.printStackTrace(); }
	}

	private void toggleAudio() {
		if (current == null)
			return;
		if (WavPlayer.isPlaying())
			WavPlayer.stop();
		else
			play(current);
	}

	@Override
	public void transcribed(String output) {
		input.setText(output);
		if (Props.isAutoQuery())
			exec();
		else
			input.grabFocus();
	}

	@Override
	public void micDrop(Throwable t) { t.printStackTrace(); }

}
