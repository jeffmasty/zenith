package net.judah.zenith;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Insets;
import java.io.File;
import java.util.List;

import javax.swing.UIManager;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage.Role;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.Voice;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.knuddels.jtokkit.api.ModelType;

import net.judah.zenith.audio.AudioAiClone;
import net.judah.zenith.audio.AudioRequest;
import net.judah.zenith.audio.Text2Speech;
import net.judah.zenith.model.Encounter;
import net.judah.zenith.swing.MainView;
import net.judah.zenith.wav.Microphone;
import reactor.core.publisher.Flux;


@SpringBootApplication 
public class Zenith {

	static {
        try {
			UIManager.setLookAndFeel ("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.put("nimbusBlueGrey", new Color(220, 220, 210));
            UIManager.getLookAndFeel().getDefaults().put("Button.contentMargins", new Insets(5, 5, 5, 5));
		} catch (Exception e) { e.printStackTrace();}
	}

//	static final String KEY = System.getenv("OPEN_AI_KEY");
	static final String KEY = System.getenv("spring.ai.openai.api-key");
	public static final String TITLE = "Zenith Intelligence Tracker";
	public static final String[] MODELS = {"gpt-4o", "gpt-3.5-turbo", "gpt-4-turbo"}; // gpt-4, dall-e
	public static final File DATA_DIR = new File("data");

	public static void main(String[] args) {
        new SpringApplicationBuilder(Zenith.class)
        	.headless(false)
        	.web(WebApplicationType.NONE)
        	.run(args);
        if (!DATA_DIR.isDirectory()) 
        	DATA_DIR.mkdir();
 		EventQueue.invokeLater(() -> new MainView());
	}

	public static Encounter stream(String query, String model, float temp, OpenAiApi ai) {
		ChatCompletionMessage chatCompletionMessage = new ChatCompletionMessage(query, Role.USER);
		long now = System.currentTimeMillis();
		ChatCompletionRequest request = new ChatCompletionRequest(
				List.of(chatCompletionMessage), "" + model, temp, true);
		Flux<ChatCompletionChunk> flux = ai.chatCompletionStream(request);
		return new Encounter(request, flux, now);
	}

	/** Blocking call to get a speech from AI */
	public static SpeechResponse text2speech(String text, Voice voice, float speed, Text2Speech tts) {
		OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
				.withModel(OpenAiAudioApi.TtsModel.TTS_1.value)
				.withVoice(voice)
				.withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.FLAC) // TODO
				.withSpeed(speed)
				.build();
		return tts.call(new AudioRequest(text, speechOptions));
	}

}

@Configuration
class AppConfig {
	@Bean SimpleVectorStore vectorStore(EmbeddingModel model) {
		EmbeddingModel newClient = new OpenAiEmbeddingModel(new OpenAiApi(Zenith.KEY), MetadataMode.EMBED, 
					OpenAiEmbeddingOptions.builder().withModel(ModelType.TEXT_EMBEDDING_3_SMALL.getName()).build(),
					RetryUtils.DEFAULT_RETRY_TEMPLATE);
		return new SimpleVectorStore(newClient);
	}
	
	@Bean OpenAiApi getOpenAiApi(@Value("${spring.ai.openai.api-key}") String apiKey) {
		return new OpenAiApi(apiKey);
	}
	
	@Bean AudioAiClone getAudioAiClone(@Value("${spring.ai.openai.api-key}") String apiKey) {
		return new AudioAiClone(apiKey);
	}
	
	@Bean Text2Speech getText2Speech(AudioAiClone audioAiClone) {
		return new Text2Speech(audioAiClone);
	}
	
	@Bean OpenAiImageModel imageModel(@Value("${spring.ai.openai.api-key}") String apiKey) {
	  return new OpenAiImageModel(new OpenAiImageApi(apiKey));
	}
	
	@Bean Microphone getMicrophone() {
		return new Microphone();
	}
	
}
