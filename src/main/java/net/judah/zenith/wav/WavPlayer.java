package net.judah.zenith.wav;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**  On Ubunu, modified /usr/lib/jvm/[version]/conf/sound.properties, added  
 *	javax.sound.sampled.Clip=com.sun.media.sound.DirectAudioDeviceProvider
 *	javax.sound.sampled.Port=com.sun.media.sound.PortMixerProvider
 *	javax.sound.sampled.SourceDataLine=com.sun.media.sound.DirectAudioDeviceProvider
 *	javax.sound.sampled.TargetDataLine=com.sun.media.sound.DirectAudioDeviceProvider
 */
public class WavPlayer implements Runnable {
	
	private static SourceDataLine line;
	private static AudioInputStream in;
	private static AudioFormat format;
	
	public static void play(File file) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (in != null)
			stop();
		in = AudioSystem.getAudioInputStream(file);
        format = getOutFormat(in.getFormat());
    	line = (SourceDataLine) AudioSystem.getLine(new Info(SourceDataLine.class, format));
    	new Thread(new WavPlayer()).start();
	}
	
	public static void stop() {
		if (in != null)
	        try { 
	        	in.close(); 
	            in = null;
	        } catch (Exception e) { e.printStackTrace(); }
		if (!line.isOpen())
			return;
        line.drain();
        line.stop();
        line.close();
	}
	
    public static AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();
        final float rate = inFormat.getSampleRate();
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }
 
    
    public static void mixerInfo() {
    	// Get the list of available mixers
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

        // Print information about each mixer
        for (Mixer.Info info : mixerInfos) {
            System.out.println("Name: " + info.getName());
            System.out.println("Description: " + info.getDescription());
            System.out.println("Vendor: " + info.getVendor());
            System.out.println("Version: " + info.getVersion());
            System.out.println();
        }
    }

	@Override
	public void run() {
		if (line == null || format == null || in == null) 
			return;
		try {
			line.open(format);
	        line.start();
	        //stream(in);
	    	byte[] buffer = new byte[512];
	    	int bytesRead;
	    	try {
		    	while (in != null && line.isOpen() && (bytesRead = in.read(buffer, 0, buffer.length)) != -1) 
		    		line.write(buffer, 0, bytesRead);
	    	} catch (Exception e) {e.printStackTrace(); }
		    stop();

		} catch (Exception e) { e.printStackTrace(); }	
	}

	public static boolean isPlaying() {
		return in != null;
	}
    

}
