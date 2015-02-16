import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import model.ColorRGB;
import model.FoodTemplate;

import java.awt.*;
import java.util.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_video.cvCamShift;
import static org.bytedeco.javacpp.opencv_video.cvMeanShift;

/**
 * Created by igor on 17.12.2014.
 */
public class FoodDetector {

    private Map<String, FoodTemplate> foodTemplateMap = new HashMap<String, FoodTemplate>();

    public Map<String, FoodTemplate> getFoodTemplateMap() {
        return foodTemplateMap;
    }

    public Map<String, CvRect> resultMap = new HashMap<String, CvRect>();

    public void setFoodTemplateMap(Map<String, FoodTemplate> foodTemplateMap) {
        this.foodTemplateMap = foodTemplateMap;
    }

    public void createTemplate(String name, String templateFileName, Rectangle rect, double coef) {

        IplImage templateImage = cvLoadImage(templateFileName + ".jpg");
//        Utils.show(templateImage, "template image");
//        Utils.show(Utils.drawOnImage(templateImage, rect, Color.BLUE), "Input image");

        templateImage.roi(Utils.toIplROI(rect));

        int minSaturation = 150;
        CvHistogram templateHueHist = new ColorHistogram().getHueHistogram(templateImage, minSaturation);

        FoodTemplate foodTemplate = new FoodTemplate();
        foodTemplate.setName(name);
        foodTemplate.setMinSaturation(minSaturation);
        foodTemplate.setRect(rect);
        foodTemplate.setTemplateHueHist(templateHueHist);

        System.out.println(name);
        ColorRGB colorRGB = getMeanColor(templateImage, rect);

        foodTemplate.setMinDist(60);
        foodTemplate.setTargetColor(colorRGB);
        foodTemplate.setCoef(coef);

        foodTemplateMap.put(name, foodTemplate);
    }

    public void camDetection(String targetFileName, FoodTemplate foodTemplate, String nameTemplate) {

        Rectangle rect = foodTemplate.getRect();
        int minSaturation = foodTemplate.getMinSaturation();
        CvHistogram templateHueHist = foodTemplate.getTemplateHueHist();

//        IplImage targetImage = cvLoadImage(targetFileName + ".jpg");
        IplImage targetImage = cvLoadImage(targetFileName);
        IplImage hsvTargetImage = cvCreateImage(cvGetSize(targetImage), targetImage.depth(), 3);

        cvCvtColor(targetImage, hsvTargetImage, CV_BGR2HSV);

        IplImage saturationChannel = ColorHistogram.splitChannels(hsvTargetImage)[1];

        cvThreshold(saturationChannel, saturationChannel, minSaturation, 255, CV_THRESH_BINARY);

        ContentFinder finder = new ContentFinder();
        finder.setHistogram(templateHueHist);
        IplImage result = finder.find(hsvTargetImage);

        cvAnd(result, saturationChannel, result, null);

        CvRect targetRect = new CvRect();
        targetRect.x(rect.x);
        targetRect.y(rect.y);
        targetRect.height(rect.height);
        targetRect.width(rect.width);

        CvTermCriteria termCriteria = new CvTermCriteria();
        termCriteria.max_iter(60);
        termCriteria.epsilon(0.005);
        termCriteria.type(CV_TERMCRIT_ITER);

        CvConnectedComp searchResult = new CvConnectedComp();
//        int iterations = cvMeanShift(result, targetRect, termCriteria, searchResult);
        int iterations = cvCamShift(result, targetRect, termCriteria, searchResult);

        if (Utils.rectIntoPlates(searchResult.rect(), detectCircleMat(targetFileName)) && iterations > 0) {

//            Utils.show(Utils.drawAndWriteOnImage(result, Utils.toRectangle(searchResult.rect()), nameTemplate, Color.RED),
//                    "Output in " + iterations + " inerations");

            resultMap.put(nameTemplate, searchResult.rect());
            System.out.println(foodTemplate.getName() + " detected");
        }
    }

    public void meanShiftDetection(String targetFileName, FoodTemplate foodTemplate, String nameTemplate) {

        Rectangle rect = foodTemplate.getRect();
        int minSaturation = foodTemplate.getMinSaturation();
        CvHistogram templateHueHist = foodTemplate.getTemplateHueHist();

//        IplImage targetImage = cvLoadImage(targetFileName + ".jpg");
        IplImage targetImage = cvLoadImage(targetFileName);
        IplImage hsvTargetImage = cvCreateImage(cvGetSize(targetImage), targetImage.depth(), 3);

        cvCvtColor(targetImage, hsvTargetImage, CV_BGR2HSV);

        IplImage saturationChannel = ColorHistogram.splitChannels(hsvTargetImage)[1];

        cvThreshold(saturationChannel, saturationChannel, minSaturation, 255, CV_THRESH_BINARY);

        ContentFinder finder = new ContentFinder();
        finder.setHistogram(templateHueHist);
        IplImage result = finder.find(hsvTargetImage);

        cvAnd(result, saturationChannel, result, null);

        CvRect targetRect = new CvRect();
        targetRect.x(rect.x);
        targetRect.y(rect.y);
        targetRect.height(rect.height);
        targetRect.width(rect.width);

        CvTermCriteria termCriteria = new CvTermCriteria();
        termCriteria.max_iter(60);
        termCriteria.epsilon(0.005);
        termCriteria.type(CV_TERMCRIT_ITER);

        CvConnectedComp searchResult = new CvConnectedComp();
//        int iterations = cvMeanShift(result, targetRect, termCriteria, searchResult);
        int iterations = cvMeanShift(result, targetRect, termCriteria, searchResult);

        if (Utils.rectIntoPlates(searchResult.rect(), detectCircleMat(targetFileName)) && iterations > 0) {

//            Utils.show(Utils.drawAndWriteOnImage(result, Utils.toRectangle(searchResult.rect()), nameTemplate, Color.RED),
//                    "Output in " + iterations + " inerations");

            resultMap.put(nameTemplate, searchResult.rect());
            System.out.println(foodTemplate.getName() + " detected");
        }
    }

    public CvSeq detectCircleMat(String fileName) {

//        Mat src = imread(fileName + ".jpg");
        Mat src = imread(fileName);
        Mat gray = new Mat();
        Size size = new Size(9, 9);

        cvtColor(src, gray, CV_BGR2GRAY);
        GaussianBlur(gray, gray, size, 1.5, 1.5, BORDER_DEFAULT);

        CvMemStorage mem = CvMemStorage.create();
        CvSeq circles = cvHoughCircles(gray.asIplImage(), mem, CV_HOUGH_GRADIENT, 1, 300, 80, 60, 100, 500);

        for(int i = 0; i < circles.total(); i++){
            CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, i));

            CvPoint2D32f point = new CvPoint2D32f();
            point.x(circle.x());
            point.y(circle.y());

            CvPoint center = cvPointFrom32f(point);

            int radius = Math.round(circle.z());
            cvCircle(src.asIplImage(), center, radius, CvScalar.BLUE, 6, CV_AA, 0);
        }

//        Utils.show(src.asIplImage(), "plates");

        return circles;
    }

    public IplImage colorDetector(String imageFilename, FoodTemplate foodTemplate, String nameTemplate) {

        IplImage iplImage = cvLoadImage(imageFilename);

        ColorProcessor src = OpenCVImageJUtils.toColorProcessor(iplImage);
        ByteProcessor dest = new ByteProcessor(src.getWidth(), src.getHeight());

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                if (distance(src.getColor(x, y), foodTemplate.getTargetColor()) < foodTemplate.getMinDist()) {
                    dest.set(x, y, 255);
                }
            }
        }

        IplImage res = AbstractIplImage.createFrom(OpenCVImageJUtils.toBufferedImage(dest));
        Utils.show(res, nameTemplate);

        Rectangle tempRect = foodTemplate.getRect();
        int idealRect = tempRect.height * tempRect.width;
        System.out.println(nameTemplate);
        System.out.println("ideal count = " + idealRect);

        CvSeq circles = detectCircleMat(imageFilename);

        for(int i = 0; i < circles.total(); i++){

            Boolean isFind = false;

            CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, i));
            CvPoint2D32f point = new CvPoint2D32f();
            point.x(circle.x());
            point.y(circle.y());

            CvPoint circleCenter = cvPointFrom32f(point);
            int radius = Math.round(circle.z());

            int xStart = (circleCenter.x() - radius > 0) ? circleCenter.x() - radius : 0;
            int xEnd = (circleCenter.x() + radius < iplImage.width()) ? circleCenter.x() + radius : iplImage.width();
            int yStart = (circleCenter.y() - radius > 0) ? circleCenter.y() - radius : 0;
            int yEnd = (circleCenter.y() + radius < iplImage.height()) ? circleCenter.y() + radius : iplImage.height();

            for (int y = yStart; y < yEnd - tempRect.height; y+=5) {
                for (int x = xStart; x < xEnd - tempRect.width; x+=5) {

                    int count = Utils.calculateColorArea(dest, x, y, tempRect);
                    if (count >= foodTemplate.getCoef() * idealRect) {
                        System.out.println(count);

//                        Utils.show(Utils.drawOnImage(iplImage, new Rectangle(x, y, tempRect.width, tempRect.height), Color.GREEN),
//                                "Output in " + " inerations");

                        CvRect cvRect = cvRect(x, y, tempRect.width, tempRect.height);
                        resultMap.put(nameTemplate, cvRect);
                        isFind = true;
                        break;
                    }
                }

                if (isFind) break;
            }
        }

        return res;
    }

    private ColorRGB getMeanColor(IplImage templateImage, Rectangle rect) {

        ColorProcessor src = OpenCVImageJUtils.toColorProcessor(templateImage);

        int redVal = 0;
        int greenVal = 0;
        int blueVal = 0;
        int[] redAvg = new int[rect.height];
        int[] greenAvg = new int[rect.height];
        int[] blueAvg = new int[rect.height];

        for (int y = rect.y; y < rect.y + rect.height; y++) {
            for (int x = rect.x; x < rect.x + rect.width; x++) {
                Color color = src.getColor(x, y);
                redVal += color.getRed();
                greenVal += color.getGreen();
                blueVal += color.getBlue();
            }
            redAvg[y - rect.y] = redVal / rect.width - 1;
            greenAvg[y - rect.y] = greenVal / rect.width - 1;
            blueAvg[y - rect.y] = blueVal / rect.width - 1;

            redVal = 0;
            greenVal = 0;
            blueVal = 0;
        }

        for (int i = 0; i < rect.height; i++) {
            redVal += redAvg[i];
            greenVal += greenAvg[i];
            blueVal += blueAvg[i];
        }

        redVal = redVal / rect.height;
        greenVal = greenVal / rect.height;
        blueVal = blueVal / rect.height;

        Color color = new Color(redVal, greenVal, blueVal);
        System.out.println(color.toString());
        ColorRGB colorRGB = new ColorRGB(redVal, greenVal, blueVal);

        return colorRGB;
    }

    private Double distance(Color color, ColorRGB colorRGB) {

        int d = Math.abs(colorRGB.getRed() - color.getRed()) +
                Math.abs(colorRGB.getGreen() - color.getGreen()) +
                Math.abs(colorRGB.getBlue() - color.getBlue());

        return Double.valueOf(d);
    }
}
