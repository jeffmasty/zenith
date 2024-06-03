package net.judah.zenith.embed;

import org.springframework.ai.vectorstore.SearchRequest;

public record EmbedRequest (String sysPrompt, String query, SearchRequest searchRequest, long start) {

	public EmbedRequest(String sysPrompt, String query, SearchRequest sr) {
		this(sysPrompt, query, sr, System.currentTimeMillis());
	}

}
