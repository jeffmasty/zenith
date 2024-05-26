package net.judah.zenith.model;

import javax.sound.sampled.AudioFileFormat.Type;

import net.judah.zenith.swing.Common;
import reactor.core.publisher.Flux;

public interface Contact {
	
	String NL = Common.NL;
	
    /** format of audio files */
    Type TYPE = Type.WAVE;

	String query();
	String model();
//	String date();
//	String time();
	String info();
	long start();
	@SuppressWarnings("rawtypes")
	Flux flux(); // ChatCompletionChunk ChatResponse
	Object request();
	
	
}
