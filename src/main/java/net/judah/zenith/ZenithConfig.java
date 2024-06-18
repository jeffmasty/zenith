package net.judah.zenith;

import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.judah.zenith.audio.AudioAiClone;
import net.judah.zenith.settings.StaticProperties;
import net.judah.zenith.wav.Microphone;

@Configuration
class ZenithConfig {
	@Value("${spring.ai.openai.api-key}")
	final String apiKey = StaticProperties.getInstance().getApiKey();

	@Bean OpenAiApi getOpenAiApi() {
		return new OpenAiApi(apiKey);
	}

	@Bean OpenAiChatModel chatModel() {
		return new OpenAiChatModel(getOpenAiApi());
	};

	@Bean AudioAiClone getAudioAiClone() {
		return new AudioAiClone(apiKey);
	}

	@Bean OpenAiImageModel imageModel() {
	  return new OpenAiImageModel(new OpenAiImageApi(apiKey));
	}

	@Bean Microphone getMicrophone() {
		return new Microphone();
	}

	@Bean OpenAiAudioTranscriptionModel getTranscriber() {
		return new OpenAiAudioTranscriptionModel(new OpenAiAudioApi(apiKey));
	}

}