package net.judah.zenith.chat;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.Role;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.judah.zenith.audio.TextToVoice;
import net.judah.zenith.model.Encounter;
import net.judah.zenith.settings.AutoSave;
import net.judah.zenith.settings.Props;
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
	public static final String PROMPT = "Inquire: ";
	static final int DEFAULT_TEMP = 7; // = 0.7

	@Autowired private OpenAiApi openAiApi;
	@Autowired private TextToVoice textToVoice;
	@Autowired private Microphone mic;
	@Autowired private WavPlayer player;
	@Autowired private ImageIcon micIcon;
	@Autowired private ImageIcon recordIcon;
	@Autowired private ImageIcon sendIcon;
	@Autowired private ImageIcon send16;
	@Autowired private ImageIcon speakersIcon;

    private Encounter current;
    private final HistoryText input = new HistoryText(new Dimension(520, 25));
	private final ArrayList<ChatCompletionMessage> history = new ArrayList<>();
    private final TextScroll scroll;
    private final JComboBox<String> model = new JComboBox<>(MODELS);
	private final JComboBox<Integer> temp = new JComboBox<>();
	private final JButton searchBtn;
	private JButton micBtn;
	private JButton audio;

	public ChatView() {
		scroll = new TextScroll(()->this.endStream());
    	input.addActionListener(e -> exec());
        searchBtn = new Btn("", e -> exec());

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Common.wrap(new JLabel(PROMPT), input, Common.wrap(searchBtn)));
		add(new Scroller(scroll));
		add(Box.createVerticalGlue());
	}

	@PostConstruct
	public void init() {
		add(footer());
        EventQueue.invokeLater(()->clear());
        searchBtn.setIcon(send16);
	}


	private JComponent footer() {
		micBtn = new Btn(micIcon, e->mic());
		audio = new Btn(speakersIcon, e->toggleAudio());
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

        result.add(audio);
		result.add(micBtn);
        result.add(new Btn(sendIcon, e->exec()));
        result.add(Box.createHorizontalStrut(STRUT));
		return result;
	}

	private void exec() {
		String query = input.acquire();
		if (query.isBlank())
			return;
		history.add(new ChatCompletionMessage(query, Role.USER));
        int size = history.size();
        List<ChatCompletionMessage> messages = size <= Props.HISTORY_SEND ?
        		new ArrayList<>(history) :
        		new ArrayList<>(history.subList(size - Props.HISTORY_SEND, size));
		current = stream(messages, "" + model.getSelectedItem(), temp());
    	input.setText("Communicating...");
		input.setEnabled(false);
    	scroll.show(current); // ChatWidget subscribes and calls endStream() when done
	}

	private Encounter stream(List<ChatCompletionMessage> history, String model, float temp) {
		ChatCompletionRequest request = new ChatCompletionRequest(history, "" + model, temp, true);
		Flux<ChatCompletionChunk> flux = openAiApi.chatCompletionStream(request);
		return new Encounter(request, flux, System.currentTimeMillis());
	}

	private float temp() {
		return temp == null ? 1 : (Integer)temp.getSelectedItem() * 0.1f;
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
		micBtn.setIcon(recording ? recordIcon : micIcon);
	}

	public void play(Encounter interaction) {
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
