package net.judah.zenith.swing;

import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

public class Icons {

	public static final Icon SAVE = UIManager.getIcon("FileView.floppyDriveIcon");
	public static final Icon NEW_FILE = UIManager.getIcon("FileView.fileIcon");
	public static final Icon DETAILS_VEW = UIManager.getIcon("FileChooser.detailsViewIcon");
	public static final Icon HOME = UIManager.getIcon("FileChooser.homeFolderIcon");

	private static ClassLoader loader = Icons.class.getClassLoader();
	private static HashMap<String, ImageIcon> map = new HashMap<>();

	/** load or retrieve from cache */
    public static ImageIcon get(String name) {
    	if (map == null)
    		map = new HashMap<>();
    	if (map.get(name) == null) {
    		try {
    			map.put(name, new ImageIcon(loader.getResource(name)));
    		} catch (Exception e) { e.printStackTrace(); }
    	}
        return map.get(name);
    }

    public static final Icon PLAYING = get("speakersOn.png");
	public static final Icon SPEAKERS = get("speakers.png");
	public static final Icon SEND = get("send.png");
	public static final Icon SEND16 = get("send16.png");
	public static final Icon MIC = get("mic.png");
	public static final Icon RECORD = get("record.png");
	public static final Icon PAPERCLIP = get("paperclip.png");
	public static final Icon ATTACHED = get("attached.png");

}
