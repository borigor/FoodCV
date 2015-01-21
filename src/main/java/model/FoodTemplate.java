package model;

import org.bytedeco.javacpp.opencv_core.CvHistogram;

import java.awt.*;

/**
 * Created by igor on 17.12.2014.
 */
public class FoodTemplate {

    private String name;
    private String price;

    private Rectangle rect;
    private int minSaturation;
    private CvHistogram templateHueHist;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Rectangle getRect() {
        return rect;
    }

    public void setRect(Rectangle rect) {
        this.rect = rect;
    }

    public int getMinSaturation() {
        return minSaturation;
    }

    public void setMinSaturation(int minSaturation) {
        this.minSaturation = minSaturation;
    }

    public CvHistogram getTemplateHueHist() {
        return templateHueHist;
    }

    public void setTemplateHueHist(CvHistogram templateHueHist) {
        this.templateHueHist = templateHueHist;
    }
}
