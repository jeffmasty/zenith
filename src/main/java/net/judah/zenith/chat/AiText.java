package net.judah.zenith.chat;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextArea;

public class AiText extends JTextArea {

	public AiText(TextScroll parent) {
		super(2, 44);
		setLineWrap(true);
    	setWrapStyleWord(true);
    	setEditable(false);
    	addMouseListener(new MouseAdapter() {
    		@Override public void mousePressed(MouseEvent e) {
    			parent.mouseClick(e);
    		}});
    	// ((DefaultCaret)getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}
}
