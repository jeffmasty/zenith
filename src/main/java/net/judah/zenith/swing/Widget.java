package net.judah.zenith.swing;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class Widget extends JPanel implements Common {

	final Scroll parent;
	protected final Runnable onEndStream;
	
	public Widget(Scroll parent, String query, Runnable onEndStream) {
		setLayout(new BorderLayout());
		this.parent = parent;
		this.onEndStream = onEndStream;
		JComponent top = Box.createHorizontalBox();
		top.add(new BoldLbl(query));
		top.add(Box.createHorizontalGlue());
		top.add(btns());
		add(top, BorderLayout.NORTH);

	}
	
	protected JComponent btns() {
		JComponent btns = Box.createHorizontalBox();
		btns.add(new Btn("Copy", e->copy())); 
		btns.add(new Btn("Save", e->save()));
		btns.add(new Btn("Info", e->info()));
		btns.add(new Btn("Del", e->parent.flush(this)));
		return btns;
	}
	
	public abstract void copy();
	protected abstract void save();
	protected abstract void info();
}
