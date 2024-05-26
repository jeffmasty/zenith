package net.judah.zenith.image;

import net.judah.zenith.model.Snapshot;
import net.judah.zenith.swing.Scroll;

public class ImageScroll extends Scroll { 
	
	public ImageScroll(Runnable onComplete) {
		super(onComplete);
	}
	
	public ImageWidget show(Snapshot sayCheese) {
		ImageWidget result = new ImageWidget(sayCheese, this, onEndStream);
		chats.add(result);
		populate();
		return result;
	}

}
