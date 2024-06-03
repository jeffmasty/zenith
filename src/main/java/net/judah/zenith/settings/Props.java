package net.judah.zenith.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.Voice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
import net.judah.zenith.audio.TextToVoice;
import net.judah.zenith.swing.Common;
import net.judah.zenith.wav.WavPlayer;

//TODO chat history size, scrolling, width, height, default Embed SysPrompt?
@Component
public class Props extends JPanel implements Common {

	@Autowired
	WavPlayer player;

	/** number of recent chats to send to LLM */
	public static final int HISTORY_SEND = 15;
	public static final String PROPERTIES_FILE = "zenith.properties";
	public static final String DOTJSON = ".json";
	public static final String DOTRAG = ".rag";
	public static final File DEFAULT_FOLDER =
			new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "data");

	private static final File propertiesFile = new File(System.getProperty("user.dir"), PROPERTIES_FILE);
	private static Properties properties = new Properties();
	private final Dimension SZ = new Dimension(210, 27);
	private final JButton saveBtn;
	private final JComponent footer = Box.createHorizontalBox();
	private final ActionListener autoSaver = e->{
		if (autoSave(AutoSave.Settings))
			save();
	};

	/** Load the properties file, setup gui */
	public Props() {
        try (InputStream input = new FileInputStream(propertiesFile)) {
            properties.load(input);
			input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        saveBtn = new Btn("Save", e->save());
        JComponent inner = Box.createVerticalBox();
        inner.add(new Titled("Folders", foldersPnl()));
        inner.add(new Titled("Audio", audioPnl()));
//        inner.add(new Titled("View", viewPnl()));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());
        add(Common.wrap(inner));
        add(Box.createVerticalGlue());
		footer.add(Box.createHorizontalGlue());
        add(footer);
        toggleAutoSave(autoSave(AutoSave.Settings));
	}

	private JComponent foldersPnl() {
		Grid grid = new Grid();
		final GridBagConstraints gbc = grid.getConstraints();
		gbc.gridy = -1;
		for (int i = 0; i < Folder.values().length; i++)
			grid.pair(new Check(AutoSave.values()[i]), new Dir(Folder.values()[i]));
		gbc.gridy += 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;

		JCheckBox autosave = new Check(AutoSave.Settings);
		autosave.addActionListener(l->toggleAutoSave(autosave.isSelected()));
		grid.add(autosave, gbc);
		gbc.gridx += 1;
		grid.add(new JLabel(), gbc);
		gbc.gridx += 1;
		grid.add(new JLabel(), gbc);
		gbc.gridx += 1;
		grid.add(new JLabel(), gbc);

		JPanel result = new JPanel(new BorderLayout());
		result.add(new JLabel("AutoSave", JLabel.LEFT), BorderLayout.NORTH);
		result.add(grid, BorderLayout.CENTER);
		return result;
	}

    private JComponent audioPnl() {
        Audio vox = Audio.Voice;
        Audio fast = Audio.Speed;
        Audio volume = Audio.Volume;
        JComboBox<Voice> voice = new JComboBox<>(Voice.values());
        JComboBox<Float> speed = new JComboBox<>(TextToVoice.SPEEDS);
        JSlider vol = new JSlider(0, 100);

        voice.setSelectedItem(Voice.valueOf(properties.getProperty(vox.key)));
        speed.setSelectedItem(Float.valueOf(properties.getProperty(fast.key)));
        vol.setValue(Integer.valueOf(properties.getProperty(volume.key)));
		voice.addActionListener( listener-> {
			properties.setProperty(vox.key, ((Voice)voice.getSelectedItem()).name());
			autoSaver.actionPerformed(null);
		});
		speed.addActionListener( listener-> {
			properties.setProperty(fast.key, "" + speed.getSelectedItem());
			autoSaver.actionPerformed(null);
		});
		vol.addChangeListener( listener-> {
			int newVol = vol.getValue();
			properties.setProperty(volume.key, "" + newVol);
			player.setVolume(newVol);
			autoSaver.actionPerformed(null);
		});
        Common.resize(vol, SZ);

    	Grid audio = new Grid();
    	final GridBagConstraints gbc = audio.getConstraints();
    	gbc.gridy = 0;
    	gbc.gridx = 0;
    	audio.add(new Check(Audio.AutoPlay), gbc);
    	gbc.gridx = ++gbc.gridx;
    	audio.add(new Check(Audio.AutoQuery), gbc);
    	gbc.gridx = ++gbc.gridx;
    	audio.add(new Check(Audio.HiDef), gbc);
    	gbc.gridx = ++gbc.gridx;
    	audio.add(new JLabel(" "));
    	audio.pair(vox.name(), voice);
    	audio.pair(fast.name(), speed);
    	audio.pair(volume.name(), vol);
		return audio;
    }

    private JComponent viewPnl() {
        Inner view = new Inner();
        // TODO Transaction:
        view.install("Width/Height", Common.wrap(new JTextField(4), new JLabel("/"), new JTextField(4)));
        view.install("AutoScroll", new JCheckBox()); //
        view.install("Accordian", new JCheckBox()); //
        return view;
    }

    private void save() {
        try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
            properties.store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleAutoSave(boolean selected) {
    	if (selected) {
    		footer.removeAll();
    		footer.add(Box.createHorizontalGlue());
    	}
    	else {
    		footer.add(saveBtn);
    		footer.doLayout();
    	}
    	repaint();
    }
    /////////////////////////////////
    public static String get(String key) {
    	return properties.getProperty(key);
    }

    public static Common.Dual getVectors(String sessionName) {
    	File parent = getFolder(Folder.VECTORS);
    	return new Dual(new File(parent, sessionName + DOTJSON), new File(parent, sessionName + DOTRAG));
    }

    public static String get(String key, String onNull) {
    	String result = get(key);
    	if (result == null)
    		return onNull;
    	return result;
    }

    public static boolean isAutoQuery() {
		return "true".equals(get(Audio.AutoQuery.key));
    }

    public static File getFolder(Folder f) {
    	String folder = properties.getProperty(f.key);
    	if (folder == null || folder.isBlank()) {
    		properties.setProperty(f.key, DEFAULT_FOLDER.getAbsolutePath());
    		return DEFAULT_FOLDER;
    	}
    	return new File(folder);
    }

	public static boolean autoSave(AutoSave a) {
		String tOrF = properties.getProperty(a.key);
		return tOrF != null && Boolean.parseBoolean(tOrF) == true;
	}

    ////////////////////////////////////
	private class Grid extends JPanel {
		@Getter GridBagConstraints constraints = new GridBagConstraints();
		Grid() {
			super(new GridBagLayout());
			constraints.insets = new Insets(3, 0, 3, 20);
		}
		void install(JComponent label, JComponent widget, int width) {
			constraints.fill = GridBagConstraints.HORIZONTAL;
    		constraints.gridwidth = 1;
    		constraints.gridx = 0;
    		constraints.gridy = ++constraints.gridy;
			add(label, constraints);
    		constraints.gridx = 1;
    		constraints.gridwidth = width;
    		add(widget, constraints);
		}
		void pair(String label, JComponent widget) {
			install(new JLabel("        " + label, JLabel.LEFT), widget, 2);
		}
		void pair(JComponent label, JComponent widget) {
			install(label, widget, 3);
		}

	}

    private class Inner extends JPanel {
    	Inner() {
    		super(new GridLayout(0, 2));
    	}
    	void install(String label, JComponent var) {
    		add(new JLabel("       " + label));
    		add(var);
    	}
    }

    private class Titled extends JPanel {
    	Titled(String title, JComponent inner) {
    		setBorder(BorderFactory.createTitledBorder(title));
    		add(inner);
    	}
    }

    private class Dir extends Common.BorderLbl{
    	Dir(Folder f) {
    		super(getFolder(f).getAbsolutePath());
    		addMouseListener(new FileManager(this, f));
    		setSize(SZ);
    	}
    }

    private class FileManager extends MouseAdapter {
    	final Folder folder;
    	final JLabel lbl;
    	FileManager(JLabel lbl, Folder f) {
    		folder = f;
    		this.lbl = lbl;
    	}
    	@Override public void mouseClicked(MouseEvent e) {
	        JFileChooser fileChooser = new JFileChooser(getFolder(folder));
	        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        int result = fileChooser.showOpenDialog(getParent());
	        if (result != JFileChooser.APPROVE_OPTION)
	        	return;
	        File selected = fileChooser.getSelectedFile();
	        if (selected.isDirectory() == false && selected.mkdir() == false)
	        	return; // ?
	        System.out.println("Selected folder: " + selected.getAbsolutePath());
	        properties.setProperty(folder.key, selected.getAbsolutePath());
	        lbl.setText(selected.getAbsolutePath());
	        autoSaver.actionPerformed(null);
    	}
    }

    private class Check extends JCheckBox {
    	Check(Key key) {
    		this(key.name(), key.getKey());
    		setToolTipText(key.getTooltip());
    	}
    	Check(String lbl, String key) {
    		super(" " + lbl); // spacer
    		setSelected(Boolean.parseBoolean("" + get(key, "false")));
    		addActionListener(e-> {
    			properties.setProperty(key, Boolean.toString(isSelected()));
    			autoSaver.actionPerformed(e);});
    		setHorizontalTextPosition(SwingConstants.RIGHT);
    		setHorizontalAlignment(SwingConstants.LEFT);
    	}
    }

}
