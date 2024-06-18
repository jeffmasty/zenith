package net.judah.zenith.wav;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;

import lombok.Getter;
import net.judah.zenith.settings.Audio;
import net.judah.zenith.settings.Props;
import net.judah.zenith.swing.Icons;

public class WavPlayer implements Runnable {

	@Getter private static WavPlayer instance = new WavPlayer();

	private SourceDataLine line;
	private AudioInputStream in;
	private AudioFormat format;
	private JButton speakers;

	private WavPlayer() {
		instance = this;
	}

	/**  On Ubunu, modified /usr/lib/jvm/[version]/conf/sound.properties, added
	 *	javax.sound.sampled.Clip=com.sun.media.sound.DirectAudioDeviceProvider
	 *	javax.sound.sampled.Port=com.sun.media.sound.PortMixerProvider
	 *	javax.sound.sampled.SourceDataLine=com.sun.media.sound.DirectAudioDeviceProvider
	 *	javax.sound.sampled.TargetDataLine=com.sun.media.sound.DirectAudioDeviceProvider	*/
	public void play(File file, JButton speakers) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (in != null)
			internalStop();
		in = AudioSystem.getAudioInputStream(file);
        format = getOutFormat(in.getFormat());
    	line = (SourceDataLine) AudioSystem.getLine(new Info(SourceDataLine.class, format));
    	this.speakers = speakers;
    	new Thread(this).start();
	}

	@Override
	public void run() {
		if (line == null || format == null || in == null)
			return;
		try {
			line.open(format);
			speakers.setIcon(Icons.PLAYING);

			line.start();
            setVolume(Integer.parseInt(Props.get(Audio.Volume.key, "80"))); // (0.0 is max volume, -80.0 is min volume)

            byte[] buffer = new byte[512];
	    	int bytesRead;
	    	try {
		    	while (in != null && line.isOpen() && (bytesRead = in.read(buffer, 0, buffer.length)) != -1)
		    		line.write(buffer, 0, bytesRead);
	    	} catch (Exception e) {e.printStackTrace(); }
		    internalStop();

		} catch (Exception e) { e.printStackTrace(); }
	}

	/** logarithmic
	 * @param slider 0 to 100 */
	public void setVolume(int slider) {
		if (isPlaying() == false)
			return;
		if (slider < 0 || slider > 100)
			return;
		FloatControl vol = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
		if (vol == null)
			return;
        float percent = slider * 0.01f;
        float logMin = vol.getMinimum();
        float logMax = vol.getMaximum();
        float logValue = logMin + percent * (logMax - logMin);
        vol.setValue(logValue);
	}

	private void internalStop() {
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
        speakers.setIcon(Icons.SPEAKERS);
	}

	public static void stop() {
		instance.internalStop();
	}
	public boolean isStopped() {
		return in == null || line == null || !line.isOpen();
	}

	public static boolean isPlaying() {
		return !instance.isStopped();
	}

    public static AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();
        final float rate = inFormat.getSampleRate();

        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, 2, rate, false);
    }

    /** print information about system mixers */
    public static void mixerInfo() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            System.out.println("Name: " + info.getName());
            System.out.println("Description: " + info.getDescription());
            System.out.println("Vendor: " + info.getVendor());
            System.out.println("Version: " + info.getVersion());
            System.out.println();
        }
    }

}
