package net.judah.zenith.swing;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

//TODO better layout manager
public abstract class Scroll extends JPanel implements Iterable<JPanel>  {
	
	protected final Vector<JPanel> chats = new Vector<>();
	protected final Runnable onEndStream;

	public Scroll(Runnable onEndStream) {
		this.onEndStream = onEndStream;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	protected void populate() {
		removeAll();
		invalidate();
		Iterator<JPanel> it = iterator();
		while(it.hasNext()) 
			add(it.next());
		add(Box.createVerticalGlue());
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

	public final void flush(JPanel e) {
		chats.remove(e);
		populate();
	}
	

}
