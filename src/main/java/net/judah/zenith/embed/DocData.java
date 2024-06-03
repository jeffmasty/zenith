package net.judah.zenith.embed;

import java.util.Map;

import org.springframework.ai.document.Document;

public record DocData(String id, Map<String, Object> metadata) {

	public DocData(Document d) {
		this(d.getId(), d.getMetadata());
	}

}
