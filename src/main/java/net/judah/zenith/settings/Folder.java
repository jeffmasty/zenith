package net.judah.zenith.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Folder implements Key {

	TEXTS("folder.texts"),
	IMAGES("folder.images"),
	AUDIO("folder.audio"),
	VECTORS("folder.vectors"),;

	@Getter public final String key;

	@Override
	public String getTooltip() {
		return name() + " save folder";
	}

}
