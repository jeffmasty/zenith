package net.judah.zenith.embed;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public enum EmbedType {

	SMALL("text-embedding-3-small", new Integer[] {256, 512, 1024, 1536}),
	LARGE("text-embedding-3-large", new Integer[] {256, 512, 1024, 1536, 2048, 3072});

	private final String model;
	private final Integer[] dimensions;

	public static EmbedType match(String model) {
		for (EmbedType t : values())
			if (t.model.equals(model))
				return t;
		return null;
	}

}
