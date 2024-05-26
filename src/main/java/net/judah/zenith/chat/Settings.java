package net.judah.zenith.chat;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.Voice;

import net.judah.zenith.swing.Common;
import net.judah.zenith.wav.WavPlayer;

public class Settings extends JPanel {
	public static final int DEFAULT_TEMP = 7; // = 0.7
	public static final Voice DEFAULT_VOICE = Voice.NOVA;
	public static final Float[] SPEEDS = {0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f};

    public final JComboBox<String> model = new JComboBox<>();
	public final JComboBox<Integer> temp = new JComboBox<>(); 
	public final JComboBox<Voice> voices = new JComboBox<>(Voice.values());
    public final JComboBox<Float> speed = new JComboBox<>(SPEEDS);
    public final JToggleButton voice = new JToggleButton(" Voice ");  

	public Settings(String[] models, boolean tempAware) {
		
		for (String s : models) 
			model.addItem(s);
		model.setSelectedIndex(0);
		voices.setSelectedItem(DEFAULT_VOICE);
		speed.setSelectedItem(1f);
		voice.setToolTipText("Speak answers back");
		voice.addActionListener(e->toggleVoice());
		JPanel menu = new JPanel();
		menu.add(new JLabel(" Model "));
		menu.add(model);
		if (tempAware) {
			for (int i = 1; i < 20; i++)
				temp.addItem(i);
			temp.setSelectedItem(DEFAULT_TEMP);
			temp.setToolTipText("Temperature setting (1 to 20)");
			menu.add(new JLabel(" @ "));
			menu.add(temp);
			menu.add(new JLabel("° "));
		}
		menu.add(voice);
		menu.add(voices);
		menu.add(new JLabel(" @ "));
		menu.add(speed);
		add(Common.wrap(menu));
	}

	public void toggleVoice() {
		if (!voice.isSelected() && WavPlayer.isPlaying()) 
			WavPlayer.stop();
	}


	public float temp() {
		return temp == null ? 1 : (Integer)temp.getSelectedItem() * 0.1f;
	}



}
