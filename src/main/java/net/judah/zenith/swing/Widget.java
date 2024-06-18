package net.judah.zenith.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class Widget extends JPanel implements Common {

	final Scroll parent;
	private static final int LBL_LENGTH = 85;

	public Widget(Scroll parent, String query) {
		setLayout(new BorderLayout());
		this.parent = parent;

		JLabel question = new JLabel(query, JLabel.LEADING);
		if (query.length() > LBL_LENGTH) {
			question.setToolTipText(query);
			question.setText(query.substring(0, LBL_LENGTH - 1) + "..");
		}
		JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 5));
		wrap.add(question);
		add(wrap, BorderLayout.NORTH);
	}

	protected abstract void copy();
	protected abstract File save();
	protected abstract void open();
	protected abstract void info();

}
