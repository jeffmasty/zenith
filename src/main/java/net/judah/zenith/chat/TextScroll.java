package net.judah.zenith.chat;

import javax.swing.JPanel;

import net.judah.zenith.model.Contact;
import net.judah.zenith.swing.Scroll;
import net.judah.zenith.swing.Widget;

public class TextScroll extends Scroll {

	@Override
	public Widget show(Contact transaction) {
		ChatWidget result = new ChatWidget(transaction, this);
		chats.add(result);
		populate();
		return result;
	}

	public ChatWidget getChatWidget(Contact e) {
		for (JPanel panel : chats)
			if (e == ((ChatWidget)panel).getChat())
				return ((ChatWidget)panel);
		return null;
	}

}
