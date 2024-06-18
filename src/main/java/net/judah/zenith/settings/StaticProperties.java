package net.judah.zenith.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

import lombok.Getter;

public class StaticProperties extends Properties {

	@Getter private static final StaticProperties instance = new StaticProperties();

	public static final String PROPERTIES_FILE = "zenith.properties";
	public final File propertiesFile = new File(System.getProperty("user.dir"), PROPERTIES_FILE);

	private StaticProperties() {
        try (InputStream input = new FileInputStream(propertiesFile)) {
            load(input);
			input.close();
        } catch (IOException ex) {
	        System.out.println("Missing properties, going for backup..");
        	try {
        		load(StaticProperties.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE));
        		save();
        	} catch (IOException ex2) {ex2.printStackTrace();}
        }
        String apiKey = getProperty("apiKey");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = JOptionPane.showInputDialog(null, "OpenAI API key not found in properties file. Please enter:");
            if (apiKey == null || apiKey.isEmpty())
                throw new IllegalArgumentException("API key is required.");
            setApiKey(apiKey);
        }
	}

    public void save() {
        try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
            store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public String getApiKey() {
		return getProperty("apiKey");
	}

	public void setApiKey(String apiKey) {
        setProperty("apiKey", apiKey);
        save();
	}

	public String getUsername() {
		String user = getProperty("username");
		if (user == null || user.isBlank()) {
            user = JOptionPane.showInputDialog(null, "Please enter a name I may refer to you as:");
            if (user == null || user.isEmpty())
            	return null;
            setProperty("username", user);
            save();
		}
		return user;
	}


}
