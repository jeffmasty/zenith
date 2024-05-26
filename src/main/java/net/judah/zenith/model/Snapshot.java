package net.judah.zenith.model;

import static net.judah.zenith.swing.Common.date;
import static net.judah.zenith.swing.Common.time;

import org.springframework.ai.image.ImageOptions;

import net.judah.zenith.swing.Common;

public record Snapshot(String query, ImageOptions settings, long start) {
	private static final String NL = Common.NL;
	
	public Snapshot(String query, ImageOptions settings, String imageUrl) {
		this(query, settings, System.currentTimeMillis());
	}
	
	public String info() {
		StringBuilder sb = new StringBuilder();
		sb.append("model: ").append(settings.getModel()).append(NL);
		sb.append("date: ").append(date(start())).append(NL);
		sb.append("time: ").append(time(start())).append(NL);
		sb.append("size: ").append(settings.getWidth()).append("x").append(settings.getHeight()).append(NL);
		// sb.append("seconds: ").append(seconds()).append(NL);
		// sb.append("tokens: ").append( _ ).append(NL); 
		return sb.toString();
	}

}
