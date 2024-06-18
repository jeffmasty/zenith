package net.judah.zenith.image;

import net.judah.zenith.model.Contact;
import net.judah.zenith.model.Snapshot;
import net.judah.zenith.swing.Scroll;

public class ImageScroll extends Scroll {

	@Override
	public ImageWidget show(Contact c) {
		ImageWidget result = null;
		if (c instanceof Snapshot sayCheese) {
			result = new ImageWidget(sayCheese, this);
			chats.add(result);
			populate();
		}
		return result;
	}

}
