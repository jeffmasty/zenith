package net.judah.zenith.chat;

import javax.swing.JTextArea;

public class AiText extends JTextArea {
	
	public AiText() {
		super(2, 44);
		setLineWrap(true);
    	setWrapStyleWord(true);
    	setEditable(false);
    	// ((DefaultCaret)getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}
}
