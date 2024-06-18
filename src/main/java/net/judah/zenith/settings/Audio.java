package net.judah.zenith.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Audio implements Key {

	AutoPlay("voice.autoplay", "Text responses will be converted to audio"),
	AutoQuery("voice.autoquery", "Transcriptions will not be edited before AI query"),
	HiDef("voice.hd", "Use the Hi-Definition audio library"),
	Voice("voice", "Style of voice response"),
	Speed("voice.speed", "Playback speed"),
	Volume("voice.vol", "Audio playback volume");

	@Getter public final String key;
	@Getter public final String tooltip;
}