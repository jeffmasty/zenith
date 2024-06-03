package net.judah.zenith.image;

import java.awt.Dimension;
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

import org.springframework.ai.image.ImageMessage;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.judah.zenith.model.Snapshot;
import net.judah.zenith.settings.AutoSave;
import net.judah.zenith.settings.Props;
import net.judah.zenith.swing.Common;
import net.judah.zenith.swing.Common.Btn;
import net.judah.zenith.swing.HistoryText;
import net.judah.zenith.swing.Scroller;
import net.judah.zenith.wav.MicCheck;
import net.judah.zenith.wav.Microphone;

@Component
public class ImageView extends JPanel implements MicCheck {

	public static final String[] MODELS = {"dall-e-3", "dall-e-2"};
	private static final String SIZE_MODEL = MODELS[0];
	public static final String[][] SIZES = {
		{"1024x1024", "1024x1792", "1792x1024"}, // e-3 sizes
		{"1024x1024", "256x256", "512x512"}};	// e-2 sizes
	public static final String PROMPT = "Imagine: ";
	private static final int SEND_HISTORY = 15; // TODO to Config, keeps context window smaller

	@Autowired private OpenAiImageModel client;
	@Autowired private Microphone mic;
	@Autowired private ImageIcon micIcon;
	@Autowired private ImageIcon recordIcon;
	@Autowired private ImageIcon sendIcon;
	@Autowired private ImageIcon send16;

    private Snapshot current;
    private final HistoryText input = new HistoryText(new Dimension(520, 25));
	private final ImageScroll scroll;
    private final ArrayList<ImageMessage> history = new ArrayList<>();
    private final JComboBox<String> model = new JComboBox<>(MODELS);
    private final JComboBox<String> size = new JComboBox<>(SIZES[0]);
    private final JComponent footer = Box.createHorizontalBox();
    private final JButton searchBtn;
    private JButton micBtn;
    // private final JComboBox<String> style = new JComboBox<>(new String[] {"natural", "vivid"});

	public ImageView() {
		scroll = new ImageScroll(()->clear());
        searchBtn = new Btn("", e -> exec());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Common.wrap(new JLabel(PROMPT), input, searchBtn));
		add(new Scroller(scroll));
		add(Box.createVerticalGlue());
	    input.addActionListener(e->exec());
	}
	@PostConstruct
	public void init() {
		footer();
		searchBtn.setIcon(send16);
	}

    private void footer() {

		micBtn = new Btn(micIcon, e->mic());
		footer.add(Box.createHorizontalGlue());
		footer.add(new JLabel(" Model: "));
		footer.add(model);
		footer.add(size);
		// footer.add(style); // spring.io may not currently support
		footer.add(Box.createHorizontalGlue());
		footer.add(micBtn);
		footer.add(new Btn(sendIcon, e->exec()));

		model.addActionListener(l->{
			size.removeAllItems();
			for (String s : SIZES[model.getSelectedIndex()])
				size.addItem(s);
			// size.setSelectedIndex(0);
			// style.setVisible(model.getSelectedItem().equals(SIZE_MODEL));
		});
		model.setSelectedItem(SIZE_MODEL);

		add(footer);
    }

	public void clear() {
		input.setText("");
		input.grabFocus();
	}

	public void mic() {
		boolean recording = mic.toggle(this);
		if (recording)
			input.setText("Recording started...");
		else
			input.setText("Processing audio...");
		micBtn.setIcon(recording ? recordIcon : micIcon);
	}

	public void exec() {
		String query = input.acquire();
		if (query.isBlank())
			return;
	    input.setText("Calculating...");

	    int width = 1024;
		int height = 1024;
		String[] split = ("" + size.getSelectedItem()).split("x");
		try {
			width = Integer.parseInt(split[0]);
			height = Integer.parseInt(split[1]);
		} catch (Throwable t) { System.out.println(t.getMessage()); }

		ImageOptions options = ImageOptionsBuilder.builder()
	        .withModel("" + model.getSelectedItem())
	        .withWidth(width)
	        .withHeight(height)

	        .build();

		history.add(new ImageMessage(query));
        int size = history.size();
        List<ImageMessage> messages = size <= SEND_HISTORY ?
        		new ArrayList<>(history) :
        		new ArrayList<>(history.subList(size - SEND_HISTORY, size));

	    current = new Snapshot(query, options, System.currentTimeMillis());
	    ImageWidget widget = scroll.show(current);
	    new Thread(()->{
		    ImageResponse response = client.call(new ImagePrompt(messages, options));
		    String imageUrl = response.getResult().getOutput().getUrl();
		    input.setText("Retreiving...");
		    if (Props.autoSave(AutoSave.Images))
		    	widget.downloadAndSave(imageUrl, current.getLocation());
		    else
		    	widget.downloadImage(imageUrl);
	    }).start();
	}

	@Override
	public void transcribed(String result) {
		input.setText(result);
		if (Props.isAutoQuery())
			exec();
		else
			input.grabFocus();
	}

	@Override
	public void micDrop(Throwable t) { t.printStackTrace(); }

}
