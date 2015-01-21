import org.bytedeco.javacpp.opencv_core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by igor on 11.01.2015.
 */
public class ImagePanel extends JPanel {

    Image img;

    public ImagePanel() {

        Dimension size = new Dimension(700, 525);

        setPreferredSize(size);
        setSize(size);
    }

    public void setImg(opencv_core.IplImage image) {
        BufferedImage bufferedImage = image.getBufferedImage();
        this.img = bufferedImage.getScaledInstance(700, 525, Image.SCALE_DEFAULT);

        this.repaint();
    }

    public void setImg(BufferedImage image) {
        this.img = image.getScaledInstance(700, 525, Image.SCALE_DEFAULT);

        this.repaint();
    }

    public Image getImg() {
        return img;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }
}
