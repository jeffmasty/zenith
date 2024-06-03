package net.judah.zenith.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Audio implements Key {

	Voice("voice", "Style of voice response"),
	AutoPlay("voice.autoplay", "Text responses will be converted to audio"),
	HiDef("voice.hd", "Use the Hi-Definition audio library"),
	Speed("voice.speed", "Playback speed"),
	AutoQuery("voice.autoquery", "Transcriptions will not be edited before AI query"),
	Volume("voice.vol", "Audio playback volume");

	@Getter public final String key;
	@Getter public final String tooltip;
}