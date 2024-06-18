package net.judah.zenith.swing;

import net.judah.zenith.model.Contact;

public interface Actionable {

	public void copy(Contact c);
	public void edit(Contact c);
	public void info(Contact c);
	public void delete(Contact c);
	// public void play(Contact c);
}
