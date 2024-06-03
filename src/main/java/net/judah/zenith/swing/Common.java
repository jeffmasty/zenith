package net.judah.zenith.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public interface Common {

	int WIDE = 666;
	int PAGE = 777;
	int LINE = 24;
	int STRUT = 9;

	DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(TimeZone.getDefault().toZoneId());
	DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yy").withZone(TimeZone.getDefault().toZoneId());

	Font BOLD = new Font("Arial", Font.BOLD, 12);
	Font SMALL = new Font("Arial", Font.PLAIN, 10);
	Dimension BOLD_SZ = new Dimension((int) ((WIDE / 3f)*2), LINE * 2);
	Dimension SETTING_SLIDER = new Dimension(160, LINE * 2);
    CompoundBorder COMPOUND_BORDER = BorderFactory.createCompoundBorder(
    		BorderFactory.createLineBorder(Color.GRAY),
    		new EmptyBorder(3, 9, 3, 9));

	String SEND = " Send ";

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

	static String datetime(long time) {
		return date(time) + " " + time(time);
	}

	public class BoldLbl extends JLabel {
		public BoldLbl(String query) {
			super(query, JLabel.LEFT);
			setFont(Common.BOLD);
			Common.resize(this, BOLD_SZ);
		}
	}

	class BorderLbl extends JLabel {
		public BorderLbl(String lbl) {
		    super(lbl, JLabel.CENTER);
		    setBorder(COMPOUND_BORDER);
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

    record Dual(File json, File db) { }

    String AUTOMAGIC = " will automatically be saved";

}
