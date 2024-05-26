package net.judah.zenith.image;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.judah.zenith.swing.Common;

public class ImageSettings extends JPanel {
	public static final String[] MODELS = {"dall-e-3", "dall-e-2"};
	public static final String[] SIZES = {"256x256", "512x512", "1024x1024", "1024x1792", "1792x1024"};
	
    public final JComboBox<String> model = new JComboBox<>(MODELS);
    public final JComboBox<String> size = new JComboBox<>(SIZES);
    
    public ImageSettings() {
		model.setSelectedIndex(0);
		size.setSelectedIndex(2);
		
		JPanel menu = new JPanel();
		menu.add(new JLabel(" Model: "));
		menu.add(model);
		menu.add(new JLabel (" Size: "));
		menu.add(size);
    	setLayout(new BorderLayout());
    	add(Common.wrap(menu), BorderLayout.CENTER);
    }
    
}
