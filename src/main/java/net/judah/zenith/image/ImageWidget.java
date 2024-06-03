package net.judah.zenith.image;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.springframework.beans.factory.annotation.Autowired;

import net.judah.zenith.model.Snapshot;
import net.judah.zenith.settings.Props;
import net.judah.zenith.swing.Scroll;
import net.judah.zenith.swing.Widget;

public class ImageWidget extends Widget {

	@Autowired Props settings;
	private final Snapshot input;
	private final ImageLabel icon = new ImageLabel();
	private BufferedImage image;
	private long end;

	public ImageWidget(Snapshot snapshot, Scroll parent, Runnable onEndStream) {
		super(parent, snapshot.query(), onEndStream);
		this.input = snapshot;
		add(icon, BorderLayout.CENTER);
	}

	public void downloadAndSave(String imageUrl, File save) {
		try {
			URL url = new URI(imageUrl).toURL();
			image = ImageIO.read(url);
			icon.setIcon(new ImageIcon(image));
			repaint();
			onEndStream.run();
			ImageIO.write(image, "png", save);
		} catch (Throwable t) {t.printStackTrace();}
	}

	public void downloadImage(String imageUrl) {
		try {
			URL url = new URI(imageUrl).toURL();
			image = ImageIO.read(url);
			icon.setIcon(new ImageIcon(image));
			repaint();
			onEndStream.run();
		} catch (Throwable t) {t.printStackTrace();}
	}

	@Override
	protected File save() {
		if (image == null) return null;
		String imageName = JOptionPane.showInputDialog(null, "Save image as...", "img");
		if (imageName == null || imageName.isBlank())
			return null;
		File outputFile = input.getLocation(imageName);
        try {
            ImageIO.write(image, "png", outputFile);
            return outputFile;
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
        return null;
	}

	@Override
	protected void open() {
		File saved = input.getLocation();
		if (saved.isFile() == false)
			saved = save();
		if (saved == null)
			return;
		try {
			Desktop.getDesktop().open(saved);
		} catch (IOException e) { e.printStackTrace(); }
	}

	@Override
	public void copy() {
        TransferableImage transferableImage = new TransferableImage(image);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(transferableImage, null);
	}

	@Override
	protected void info() {
		JOptionPane.showMessageDialog(null, input.info(), "Chat", JOptionPane.INFORMATION_MESSAGE);
	}

	public long millis() {
		return (end - input.start());
	}

	public String seconds() {
		return String.format("%2.03f", millis() * 0.001f);
	}

	static class TransferableImage implements Transferable {
		private final BufferedImage image;

		public TransferableImage(BufferedImage image) {
		    this.image = image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
		    return new DataFlavor[]{DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
		    return DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		    if (!isDataFlavorSupported(flavor)) {
		        throw new UnsupportedFlavorException(flavor);
		    }
		    return image;
		}
	}

}
