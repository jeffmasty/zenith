package net.judah.zenith.swing;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JTextField;

import net.judah.zenith.wav.WavPlayer;

public class HistoryText extends JTextField {

	private ArrayList<String> history = new ArrayList<>();
    private int caret;
	static final Dimension size = new Dimension(Common.WIDE - 100, 30);

	public HistoryText() {
		setPreferredSize(size);
    	addKeyListener(new KeyAdapter() {
    		@Override public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_UP) {
                	 setText(history(true));
                     e.consume();
                 }
                 else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
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

	/**Puts getText() in this input's history bank and returns it
	 * @returns getText() */
	public String acquire() {
		String input = getText();
		if (input.isBlank())
			return input;
		history.add(input);
		caret = 0;
		setText("");
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
