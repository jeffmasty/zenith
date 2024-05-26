package net.judah.zenith.swing;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class Scroller extends JScrollPane{

	public Scroller(Component view) {
		super(view);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		setPreferredSize(new Dimension(MainView.WIDE - 30,1000));
		getVerticalScrollBar().setUnitIncrement(66);
	}
	
}
