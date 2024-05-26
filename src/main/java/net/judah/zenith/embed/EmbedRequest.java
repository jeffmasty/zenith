package net.judah.zenith.embed;

public record EmbedRequest (String sysPrompt, String query, String session, String model, long start) {

	public EmbedRequest(String sysPrompt, String query, String session, String model) {
		this(sysPrompt, query, session, model, System.currentTimeMillis());
	}
	
	
}
