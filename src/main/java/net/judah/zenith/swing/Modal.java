package net.judah.zenith.swing;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;

public class Modal extends JDialog {

	private static Modal instance;

	public Modal(JComponent view, Dimension size) {
		instance = this;
		setModal(true);
        setSize(size);
        if (view.getName() != null)
        	setTitle(view.getName());
		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
			instance = null;
		}});
		add(view);
        setVisible(true);
	}

	public static void close() {
		if (instance != null) {
			instance.setVisible(false);
			instance.dispose();
			instance = null;
		}
	}

}
