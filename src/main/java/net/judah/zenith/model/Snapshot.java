package net.judah.zenith.model;

import static net.judah.zenith.swing.Common.date;
import static net.judah.zenith.swing.Common.time;

import java.io.File;

import org.springframework.ai.image.ImageOptions;

import net.judah.zenith.settings.Folder;
import net.judah.zenith.settings.Props;

public record Snapshot(String query, ImageOptions settings, long start) {

	public Snapshot(String query, ImageOptions settings, String imageUrl) {
		this(query, settings, System.currentTimeMillis());
	}

	public static final String EXT = ".png";

	public String info() {
		String NL = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("model: ").append(settings.getModel()).append(NL);
		sb.append("date: ").append(date(start())).append(NL);
		sb.append("time: ").append(time(start())).append(NL);
		sb.append("size: ").append(settings.getWidth()).append("x").append(settings.getHeight()).append(NL);
		// sb.append("seconds: ").append(seconds()).append(NL);
		// sb.append("tokens: ").append( _ ).append(NL);
		return sb.toString();
	}

	public File getLocation(String imageName) {
		if (imageName.endsWith(EXT) == false)
			imageName = imageName + EXT;
		return new File(Props.getFolder(Folder.IMAGES), imageName);
	}

	public File getLocation() {
		return getLocation(Long.toString(start));
	}

}
