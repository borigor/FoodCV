import static org.bytedeco.javacpp.helper.opencv_imgproc.cvCalcBackProject;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.cvThreshold;

/**
 * Created by igor on 15.12.2014.
 */
public class ContentFinder {

    private CvHistogram histogram;
    private float thresold;

    public CvHistogram getHistogram() {
        return histogram;
    }

    public void setHistogram(CvHistogram histogram) {
        this.histogram = histogram;
    }

    public float getThresold() {
        return thresold;
    }

    public void setThresold(float thresold) {
        this.thresold = thresold;
    }

    public IplImage find(IplImage image) {

        IplImage[] channels = ColorHistogram.splitChannels(image);

        for (int i = 0; i < channels.length; i++) {
            IplImage im = Utils.toIplImage32F(channels[i]);
            channels[i] = im;
        }

        IplImage dest = cvCreateImage(cvGetSize(image), IPL_DEPTH_32F, 1);

        cvCalcBackProject(channels, dest, histogram);

        if (thresold > 0) {
            cvThreshold(dest, dest, thresold, 1, CV_THRESH_BINARY);
        }

        return Utils.toIplImage8U(dest, true);
    }
}
