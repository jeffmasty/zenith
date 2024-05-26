package net.judah.zenith.embed;

import static net.judah.zenith.Zenith.DATA_DIR;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.Voice;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.judah.zenith.Zenith;
import net.judah.zenith.audio.Text2Speech;
import net.judah.zenith.chat.Settings;
import net.judah.zenith.chat.TextScroll;
import net.judah.zenith.model.Interaction;
import net.judah.zenith.swing.Common;
import net.judah.zenith.swing.Common.Btn;
import net.judah.zenith.swing.HistoryArea;
import net.judah.zenith.swing.Scroller;
import net.judah.zenith.wav.MicCheck;
import net.judah.zenith.wav.Microphone;
import net.judah.zenith.wav.WavPlayer;

@Component 
public class EmbedView extends JPanel implements MicCheck {
	public static final int PROMPT_WIDTH = 485;
	public static final String[] MODELS = {"text-embedding-3-small", "text-embedding-3-large"};
    
	private static final int TABLE_CELLS = 4;
    private static final int INPUT = 6;
	private static final Dimension INPUT_SIZE = new Dimension(PROMPT_WIDTH, 75);
	
	public static final String SYS_INIT = 
			"You are a helpful assistant.\r\n" +
	        "Use only the following information.\r\n" +
	        "Do not use other information.\r\n" + 
	        "Respond 'Incognitum!', if you do not know.\r\n";

	@Autowired private Embedder rag;
	@Autowired private Microphone mic;
	@Autowired private Text2Speech text2Speech;
	
	private final TextScroll scroll;
  	private final HistoryArea input = new HistoryArea();
  	private final HistoryArea sysInput = new HistoryArea();
    private Interaction inProgress;
	private JPanel top = new JPanel();
	private final Settings footer = new Settings(MODELS, false);
    private final FileTableModel filesModel = new FileTableModel();
    private final JTable files ;
    private final JTextField sessionName = new JTextField(27);
    private JTextArea target = input;
    
	public EmbedView() {
		scroll = new TextScroll(()->endStream());
		files = new JTable(filesModel);
		input.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (!e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
					exec(input.acquire());
					e.consume(); }}});
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		top();
		add(top);
		add(new Scroller(scroll));
		add(Box.createVerticalGlue());
		add(footer);
	}
	
	private void top() {
		
		top.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		top.add(new JLabel("Session"));
        gbc.gridheight = 2;
        gbc.gridy = 5;
        JComponent lbl = Box.createVerticalBox();
        lbl.add(new JLabel("<html><center>System<br/>Prompt</center></html"));
        lbl.add(new Btn("Record", e->mic(sysInput)));
        top.add(Common.wrap(lbl), gbc);
        
        gbc.gridy = 7;
        lbl = Box.createVerticalBox();
        lbl.add(new JLabel("<html><center>User<br/>Query</center></html"));
        lbl.add(new Btn("Record", e->mic(input)));
        top.add(Common.wrap(lbl), gbc);
        gbc.gridheight = 1;
        
        JComponent btns = Box.createVerticalBox();
        btns.add(new Btn("  Run  ", e->exec(input.getText())));
        btns.add(new Btn("  Add  ", e->addFiles()));
        // btns.add(new Btn(" Clear ", e->clearfiles())); TODO 
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = TABLE_CELLS;
        top.add(btns, gbc);
        gbc.gridheight = 1; //
        
        // center load/save session
        gbc.gridwidth = INPUT;
        gbc.gridy = 0; gbc.gridx = 1;
        Btn save = new Btn("Save", e->saveSession());
        Btn load = new Btn("Load", e->loadSession());
        top.add(Common.wrap(sessionName, save, load), gbc);
        
		// center files and inputs
        gbc.gridwidth = INPUT;
        gbc.gridy = 1;
        gbc.gridheight = TABLE_CELLS;
        top.add(Common.resize(new JScrollPane(files), PROMPT_WIDTH, 150), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 5;
        top.add(new Holder(sysInput), gbc);
        gbc.gridy = 7;
        top.add(new Holder(input), gbc);
	}
	
	private void addFiles() {
		File[] multi = FileChooser.multi(DATA_DIR, ".txt", "Text files");
		if (multi == null) 
			return;
		for (File file : multi) {
			rag.vectorize(file);
			filesModel.addFile(file);
		}
	}

	private void loadSession() {
		FileChooser.setCurrentDir(DATA_DIR);
		File selected = FileChooser.choose(JFileChooser.FILES_ONLY, Embedder.EXT, "Vector Databases");
		if (selected == null) 
			return;
		rag.loadSession(selected);
		sessionName.setText(selected.getName().replace(Embedder.EXT, ""));
	}

	private void saveSession() {
		String sess = sessionName.getText();
		if (sess.isBlank()) return;
		if (!sess.endsWith(Embedder.EXT))
			sess += Embedder.EXT;
		rag.saveSession(new File(Zenith.DATA_DIR, sess));
	}
	
	@PostConstruct
	public void init() {
		sysInput.setText(SYS_INIT);
		doLayout();
	}
	
	public void endStream() {
		if (footer.voice.isSelected())
			toSpeech(inProgress);
		input.setText(""); // clear();
		input.grabFocus();
	}

	protected void exec(String question) {
    	if (question == null || question.isBlank())
    		return;
    	input.setText("Communicating...");
    	inProgress = rag.query(new EmbedRequest(sysInput.getText(), question, sessionName.getText(), "" + footer.model.getSelectedItem()));
    	scroll.show(inProgress);
	}

	public void toSpeech(Interaction interaction) {
		try {
			File location = interaction.location(DATA_DIR);
			if (false == location.isFile()) { // already created, play from disk
				// to AI
				String text = scroll.getAnswer(interaction);
				SpeechResponse response = Zenith.text2speech(text, 
						(Voice)footer.voices.getSelectedItem(), (float)footer.speed.getSelectedItem(), text2Speech);
				Files.write(Path.of(location.toURI()), 
						response.getResult().getOutput()); 
			}
			WavPlayer.play(location); 
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public void mic(JTextArea target) {
		this.target = target;
		boolean recording = mic.toggle(this);
		if (recording) 
			target.setText("Recording started...");
		else 
			target.setText("Processing audio...");
	}

	@Override
	public void transcribed(String output) {
		target.setText(output);
		exec(input.acquire());
	}

	@Override
	public void micDrop(Throwable t) { t.printStackTrace(); }

	private class Holder extends JScrollPane {
		Holder(JComponent view) {
			super(view);
			Common.resize(this, INPUT_SIZE);
		}
	}

}
