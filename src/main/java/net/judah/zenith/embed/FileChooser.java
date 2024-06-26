package net.judah.zenith.embed;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileChooser {

	static File currentDir = new File(System.getProperty("user.dir"));

	public static void setCurrentDir(File folder) {
		currentDir = folder;
	}
	public static void setCurrentFile(File file) {
		currentDir = file;
	}

	public static File choose(int selectionMode, final String extension, final String description) {
		JFileChooser fc = new JFileChooser();
		if (selectionMode >= 0)
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if (extension != null)
			fc.setFileFilter(new FileFilter() {
				@Override public String getDescription() {
					return description; }
				@Override public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(extension); }
			});

		fc.setCurrentDirectory(new File(System.getProperty("user.home")));
		if (currentDir != null && currentDir.isDirectory())
			fc.setCurrentDirectory(currentDir);
		else if (currentDir != null && currentDir.isFile())
			fc.setSelectedFile(currentDir);
		int result = fc.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = fc.getSelectedFile();
		    currentDir = fc.getCurrentDirectory();
		    return selectedFile == null ? null :
		    	new File(selectedFile.getAbsolutePath()); // JSON doesn't like File subclass
		}
		return null;
	}

	public static File[] multi(File dir, String extension, String description) {
		JFileChooser fc = new JFileChooser();
		if (extension != null)
			fc.setFileFilter(new FileFilter() {
				@Override public String getDescription() {
					return description; }
				@Override public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(extension); }
			});
		fc.setCurrentDirectory(dir);
		fc.setMultiSelectionEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int result = fc.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
		    // currentDir = fc.getCurrentDirectory();
		    return fc.getSelectedFiles() == null ? null : fc.getSelectedFiles();
		}
		return null;
	}

	public static File choose() {
		return choose(0, null, null);
	}
	public static File choose(File folder) {
		setCurrentDir(folder);
		return choose();
	}

}
