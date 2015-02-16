import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import org.bytedeco.javacpp.opencv_core;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by igor on 22.01.2015.
 */
public class OpenCVImageJUtils {

    public static ImageProcessor toImageProcessor(opencv_core.IplImage image) {
        BufferedImage bi = image.getBufferedImage();

        if (bi.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return new ColorProcessor(bi);
        } else
            return null;
    }

    public static ColorProcessor toColorProcessor(opencv_core.IplImage image) {
        ColorProcessor ip = (ColorProcessor) toImageProcessor(image);

        return ip;
    }

    public static BufferedImage toBufferedImage(ImageProcessor ip) {

        BufferedImage dest = new BufferedImage(ip.getWidth(), ip.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        Graphics g = dest.getGraphics();
        g.drawImage(ip.getBufferedImage(), 0, 0, null);

        return dest;
    }

}
