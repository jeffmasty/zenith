package net.judah.zenith;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Insets;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import net.judah.zenith.audio.AudioAiClone;
import net.judah.zenith.swing.MainView;
import net.judah.zenith.wav.Microphone;
import net.judah.zenith.wav.WavPlayer;

@SpringBootApplication
public class Zenith {

	public static void main(String[] args) {
        new SpringApplicationBuilder(Zenith.class)
        	.headless(false)
        	.web(WebApplicationType.NONE)
        	.run(args);
 		EventQueue.invokeLater(() -> new MainView());
	}
}

@Configuration
class ZenithConfig {

	@Value("${spring.ai.openai.api-key}") String apiKey;

	@Bean OpenAiApi getOpenAiApi() {
		return new OpenAiApi(apiKey);
	}

	@Bean AudioAiClone getAudioAiClone() {
		return new AudioAiClone(apiKey);
	}

	@Bean OpenAiImageModel imageModel() {
	  return new OpenAiImageModel(new OpenAiImageApi(apiKey));
	}

	@Bean Microphone getMicrophone() {
		return new Microphone();
	}

	@Value("classpath:mic.png")
	@Bean ImageIcon micIcon(Resource imageMic) { try {
		return new ImageIcon(ImageIO.read(imageMic.getInputStream()));
		} catch (Exception e) { e.printStackTrace(); return null;} }
	@Value("classpath:record.png")
	@Bean ImageIcon recordIcon(Resource imageRecord) { try {
		return new ImageIcon(ImageIO.read(imageRecord.getInputStream()));
		} catch (Exception e) { e.printStackTrace(); return null;} }
	@Value("classpath:send.png")
	@Bean ImageIcon sendIcon(Resource imageSend) { try {
		return new ImageIcon(ImageIO.read(imageSend.getInputStream()));
		} catch (Exception e) { e.printStackTrace(); return null;} }
	@Value("classpath:send16.png")
	@Bean ImageIcon send16(Resource imageSend16) { try {
		return new ImageIcon(ImageIO.read(imageSend16.getInputStream()));
		} catch (Exception e) { e.printStackTrace(); return null;} }
	@Value("classpath:speakers.png")
	@Bean ImageIcon speakersIcon(Resource imageSpeakers) { try {
		return new ImageIcon(ImageIO.read(imageSpeakers.getInputStream()));
		} catch (Exception e) { e.printStackTrace(); return null;} }

	@Value("classpath:speakersOn.png")
	@Bean WavPlayer getWavPlayer(Resource speakersOn) {
		try {return new WavPlayer(
				new ImageIcon(ImageIO.read(speakersOn.getInputStream())));}
		catch (Exception e) { e.printStackTrace(); return null;}
	}

	static {
        try {
			UIManager.setLookAndFeel ("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.put("nimbusBlueGrey", new Color(190, 190, 180));
			UIManager.getLookAndFeel().getDefaults().put("Button.contentMargins", new Insets(4, 5, 4, 5));
		} catch (Exception e) { e.printStackTrace();}
	}

}
