package net.judah.zenith.model;

import static net.judah.zenith.swing.Common.date;
import static net.judah.zenith.swing.Common.time;

import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;

import reactor.core.publisher.Flux;


public record Encounter(ChatCompletionRequest request, Flux<ChatCompletionChunk> flux, long start) implements Contact {

	public Encounter(ChatCompletionRequest request, Flux<ChatCompletionChunk> flux) {
		this(request, flux, System.currentTimeMillis());
	}

	@Override
	public String query() {
		return request.messages().get(0).content();
	}

	public float temperature() {
		return request.temperature();
	}

	@Override
	public String model() {
		return request.model();
	}

	@Override
	public String info() {
		StringBuilder sb = new StringBuilder();
		sb.append("model: ").append(model()).append(NL);
		sb.append("temp: ").append(temperature()).append(NL);
		sb.append("date: ").append(date(start())).append(NL);
		sb.append("time: ").append(time(start())).append(NL);
		// sb.append("seconds: ").append(seconds()).append(NL);
		// sb.append("tokens: ").append("TODO").append(NL);
		return sb.toString();
	}

}
