import org.bytedeco.javacpp.opencv_core.CvHistogram;
import org.bytedeco.javacpp.opencv_core.IplImage;

import static org.bytedeco.javacpp.helper.opencv_imgproc.cvCreateHist;
import static org.bytedeco.javacpp.helper.opencv_imgproc.cvCalcHist;
import static org.bytedeco.javacpp.opencv_core.CV_HIST_ARRAY;

/**
 * Created by igor on 15.12.2014.
 */
public class Histogram1D {

    private int numbersOfBins = 256;
    private float minRange = 0.0f;
    private float maxRange = 255.0f;

    public void setRanges(float minRange, float maxRange) {
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    public CvHistogram getHistogram(IplImage image, IplImage mask) {

        int dims = 1;
        int[] sizes = new int[]{numbersOfBins};
        float[][] ranges = new float[][]{new float[]{minRange, maxRange}};

        CvHistogram hist = cvCreateHist(dims, sizes, CV_HIST_ARRAY, ranges, 1);

        int accumulate = 0;
        cvCalcHist(new IplImage[]{image}, hist, accumulate, mask);

        return hist;
    }
}