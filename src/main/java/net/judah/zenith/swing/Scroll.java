package net.judah.zenith.swing;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.judah.zenith.chat.ChatWidget;
import net.judah.zenith.image.ImageWidget;
import net.judah.zenith.model.Contact;

//TODO accordion layout manager
public abstract class Scroll extends JPanel implements Iterable<JPanel> {

	protected final Vector<JPanel> chats = new Vector<>();
	private JPopupMenu menu;
	private Optional<Widget> selected;

	public Scroll() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
            	mouseClick(e); }});
	}
	protected abstract Widget show(Contact c);

	public Widget getWidget(Object key) {
		for (JPanel pnl : chats) {
			if (pnl instanceof ChatWidget chat) {
				if (key == chat.getChat())
					return chat;
			}
			else if (pnl instanceof ImageWidget img) {
				if (key == img.getInput())
					return img;
			}
		}
		return null;
	}

	public final void flush(JPanel e) {
		chats.remove(e);
		populate();
	}


	public void mouseClick(MouseEvent e) {
        if (false == SwingUtilities.isRightMouseButton(e))
        	return;
        Point p = e.getPoint();
        if (e.getSource() != this)
			p = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), this);

        if (menu == null)
        	menu = createMenu();

        selected = Optional.ofNullable(null);
        if (getComponentAt(e.getPoint()) instanceof Widget widget) {
        	selected = Optional.of(widget);
            menu.show(Scroll.this, (int)p.getX(), (int)p.getY());
        }
	}

	private final JPopupMenu createMenu() {
		JPopupMenu result = new JPopupMenu();
		result.add(new MyMenu("Copy", e->selected.ifPresent(widget -> widget.copy())));
		result.add(new MyMenu("Edit", e->selected.ifPresent(widget -> widget.open())));
		result.add(new MyMenu("Info", e->selected.ifPresent(widget -> widget.info())));
		// Hide (accordion)
//		if (player != null)
//			result.add(new MyMenu("Play", e->selected.ifPresent(widget -> player.play(file, speakers)widget.info())));
		result.add(new MyMenu("Delete", e->selected.ifPresent(widget -> flush(widget))));
		return result;
	};

	protected void populate() {
		removeAll();
		invalidate();
		Iterator<JPanel> it = iterator();
		while(it.hasNext())
			add(it.next());
		add(Box.createVerticalGlue());
		add(Box.createHorizontalGlue());
		repaint();
	}

	/** @return newest first iterator (reverse iterator) */
	@Override
	public Iterator<JPanel> iterator() {
		return new IterateIt<JPanel>(chats);
	}

	// Single-use Reverse Iterator, original copied from java.util.Array
	@SuppressWarnings("hiding")
	private class IterateIt<JPanel> implements Iterator<JPanel> {
	    private int cursor;
	    private final Vector<JPanel> list;

	    IterateIt(Vector<JPanel> vect) {
	        this.list = vect;
	        cursor = list.size() -1;
	    }
	    @Override public boolean hasNext() {
	        return cursor >= 0;
	    }

	    @Override public JPanel next() {
	        int i = cursor;
	        if (i < 0)
	        	throw new NoSuchElementException();
	        cursor = i - 1;
	        return list.get(i);
	    }
	}


	private class MyMenu extends JMenuItem {
		MyMenu(String lbl, ActionListener l) {
			super(lbl);
			addActionListener(l);
		}
	}


}

/*
The most common approach to creating an accordion-like interface in Swing is by using a `JPanel` with a `BoxLayout` or `GridBagLayout`, combined with `JToggleButton` or `JButton` components to act as the headers for each section. When a header button is clicked, it expands or collapses the associated content panel.
Here's a simple example to illustrate how you might create an accordion-style interface in Swing:

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccordionExample {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AccordionExample::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Accordion Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        addAccordionSection(mainPanel, "Section 1", new JLabel("Content for Section 1"));
        addAccordionSection(mainPanel, "Section 2", new JLabel("Content for Section 2"));
        addAccordionSection(mainPanel, "Section 3", new JLabel("Content for Section 3"));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    private static void addAccordionSection(JPanel parent, String title, JComponent content) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));

        JToggleButton toggleButton = new JToggleButton(title);
        toggleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionPanel.add(toggleButton);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(content);
        contentPanel.setVisible(false);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sectionPanel.add(contentPanel);

        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPanel.setVisible(toggleButton.isSelected());
                sectionPanel.revalidate();
                sectionPanel.repaint();
            }
        });

        parent.add(sectionPanel);
    }
}
*/
