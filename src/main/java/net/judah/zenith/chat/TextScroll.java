package net.judah.zenith.chat;

import javax.swing.JPanel;

import net.judah.zenith.model.Contact;
import net.judah.zenith.swing.Scroll;

public class TextScroll extends Scroll { 
	
	public TextScroll(Runnable onComplete) {
		super(onComplete);
	}
	
	public void show(Contact transaction) {
		chats.add(new ChatWidget(transaction, this, onEndStream));
		populate();
	}
	
	public String getAnswer(Contact e) {
		for (JPanel panel : chats) 
			if (e == ((ChatWidget)panel).getChat())
				return ((ChatWidget)panel).getAnswer();
		return e.start() + " NOT FOUND";
	}
//	public String getAnswer(Encounter e) {
//		for (JPanel panel : chats) 
//			if (e == ((ChatWidget)panel).getChat())
//				return ((ChatWidget)panel).getAnswer();
//		return e.start() + " NOT FOUND";
//	}

}
