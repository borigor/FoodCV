import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Created by igor on 14.12.2014.
 */
public class ColorHistogram {

    public static IplImage[] splitChannels(IplImage src) {

        if (src == null) {
            System.out.println("image == null");
            return null;
        }

        if (src.nChannels() != 3) {
            System.out.println("nChannesl != 3, = " + src.nChannels());
        }

        CvSize size = cvGetSize(src);

        IplImage channel0 = cvCreateImage(size, src.depth(), 1);
        IplImage channel1 = cvCreateImage(size, src.depth(), 1);
        IplImage channel2 = cvCreateImage(size, src.depth(), 1);

        cvSplit(src, channel0, channel1, channel2, null);

        return new IplImage[]{channel0, channel1, channel2};
    }

    public CvHistogram getHueHistogram(IplImage image, int minSaturation) {
        if (image == null) {
            System.out.println("image == null");
            return null;
        }

        if (image.nChannels() != 3) {
            System.out.println("nChannels != 3 , = " + image.nChannels());
            return null;
        }

        IplImage hsvImage = cvCreateImage(cvGetSize(image), image.depth(), 3);
        cvCvtColor(image, hsvImage, CV_BGR2HSV);

        IplImage[] hsvChannels = splitChannels(hsvImage);

        IplImage saturationMask = null;
        if (minSaturation > 0) {
            saturationMask = cvCreateImage(cvGetSize(hsvImage), IPL_DEPTH_8U, 1);
            cvThreshold(hsvChannels[1], saturationMask, minSaturation, 255, CV_THRESH_BINARY);
        }

        Histogram1D h1D = new Histogram1D();
        h1D.setRanges(0, 180);
        CvHistogram histogram = h1D.getHistogram(hsvChannels[0], saturationMask);

        return histogram;
    }
}
