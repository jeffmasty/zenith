package net.judah.zenith;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Insets;

import javax.swing.UIManager;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import net.judah.zenith.settings.StaticProperties;
import net.judah.zenith.swing.MainView;

@SpringBootApplication
public class Zenith {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Zenith.class)
			.properties("spring.ai.openai.api-key=" + StaticProperties.getInstance().getApiKey())
        	.sources(ZenithConfig.class)
        	.web(WebApplicationType.NONE)
        	.headless(false)
        	.run(args);
 		EventQueue.invokeLater(() -> new MainView());
	}

	static {
        try {
			UIManager.setLookAndFeel ("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.put("nimbusBlueGrey", new Color(190, 190, 180));
			UIManager.getLookAndFeel().getDefaults().put("Button.contentMargins", new Insets(4, 5, 4, 5));
		} catch (Exception e) { e.printStackTrace();}
	}
}

