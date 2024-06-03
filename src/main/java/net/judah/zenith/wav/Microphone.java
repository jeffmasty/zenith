package net.judah.zenith.wav;

import java.io.File;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;

import lombok.Getter;
import lombok.Setter;
import net.judah.zenith.settings.Folder;
import net.judah.zenith.settings.Props;

/**https://www.codejava.net/coding/capture-and-record-sound-into-wav-file-with-java-sound-api
 *
 * A sample program is to demonstrate how to record sound in Java
 * author: www.codejava.net */
public class Microphone implements Runnable {

	private final File AUDIO_QUERY = new File(Props.getFolder(Folder.AUDIO), "Query.wav");

	public static final float SAMPLE_RATE = 16000;
    /** format of audio file */
    public static final Type TYPE = Type.WAVE;
    /** max recording duration, in milliseconds */
    public static final long MAX_RECORD_TIME = 60000;  // 1 Minute

	@Autowired private OpenAiAudioTranscriptionModel transcriber;

    /** path of the wav file */
    @Getter @Setter private File wavFile;
    /** the line from which audio data is captured */
    private TargetDataLine line;
    @Getter @Setter private AudioFormat format;

    /** Defines the audio format */
    public static AudioFormat defaultAudioFormat() {
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(
        		SAMPLE_RATE, sampleSizeInBits, channels, signed, bigEndian);
    }

	public boolean toggle(MicCheck listener) {
		if (line == null) {
			startRecording();
			return true;
		}
		else {
			endRecording(listener);
			return false;
		}
	}

    /** Captures the sound and record into a WAV file */
    @Override
	public void run() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start(); //System.out.println("Start capturing...");

            AudioInputStream ais = new AudioInputStream(line);
            AudioSystem.write(ais, TYPE, wavFile);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void startRecording() {
		if (wavFile == null)
			wavFile = AUDIO_QUERY;
		if (format == null)
			format = defaultAudioFormat();
		new Thread(this).start();
    }

    /** Closes the target data line to finish capturing and recording */
    private void endRecording(MicCheck listener) {
    	if (line == null)
    		return;
        line.stop();
        line.close();
        line = null;

		try {
			final FileUrlResource url = new FileUrlResource(wavFile.getAbsolutePath());
    		new Thread(() -> listener.transcribed(transcriber.call(url))).start();
		} catch (Throwable t) { listener.micDrop(t); }

    }

}