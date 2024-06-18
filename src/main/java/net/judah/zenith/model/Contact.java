package net.judah.zenith.model;

import javax.sound.sampled.AudioFileFormat.Type;

import reactor.core.publisher.Flux;

public interface Contact {

	String NL = System.lineSeparator();

    /** format of audio files */
    Type TYPE = Type.WAVE;

	String query();
	String model();
	String info();
	long start();
	/** ChatCompletionChunk or ChatResponse */
	@SuppressWarnings("rawtypes")
	Flux flux();
//	Object request();

}
