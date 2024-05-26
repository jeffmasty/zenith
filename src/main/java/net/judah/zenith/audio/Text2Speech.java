package net.judah.zenith.audio;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.AudioResponseFormat;
import org.springframework.ai.openai.api.common.OpenAiApiException;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioSpeechResponseMetadata;
import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;

public class Text2Speech{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final Float SPEED = 1.0f;
	
	private final OpenAiAudioSpeechOptions defaultOptions;
	private final AudioAiClone audioApi;
	public final RetryTemplate retryTemplate = RetryTemplate.builder()
		.maxAttempts(10)
		.retryOn(OpenAiApiException.class)
		.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
		.build();

	
	public Text2Speech(AudioAiClone audioApi) {
		
		//super(audioApi);
		this.audioApi = audioApi;
		defaultOptions = OpenAiAudioSpeechOptions.builder()
					.withModel(OpenAiAudioApi.TtsModel.TTS_1.getValue())
					.withResponseFormat(AudioResponseFormat.MP3)
					.withVoice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
					.withSpeed(SPEED)
					.build();
	}

	
	
	
	//@Override
	public SpeechResponse call(AudioRequest speechPrompt) {

		return this.retryTemplate.execute(ctx -> {

			AudioAiClone.SpeechRequest speechRequest = createRequestBody(speechPrompt);

			ResponseEntity<byte[]> speechEntity = audioApi.createSpeech(speechRequest);
			var speech = speechEntity.getBody();

			if (speech == null) {
				logger.warn("No speech response returned for speechRequest: {}", speechRequest);
				return new SpeechResponse(new Speech(new byte[0]));
			}

			RateLimit rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(speechEntity);

			return new SpeechResponse(new Speech(speech), new OpenAiAudioSpeechResponseMetadata(rateLimits));

		});
	}

	private AudioAiClone.SpeechRequest createRequestBody(AudioRequest request) {
		OpenAiAudioSpeechOptions options = this.defaultOptions;

		if (request.getOptions() != null) {
			if (request.getOptions() instanceof OpenAiAudioSpeechOptions runtimeOptions) {
				options = this.merge(runtimeOptions, options);
			}
			else {
				throw new IllegalArgumentException("Prompt options are not of type SpeechOptions: "
						+ request.getOptions().getClass().getSimpleName());
			}
		}

		String input = StringUtils.isNotBlank(options.getInput()) ? options.getInput()
				: request.getInstructions().getText();

		AudioAiClone.SpeechRequest.Builder requestBuilder = AudioAiClone.SpeechRequest.builder()
			.withModel(options.getModel())
			.withInput(input)
			.withVoice(options.getVoice())
			.withResponseFormat("wav")
			.withSpeed(options.getSpeed());

		return requestBuilder.build();
	}
	
	private OpenAiAudioSpeechOptions merge(OpenAiAudioSpeechOptions source, OpenAiAudioSpeechOptions target) {
		OpenAiAudioSpeechOptions.Builder mergedBuilder = OpenAiAudioSpeechOptions.builder();

		mergedBuilder.withModel(source.getModel() != null ? source.getModel() : target.getModel());
		mergedBuilder.withInput(source.getInput() != null ? source.getInput() : target.getInput());
		mergedBuilder.withVoice(source.getVoice() != null ? source.getVoice() : target.getVoice());
		mergedBuilder.withResponseFormat(
				source.getResponseFormat() != null ? source.getResponseFormat() : target.getResponseFormat());
		mergedBuilder.withSpeed(source.getSpeed() != null ? source.getSpeed() : target.getSpeed());

		return mergedBuilder.build();
	}
	
}
