package net.judah.zenith.swing;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class Scroller extends JScrollPane{

	private static final int YRANGE = Common.PAGE - 75;
	private static final Dimension MAX = new Dimension(Common.WIDE - 10, YRANGE);

	public Scroller(Component view) {
		super(view);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		setPreferredSize(MAX);
		setMaximumSize(MAX);
		view.setMinimumSize(new Dimension(Common.WIDE - 25, YRANGE));
		getVerticalScrollBar().setUnitIncrement(60);
	}

}
