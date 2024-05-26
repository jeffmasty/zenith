package net.judah.zenith.chat;

import static net.judah.zenith.Zenith.DATA_DIR;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.Voice;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.judah.zenith.Zenith;
import net.judah.zenith.audio.Text2Speech;
import net.judah.zenith.model.Encounter;
import net.judah.zenith.swing.Common;
import net.judah.zenith.swing.HistoryText;
import net.judah.zenith.swing.Scroller;
import net.judah.zenith.wav.MicCheck;
import net.judah.zenith.wav.Microphone;
import net.judah.zenith.wav.WavPlayer;

@Component
public class TextView extends JPanel implements Common, MicCheck {

	public static final String PROMPT = "Inquire: ";
	
	@Autowired private OpenAiApi openAiApi;
	@Autowired private Text2Speech text2Speech;
	@Autowired private Microphone mic;
	
    private final HistoryText input = new HistoryText(new Dimension(520, 25));
	private final Settings footer = new Settings(Zenith.MODELS, true);
	private final TextScroll scroll;
    private Encounter inProgress;
    
	public TextView() {
		scroll = new TextScroll(()->this.endStream());
    	input.addActionListener(e -> exec(input.acquire()));
    	
        Btn exec = new Btn(UIManager.getIcon("FileChooser.detailsViewIcon"), e -> exec(input.getText()));
        Btn record = new Btn(" ⏺ ", e -> mic(), Color.RED);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Common.wrap(new JLabel(PROMPT), input, Common.wrap(exec, record)));
		add(new Scroller(scroll));
		add(Box.createVerticalGlue());
		add(footer);
	}

	@PostConstruct
	public void init() {
        EventQueue.invokeLater(()->clear());
	}
	
	private void exec(String question) {
    	if (question.isBlank())
    		return;
		inProgress = Zenith.stream(question, "" + footer.model.getSelectedItem(), footer.temp(), openAiApi);
    	input.setText("Communicating...");
		input.setEnabled(false); 
    	scroll.show(inProgress); // ChatWidget subscribes and calls endStream() when done
	}

	public void clear() {
		input.setText("");
		input.setEnabled(true);
		input.grabFocus();
		inProgress = null;
	}
	
	public void endStream() {
		if (footer.voice.isSelected())
			toSpeech(inProgress);
		clear();
	}

	public void mic() {
		boolean recording = mic.toggle(this);
		if (recording) 
			input.setText("Recording started...");
		else 
			input.setText("Processing audio...");
	}
	
	public void toSpeech(Encounter encounter) {
			try {
				File location = encounter.location(DATA_DIR);
				if (false == location.isFile()) { // already created, play from disk
					// to AI
					String text = scroll.getAnswer(encounter);
					SpeechResponse response = Zenith.text2speech(text, 
							(Voice)footer.voices.getSelectedItem(), (float)footer.speed.getSelectedItem(), text2Speech);
					Files.write(Path.of(location.toURI()), 
							response.getResult().getOutput()); 
				}
				WavPlayer.play(location); 
			} catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	public void transcribed(String output) {
		input.setText(output);
		exec(output);
	}

	@Override
	public void micDrop(Throwable t) { t.printStackTrace(); }

}
