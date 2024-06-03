package net.judah.zenith.swing;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class Widget extends JPanel implements Common {

	final Scroll parent;
	protected final Runnable onEndStream;
	private static final int LBL_LENGTH = 75;

	public Widget(Scroll parent, String query, Runnable onEndStream) {
		setLayout(new BorderLayout());
		this.parent = parent;
		this.onEndStream = onEndStream;
		JComponent top = Box.createHorizontalBox();
		top.add(Box.createHorizontalStrut(10));
		if (query.length() > LBL_LENGTH)
			query = query.substring(0, LBL_LENGTH-1) + "..";
		top.add(new JLabel(query));
		top.add(Box.createHorizontalGlue());
		top.add(btns());
		add(top, BorderLayout.NORTH);

	}

	protected JComponent btns() {
		JComponent btns = Box.createHorizontalBox();
		btns.add(new Btn("Copy", e->copy()));
		btns.add(new Btn("Edit", e->open()));
		btns.add(new Btn("Info", e->info()));
		btns.add(new Btn("Del", e->parent.flush(this)));
		return btns;
	}

	public abstract void copy();
	protected abstract File save();
	protected abstract void open();
	protected abstract void info();
}
