package net.judah.zenith.swing;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.judah.zenith.chat.ChatView;
import net.judah.zenith.embed.EmbedView;
import net.judah.zenith.image.ImageView;
import net.judah.zenith.settings.Props;

@Component
public class MainView extends JFrame {
	public static final String TITLE = "Zenith Intelligence Tracker";


	@Autowired private ChatView text;
	@Autowired private EmbedView rag;
	@Autowired private ImageView dali;
	@Autowired private Props settings;

	private final JTabbedPane tabs = new JTabbedPane();

	public MainView() {
        setTitle(TITLE);
		setSize(Common.WIDE, Common.PAGE);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setContentPane(tabs);
	}

	@PostConstruct
	public void init() {
		tabs.addTab("Ask Zenith", text);
		tabs.addTab("Images", dali);
		tabs.addTab("Documents", rag);
		tabs.addTab("Settings", settings);
		tabs.setSelectedIndex(0);
		doLayout();
		setVisible(true);
	}


}
