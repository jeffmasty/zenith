package net.judah.zenith.swing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JTextArea;

import net.judah.zenith.wav.WavPlayer;

public class HistoryArea extends JTextArea {

	private Vector<String> history = new Vector<>();
    private int caret;

    public HistoryArea(String text) {
    	this();
    	append(text);
    }

	public HistoryArea() {
		setLineWrap(true);
    	setWrapStyleWord(true);
    	addKeyListener(new KeyAdapter() {
             @Override public void keyPressed(KeyEvent e) {
                 if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_UP) {
                	 setText(history(true));
                     e.consume();
                 }
                 else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DOWN) {
                	 setText(history(false));
                     e.consume();
                 }
                 else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                	 if (WavPlayer.isPlaying())
                		 WavPlayer.stop();
                	 else
                		 setText("");
                     e.consume();
                 }
             }
         });
	}

	public void disable(String prompt) {
		setText(prompt);
		setEnabled(false);
	}

	public void clear() {
		setText("");
		setEnabled(true);
		grabFocus();
	}

	/**Puts the text in the history bank and returns it
	 * @returns getText() */
	public String acquire() {
		String input = getText();
		if (input.isBlank())
			return input;
		history.add(input);
		caret = 0;
		return input;
	}

    public String history(boolean up) {
    	caret += up ? -1 : 1;
    	if (caret < 0)
    		caret = history.size() - 1;
    	if (caret >= history.size())
    		caret = 0;
    	return history.get(caret);
    }

}
