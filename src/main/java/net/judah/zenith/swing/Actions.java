package net.judah.zenith.swing;

import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Actions extends JPopupMenu {

	// copy edit (play) info delete

	public Actions(Map<String, ActionListener> show) {
		show.forEach( (lbl, listener)->add(lbl, listener));
	}

	public void add(String lbl, ActionListener action) {
		JMenuItem it = new JMenuItem(lbl);
		it.addActionListener(action);
		add(it);
	}

	public void Init(Widget widget) {

	}

}
