import org.bytedeco.javacv.CanvasFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;

/**
 * Created by igor on 15.12.2014.
 */
public class Utils {

    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height, int type) {
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public static void show(Image image, String title) {

        CanvasFrame canvas = new CanvasFrame(title, 1);
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.showImage(image);
    }

    public static void show(IplImage image, String title) {

        CanvasFrame canvas = new CanvasFrame(title, 1);
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.showImage(image);
    }

    public static BufferedImage drawOnImage(IplImage image, Shape overlay, Color color) {

        BufferedImage bi = image.getBufferedImage();
        BufferedImage canvas = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = canvas.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.setPaint(color);
        g.draw(overlay);
        g.dispose();

        return canvas;
    }

    public static BufferedImage drawAndWriteOnImage(IplImage image, Shape overlay, String str, Color color) {

        BufferedImage bi = image.getBufferedImage();
        BufferedImage canvas = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);

        Font font = new Font("TimeRoman", Font.PLAIN, 30);

        Graphics2D g = canvas.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.setPaint(color);
        g.setFont(font);
        g.draw(overlay);
        g.drawString(str, overlay.getBounds().x + 20, overlay.getBounds().y + 50);
        g.dispose();

        return canvas;
    }

    public static IplROI toIplROI(Rectangle r) {
        IplROI roi = new IplROI();

        roi.xOffset(r.x);
        roi.yOffset(r.y);
        roi.width(r.width);
        roi.height(r.height);

        return roi;
    }

    public static IplImage toIplImage32F(IplImage src) {

        IplImage dest = cvCreateImage(cvGetSize(src), IPL_DEPTH_32F, src.nChannels());
        cvConvertScale(src, dest, 1, 0);
        return dest;
    }

    public static IplImage toIplImage8U(IplImage src, boolean doScaling) {
        double[] min = new double[]{Double.MIN_VALUE};
        double[] max = new double[]{Double.MAX_VALUE};

        double scale;
        double offset;

        cvMinMaxLoc(src, min, max);

        if (doScaling) {
            scale = 255 / max[0] - min[0];
            offset = -min[0];
        } else {
            scale = 1d;
            offset = 0d;
        }

        IplImage dest = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, src.nChannels());

        cvConvertScale(src, dest, scale, offset);

        return dest;
    }

    public static Rectangle toRectangle(CvRect rect) {
        return new Rectangle(rect.x(), rect.y(), rect.width(), rect.height());
    }

    public static boolean rectIntoPlates (CvRect rect, CvSeq circles) {

        CvPoint rectCenter = new CvPoint();
        rectCenter.x(rect.x() + rect.width() / 2);
        rectCenter.y(rect.y() + rect.height() / 2);

        for(int i = 0; i < circles.total(); i++){
            CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, i));

            CvPoint2D32f point = new CvPoint2D32f();
            point.x(circle.x());
            point.y(circle.y());

            CvPoint circleCenter = cvPointFrom32f(point);

            int radius = Math.round(circle.z());

            if (distanceBetweenPoints(rectCenter, circleCenter) < radius) {
                return true;
            }
        }

        return false;
    }

    public static long distanceBetweenPoints(CvPoint pointA, CvPoint pointB) {

        int xDif = pointB.x() - pointA.x();
        int yDif = pointB.y() - pointA.y();

        double dist = Math.sqrt(xDif * xDif + yDif * yDif);

        return Math.round(dist);
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static BufferedImage drawAndWriteOnImageResults(BufferedImage bi, Shape overlay, String str, Color color) {

        BufferedImage canvas = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);

        Font font = new Font("TimeRoman", Font.PLAIN, 30);

        Graphics2D g = canvas.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.setPaint(color);
        g.setFont(font);
        g.draw(overlay);
        g.drawString(str, overlay.getBounds().x + 20, overlay.getBounds().y + 50);
        g.dispose();

        return canvas;
    }
}
