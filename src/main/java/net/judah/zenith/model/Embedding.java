package net.judah.zenith.model;

import static net.judah.zenith.swing.Common.date;
import static net.judah.zenith.swing.Common.time;

import org.springframework.ai.chat.model.ChatResponse;

import net.judah.zenith.embed.EmbedRequest;
import reactor.core.publisher.Flux;

public record Embedding(EmbedRequest request, Flux<ChatResponse> flux) implements Contact {

	@Override public String query() {
		return request.query();
	}

	@Override public String model() {
		return "small";
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

