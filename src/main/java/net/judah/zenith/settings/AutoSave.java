package net.judah.zenith.settings;

import static net.judah.zenith.swing.Common.AUTOMAGIC;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AutoSave implements Key {

	Texts("autosave.texts", "Texts responses" + AUTOMAGIC),
	Images("autosave.images", "Generated Images" + AUTOMAGIC),
	Audio("autosave.audio", "Audio responses" + AUTOMAGIC),
	Vectors("autosave.vectors", "Database changes" + AUTOMAGIC),
	Settings("autosave.settings", "These Settings" + AUTOMAGIC);

	@Getter public final String key;
	@Getter public final String tooltip;
}
