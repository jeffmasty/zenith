package net.judah.zenith.image;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.judah.zenith.model.Snapshot;
import net.judah.zenith.swing.Common;
import net.judah.zenith.swing.Common.Btn;
import net.judah.zenith.swing.HistoryText;
import net.judah.zenith.swing.Scroller;
import net.judah.zenith.wav.MicCheck;
import net.judah.zenith.wav.Microphone;

@Component 
public class ImageView extends JPanel implements MicCheck {

	public static final String PROMPT = "Imagine: ";
	
	@Autowired private OpenAiImageModel client;
	@Autowired private Microphone mic;
	
    private final HistoryText input = new HistoryText(new Dimension(520, 25));
	private final ImageSettings settings = new ImageSettings();
	private final ImageScroll scroll;
    private Snapshot inProgress;
	
	public ImageView() {
		scroll = new ImageScroll(()->clear());
        Btn exec = new Btn(UIManager.getIcon("FileChooser.detailsViewIcon"), e -> exec());
		Btn record = new Btn(" ⏺ ", e -> mic(), Color.RED);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Common.wrap(new JLabel(PROMPT), input, Common.wrap(exec, record)));
		add(new Scroller(scroll));
		add(Box.createVerticalGlue());
		add(settings);
	    input.addActionListener(e->exec());
	}
	@PostConstruct
	public void init() {
	}
	
	public void clear() {
		input.setText("");
		input.grabFocus();
		inProgress = null;
	}
	public void mic() {
		boolean recording = mic.toggle(this);
		if (recording) 
			input.setText("Recording started...");
		else 
			input.setText("Processing audio...");
	}

	public void exec() {
		String query = input.acquire();
		if (query.isBlank())
			return;
	    long start = System.currentTimeMillis();
	    input.setText("Calculating...");

	    int width = 1024;
		int height = 1024;
		String[] split = ("" + settings.size.getSelectedItem()).split("x");
		try {
			width = Integer.parseInt(split[0]);
			height = Integer.parseInt(split[1]);
		} catch (Throwable t) { System.out.println(t.getMessage()); }
		
		ImageOptions options = ImageOptionsBuilder.builder()
	        .withModel("" + settings.model.getSelectedItem())
	        .withWidth(width)
	        .withHeight(height)
	        .build();
	    inProgress = new Snapshot(query, options, start);
	    ImageWidget widget = scroll.show(inProgress);

	    new Thread(()->{
		    ImageResponse response = client.call(new ImagePrompt(query, options));
		    String imageUrl = response.getResult().getOutput().getUrl();
		    input.setText("Retreiving...");
		    widget.downloadImage(imageUrl);
	    }).start();
	}

	@Override
	public void transcribed(String result) {
		input.setText(result);
		exec();		
	}
	
	@Override
	public void micDrop(Throwable t) { t.printStackTrace(); }
}
