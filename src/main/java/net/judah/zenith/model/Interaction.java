package net.judah.zenith.model;

import static net.judah.zenith.swing.Common.date;
import static net.judah.zenith.swing.Common.time;

import java.io.File;
import java.net.URISyntaxException;

import org.springframework.ai.chat.model.ChatResponse;

import net.judah.zenith.embed.EmbedRequest;
import reactor.core.publisher.Flux;

public record Interaction(EmbedRequest request, Flux<ChatResponse> flux) implements Contact {

	public File location(File parent) throws URISyntaxException {
		return new File(parent, request.start() + "." + TYPE.getExtension());
	}

	@Override public String query() {
		return request.query();
	}

	@Override public String model() {
		return request.model();
	}

	@Override
	public String info() {
		StringBuilder sb = new StringBuilder();
		sb.append("model: ").append(model()).append(NL);
		sb.append("date: ").append(date(start())).append(NL);
		sb.append("time: ").append(time(start())).append(NL);
		return sb.toString();
	}

	@Override
	public long start() {
		return request.start();
	}
}
	
