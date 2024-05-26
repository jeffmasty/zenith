package net.judah.zenith.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public interface Common {

	int WIDE = 666;
	int PAGE = 777;

	String NL = System.getProperty("line.separator", "\r\n");
	Font BOLD = new Font("Arial", Font.BOLD, 11);
	DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(TimeZone.getDefault().toZoneId());
	DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yy").withZone(TimeZone.getDefault().toZoneId());
	Dimension BOLD_SZ = new Dimension((int) ((WIDE / 3f)*2), 47);
	
	static JComponent resize(JComponent c, Dimension d) {
		c.setMaximumSize(d);
		c.setPreferredSize(d);
		c.setMinimumSize(d);
		return c;
	}

	static JComponent resize(JComponent c, int width, int height) {
		return resize(c, new Dimension(width, height));
	}

	static JPanel wrap(Component... items) {
		JPanel result = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		for (Component p : items)
			result.add(p);
		return result;
	}

	static String time(long time) {
		return TIME_FORMAT.format(Instant.ofEpochMilli(time));
	}
	
	static String date(long time) {
		return DATE_FORMAT.format(Instant.ofEpochMilli(time));
	}
	
	public class BoldLbl extends JLabel {
		public BoldLbl(String query) {
			super(query, JLabel.LEFT);
			setFont(Common.BOLD);
			Common.resize(this, BOLD_SZ);
		}
	}

	class Btn extends JButton {
		
		public Btn(Icon icon, ActionListener l) {
			super(icon);
			addActionListener(l);
		}

		public Btn(Icon icon, ActionListener l, String tip) {
			this(icon, l);
			setToolTipText(tip);
		}
		
		public Btn(String lbl, ActionListener l) {
			super(lbl);
			addActionListener(l);
		}

		public Btn(String lbl, ActionListener actionListener, String tooltip) {
			this(lbl, actionListener);
			setToolTipText(tooltip);
		}

		public Btn(String lbl, ActionListener al, Color red) {
			this(lbl, al);
			setForeground(Color.RED);

		}
	}	
}
