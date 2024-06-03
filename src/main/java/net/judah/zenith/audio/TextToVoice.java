package net.judah.zenith.audio;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.Voice;
import org.springframework.ai.openai.api.OpenAiAudioApi.TtsModel;
import org.springframework.ai.openai.api.common.OpenAiApiException;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioSpeechResponseMetadata;
import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import net.judah.zenith.settings.Audio;
import net.judah.zenith.settings.Props;
import net.judah.zenith.settings.Folder;

@Component
public class TextToVoice {
	//private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final Voice DEFAULT_VOICE = Voice.NOVA;
	public static final Float[] SPEEDS = {0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f};

	private final AudioAiClone audioApi;

	public final RetryTemplate retryTemplate = RetryTemplate.builder()
			.maxAttempts(10)
			.retryOn(OpenAiApiException.class)
			.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
			.build();

	public TextToVoice(AudioAiClone audioApi) {
		this.audioApi = audioApi;
	}

	public Voice getVoice() {
		return Voice.valueOf(Props.get(Audio.Voice.key, DEFAULT_VOICE.name()));
	}

	public static boolean isAutoPlay() {
		return Boolean.valueOf(Props.get(Audio.AutoPlay.key, "false"));
	}

	public boolean isHD() {
		return Boolean.valueOf(Props.get(Audio.HiDef.key, "false"));
	}

	public float getSpeed() {
		return Float.valueOf(Props.get(Audio.Speed.key, "1.0f"));
	}

	public File location(long start) {
		StringBuilder sb = new StringBuilder().append(start).append("-");
		sb.append(getVoice()).append("-").append(getSpeed());
		sb.append(isHD() ? "HD" : "").append(".wav");
		return new File(Props.getFolder(Folder.AUDIO), sb.toString());
	}

	public void callAiAndWrite(String text, File toDisk) {
		try {
			SpeechResponse response = //call(text);
					retryTemplate.execute(ctx -> {
						AudioAiClone.SpeechRequest speechRequest =
								AudioAiClone.SpeechRequest.builder()
								.withInput(text)
								.withModel(isHD() ? TtsModel.TTS_1_HD.value : TtsModel.TTS_1.value)
								.withVoice(getVoice())
								.withResponseFormat("wav")  // The Workaround
								.withSpeed(getSpeed())
								.build();
						ResponseEntity<byte[]> speechEntity = audioApi.createSpeech(speechRequest);
						if (speechEntity.getBody() == null) {
							System.err.println("No speech response returned for speechRequest: " + speechRequest);
							return new SpeechResponse(new Speech(new byte[0]));
						}
						RateLimit rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(speechEntity);
						return new SpeechResponse(new Speech(speechEntity.getBody()), new OpenAiAudioSpeechResponseMetadata(rateLimits));
					});
			Files.write(Path.of(toDisk.toURI()),
					response.getResult().getOutput());
		} catch (Exception e) { e.printStackTrace(); }
	}

}
