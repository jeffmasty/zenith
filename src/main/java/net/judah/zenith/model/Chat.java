package net.judah.zenith.model;

import static net.judah.zenith.swing.Common.date;
import static net.judah.zenith.swing.Common.time;

import org.springframework.ai.openai.OpenAiChatOptions;

import reactor.core.publisher.Flux;

public record Chat(String query, OpenAiChatOptions options, Flux<?> flux, long start) implements Contact {

	public Chat(String query, OpenAiChatOptions options, Flux<?> flux) {
		this(query, options, flux, System.currentTimeMillis());
	}

	@Override
	public String model() {
		return options.getModel();
	}

	public float temperature() {
		return options.getTemperature();
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
