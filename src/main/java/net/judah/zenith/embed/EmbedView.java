package net.judah.zenith.embed;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.judah.zenith.audio.TextToVoice;
import net.judah.zenith.chat.TextScroll;
import net.judah.zenith.model.Embedding;
import net.judah.zenith.settings.Folder;
import net.judah.zenith.settings.Props;
import net.judah.zenith.swing.Common;
import net.judah.zenith.swing.HistoryArea;
import net.judah.zenith.swing.Modal;
import net.judah.zenith.swing.Scroller;
import net.judah.zenith.wav.MicCheck;
import net.judah.zenith.wav.Microphone;
import net.judah.zenith.wav.WavPlayer;

// TODO tabs: Documents, SysPrompt, UserQuery, Filter, MetaData, Chunkie
@Component
public class EmbedView extends JPanel implements MicCheck, Common {

	public static final int PROMPT_WIDTH = 485;
	private static final Dimension INPUT_SIZE = new Dimension(PROMPT_WIDTH, 75);

	public static final String SYS_INIT =
			"You are a helpful assistant.\r\n" +
	        "Use only the following information.\r\n" +
	        "Do not use other information.\r\n" +
	        "Respond 'Incognitum!', if you do not know.\r\n";

	private static final String MIC = "Record ⏺";

	@Autowired private TextToVoice text2Speech;
	@Autowired private Microphone mic;
	@Autowired private WavPlayer player;
	@Autowired private ImageIcon micIcon;
	@Autowired private ImageIcon recordIcon;
	@Autowired private ImageIcon sendIcon;
	@Autowired private ImageIcon speakersIcon;
	@Autowired private ImageIcon send16;

	private final VectorDB database;
    private Embedding current;
    private final SearchRequest sr = SearchRequest.defaults();
	private final TextScroll scroll;
  	private final HistoryArea userPrompt = new HistoryArea();
  	private final HistoryArea sysPrompt = new HistoryArea(SYS_INIT);
	private final JTable documents;
    private final JTextField sessionName = new JTextField(27);
    private final JSlider topK = new JSlider(0, 50, 17);
    private final JSlider threshold = new JSlider(0, 100, 0);
    private final JLabel model = new JLabel("xxxx @ xxxx");
    private JButton micBtn;
    private JButton audio;
    private JTextArea target = userPrompt; // mic transcription target: sys or user prompt

	public EmbedView(VectorDB rag) {
		this.database = rag;
		documents = new JTable(database);
		scroll = new TextScroll(()->endStream());
		userPrompt.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (!e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
					exec();
					e.consume(); }}});

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	@PostConstruct
	public void init() {
		add(titleBar());
		add(grid());
		add(new Scroller(scroll));
		add(Box.createVerticalGlue());
		resetModel(database.getOptions());
		userPrompt.clear();
		add(footer());
	}

	private JComponent titleBar() {
		JComponent box = Box.createHorizontalBox();
		box.add(Box.createHorizontalStrut(62));//?
		JLabel sesh = new JLabel("Session   ", JLabel.CENTER);
		sesh.setFont(Common.BOLD);
		box.add(sesh);
		box.add(sessionName);
		box.add(new Btn("Save", e->saveSession()));
		box.add(new Btn("Load", e->loadSession()));
		box.add(new Btn("New", e->createSession()));
		box.add(Box.createHorizontalStrut(2* STRUT));
		return box;
	}

	private JPanel grid() {
		final Dimension TABLE_SIZE = new Dimension(PROMPT_WIDTH, 90);
		final int TABLE_HEIGHT = 2;
	    final int INPUT = 6;

		JPanel top = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridheight = 2;
        gbc.gridy = TABLE_HEIGHT + 1;
        JComponent lbl = Box.createVerticalBox();
        lbl.add(new JLabel("<html><center>System<br/>Prompt</center></html"));
        lbl.add(new Btn(MIC, e->mic(sysPrompt)));
        top.add(lbl, gbc);

        gbc.gridy = TABLE_HEIGHT + 3;
        lbl = Box.createVerticalBox();
        lbl.add(new JLabel("<html><center>User<br/>Query</center></html"));
        lbl.add(new Btn(MIC, e->mic(userPrompt)));
        top.add(lbl, gbc);
        gbc.gridheight = 1;

        JComponent btns = Box.createVerticalBox();
        JButton add = new Btn(UIManager.getIcon("FileView.fileIcon"), e->addFiles());
        JButton run = new Btn(send16, e->exec());
        add.setText(" Add ");
        run.setText(" Run ");
        run.setHorizontalTextPosition(JButton.LEFT);
        add.setHorizontalTextPosition(JButton.LEFT);
        btns.add(add);
        // TODO not working, not getting doc chunk IDs
        // btns.add(new Btn("Delete", e->deleteFiles()));
        btns.add(run);
        btns.add(Box.createVerticalGlue());

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridheight = 1;
        top.add(btns, gbc);

        gbc.gridheight = 1; //
        gbc.gridx = 0;
        gbc.gridy = 1;
        model.setFont(SMALL);
        top.add(model, gbc);

		// center files and inputs
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = INPUT;
        gbc.gridheight = TABLE_HEIGHT;
        top.add(Common.resize(new JScrollPane(documents), TABLE_SIZE), gbc);

        gbc.gridheight = 2;
        gbc.gridy = TABLE_HEIGHT + 1;
        top.add(new Holder(sysPrompt), gbc);
        gbc.gridy = TABLE_HEIGHT + 3;
        top.add(new Holder(userPrompt), gbc);
        return top;
	}

	private JComponent footer() {
		micBtn = new Btn(micIcon, e->mic(userPrompt));
		audio = new Btn(speakersIcon, e->toggleAudio());
        Hashtable<Integer, JLabel> results = new Hashtable<>();
        results.put(0, new JLabel("1"));
        results.put(10, new JLabel("10"));
        results.put(25, new JLabel("Results"));
        results.put(40,  new JLabel("40"));
        results.put(50, new JLabel("50"));
        topK.setLabelTable(results);
        topK.setPaintLabels(true);
        topK.setMajorTickSpacing(10);
        topK.setPaintTicks(true);
        Hashtable<Integer, JLabel> similarities = new Hashtable<>();
        similarities.put(0, new JLabel("Any"));
        similarities.put(50, new JLabel("Match"));
        similarities.put(100, new JLabel("Exact"));
        threshold.setLabelTable(similarities);
        threshold.setPaintLabels(true);
        threshold.setMajorTickSpacing(20);
        threshold.setPaintTicks(true);

		JComponent result = Box.createHorizontalBox();
        result.add(Box.createHorizontalGlue());
		result.add(Common.resize(topK, SETTING_SLIDER));
		result.add(Box.createHorizontalStrut(STRUT * 2));
        result.add(Common.resize(threshold, SETTING_SLIDER));
        result.add(Box.createHorizontalGlue());

        result.add(audio);
		result.add(micBtn);
        result.add(new Btn(sendIcon, e->exec()));

        result.add(Box.createHorizontalStrut(STRUT));
        return result;
	}

	/** Dialog box */
	private void createSession() {
	    JTextField newSession = new JTextField(15);
	    JComboBox<EmbedType> newModel = new JComboBox<EmbedType>(EmbedType.values());
		JComboBox<Integer> newDimensions = new JComboBox<Integer>(VectorDB.DEFAULT_MODEL.getDimensions());
		newModel.setSelectedItem(VectorDB.DEFAULT_MODEL);
		newDimensions.setSelectedItem(VectorDB.DEFAULT_DIMENSIONS);
		newModel.addActionListener(l->{
			newDimensions.removeAllItems();
			for (int s : ((EmbedType)newModel.getSelectedItem()).getDimensions())
				newDimensions.addItem(s);
			newDimensions.setSelectedItem(VectorDB.DEFAULT_DIMENSIONS);
		});

		Common.resize(newDimensions, 160, 24);
		JPanel result = new JPanel(new GridLayout(3, 2, 1, 1));
		result.add(new JLabel("Session Name"));
		result.add(newSession);
		result.add(new JLabel("Model Size"));
		result.add(newModel);
		result.add(new JLabel("Dimensions"));
		result.add(newDimensions);
		JComponent btns = Box.createHorizontalBox();
		btns.add(Box.createHorizontalGlue());
		btns.add(new Btn("Create", e -> {
			Modal.close();
			OpenAiEmbeddingOptions options = database.createVectorStore(
					newSession.getText(), (EmbedType)newModel.getSelectedItem(),  (int)newDimensions.getSelectedItem());
			resetModel(options);
		}));
		btns.add(new Btn("Cancel", e -> Modal.close()));
		JComponent view = Box.createVerticalBox();
		view.setName("New Documents");
		view.add(Box.createGlue());
		view.add(Common.wrap(result));
		view.add(Box.createGlue());
		view.add(btns);
		new Modal(view, new Dimension(420, 250));
	}

	////////////////////////////////////////////
	private void addFiles() {
		File[] multi = FileChooser.multi(Props.DEFAULT_FOLDER, ".txt", "Text files");
		if (multi == null)
			return;
		for (File file : multi)
			database.addDoc(file);
	}

	private void deleteFiles() { // These are not the Ids you are looking for
		int[] selected = documents.getSelectedRows();
		for (int i = selected.length - 1; i >= 0; i--)
			database.removeDoc(database.getRow(selected[i]));
	}

	private void resetModel(OpenAiEmbeddingOptions options ) {
		if (options == null)
			return;
		sessionName.setText(options.getUser());
		model.setText(EmbedType.match(options.getModel()) + " @ " + options.getDimensions() + "  ");
	}

	private void loadSession() {
		FileChooser.setCurrentDir(Props.getFolder(Folder.VECTORS));
		File selected = FileChooser.choose(JFileChooser.FILES_ONLY, Props.DOTRAG, "Vector Databases");
		if (selected == null)
			return;
		if (database.loadSession(selected))
			resetModel(database.getOptions());
	}

	private void saveSession() {
		String sesh = sessionName.getText();
		if (sesh.isBlank()) {
			sessionName.setText("No Session Name");
			return;
		}
		try {
			database.saveSession(sesh);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void exec() {
		String question = userPrompt.acquire();
    	if (question == null || question.isBlank())
    		return;
    	userPrompt.disable("Communicating...");

    	sr.withSimilarityThreshold(threshold.getValue() * 0.01);
    	sr.withTopK(topK.getValue() + 1);
    	sr.withQuery(question);

    	current = database.query(new EmbedRequest(sysPrompt.getText(), question, sr));
    	scroll.show(current);
	}

	public void endStream() {
		if (TextToVoice.isAutoPlay())
			play(current);
		userPrompt.clear();
	}

	public void play(Embedding interaction) {
		File location = text2Speech.location(interaction.start());
		if (location.isFile() == false)
			text2Speech.callAiAndWrite(scroll.getChatWidget(interaction).getAnswer(), location);
		if (location.isFile()) try {
			player.play(location, audio);
		} catch (Exception e) {e.printStackTrace(); }
	}

	private void toggleAudio() {
		if (current == null)
			return;
		if (WavPlayer.isPlaying())
			WavPlayer.stop();
		else play(current);
	}

	public void mic(JTextArea target) {
		this.target = target;
		boolean recording = mic.toggle(this);
		if (recording)
			target.setText("Recording started...");
		else
			target.setText("Processing audio...");
		micBtn.setIcon(recording ? recordIcon : micIcon);
	}

	@Override
	public void transcribed(String output) {
		target.setText(output);
		if (Props.isAutoQuery())
			exec();
		else
			target.grabFocus();
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
