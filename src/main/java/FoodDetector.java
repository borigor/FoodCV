import model.FoodTemplate;

import java.awt.*;
import java.util.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_video.cvCamShift;

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

    public void createTemplate(String name, String templateFileName, Rectangle rect) {

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



}
