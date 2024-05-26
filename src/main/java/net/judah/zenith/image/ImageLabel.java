package net.judah.zenith.image;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.judah.zenith.swing.Common;
import net.judah.zenith.swing.MainView;

public class ImageLabel extends JLabel {
	
	private static final int WIDE = MainView.WIDE - 20;
	
	public ImageLabel() {
		Common.resize(this, new Dimension(WIDE, WIDE));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
        ImageIcon icon = (ImageIcon) getIcon();
        if (icon != null) {
            drawScaledImage(icon.getImage(), this, g);
        }
    }
	
	public static void drawScaledImage(Image image, Component canvas, Graphics g) {
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);
         
        double imgAspect = (double) imgHeight / imgWidth;
 
        int canvasWidth = WIDE;// canvas.getWidth();
        int canvasHeight = WIDE;// canvas.getHeight();
         
        double canvasAspect = (double) canvasHeight / canvasWidth;
 
        int x1 = 0; // top left X position
        int y1 = 0; // top left Y position
        int x2 = 0; // bottom right X position
        int y2 = 0; // bottom right Y position
         
        if (imgWidth < canvasWidth && imgHeight < canvasHeight) {
            // the image is smaller than the canvas
            x1 = (canvasWidth - imgWidth)  / 2;
            y1 = (canvasHeight - imgHeight) / 2;
            x2 = imgWidth + x1;
            y2 = imgHeight + y1;
             
        } else {
            if (canvasAspect > imgAspect) {
                y1 = canvasHeight;
                // keep image aspect ratio
                canvasHeight = (int) (canvasWidth * imgAspect);
                y1 = (y1 - canvasHeight) / 2;
            } else {
                x1 = canvasWidth;
                // keep image aspect ratio
                canvasWidth = (int) (canvasHeight / imgAspect);
                x1 = (x1 - canvasWidth) / 2;
            }
            x2 = canvasWidth + x1;
            y2 = canvasHeight + y1;
        }
 
        g.drawImage(image, x1, y1, x2, y2, 0, 0, imgWidth, imgHeight, null);
    }

}
