import org.bytedeco.javacpp.opencv_core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Stack;

import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_core.IplImage;

/**
 * Created by igor on 11.01.2015.
 */
public class FoodDetectorGUI extends JFrame {

    private JButton openFileButton;
    private JButton camShiftButton;
    private JButton meanShiftButton;
    private JButton colorAlgButton;

    final ImagePanel imagePanel;

    final FoodDetector foodDetector;

    String imageFlieName = null;

    public FoodDetectorGUI() {
        super("FoodDetectorGUI");

        this.setBounds(100, 200, 250, 100);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        foodDetector = new FoodDetector();

        foodDetector.createTemplate("Borsch", "data/p5", new Rectangle(600, 150, 500, 500), 0.55);
        foodDetector.createTemplate("Puree soup", "data/p1", new Rectangle(1100, 200, 500, 500), 0.8);
        foodDetector.createTemplate("Rice", "data/p2", new Rectangle(1020, 920, 400, 250), 0.8);
        foodDetector.createTemplate("Buckwheat", "data/p15", new Rectangle(400, 800, 300, 300), 0.8);
        foodDetector.createTemplate("Mashed potatoes", "data/p7", new Rectangle(1210, 950, 400, 250), 0.8);
        foodDetector.createTemplate("Cutlet", "data/p1", new Rectangle(750, 850, 350, 180), 0.8);
        foodDetector.createTemplate("Pork", "data/p14", new Rectangle(450, 600, 430, 300), 0.8);
        foodDetector.createTemplate("Kompot", "data/p1", new Rectangle(630, 150, 250, 250), 0.8);

        imagePanel = new ImagePanel();

        openFileButton = new JButton("open");
        camShiftButton = new JButton("camShift");
        meanShiftButton = new JButton("meanShift");
        colorAlgButton = new JButton("colorAlgorithm");

        System.out.print(colorAlgButton.getSize());

        final JPanel menu = new JPanel(new GridLayout(15, 1));
        menu.add(openFileButton);
        menu.add(camShiftButton);
        menu.add(meanShiftButton);
        menu.add(colorAlgButton);

        final JPanel east = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        east.add(menu, gbc);
        east.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                fileopen.setCurrentDirectory(new File("data"));
                int ret = fileopen.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    imageFlieName = fileopen.getSelectedFile().getAbsolutePath();
                    imagePanel.setImg(cvLoadImage(imageFlieName));
                }
            }
        });

        camShiftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                for (String nameTemplate : foodDetector.getFoodTemplateMap().keySet()) {
                    foodDetector.camDetection(imageFlieName, foodDetector.getFoodTemplateMap().get(nameTemplate), nameTemplate);
                }

                IplImage tempImage = cvLoadImage(imageFlieName);
                BufferedImage tempBufImage = tempImage.getBufferedImage();

                for (String s : foodDetector.resultMap.keySet()) {
                    tempBufImage = Utils.drawAndWriteOnImageResults(tempBufImage, Utils.toRectangle(foodDetector.resultMap.get(s)), s, Color.RED);

                    Utils.show(Utils.drawAndWriteOnImage(tempImage, Utils.toRectangle(foodDetector.resultMap.get(s)), s, Color.RED),
                    "Output in " + " inerations");
                }
                foodDetector.resultMap = new HashMap<String, opencv_core.CvRect>();

                Utils.show(tempBufImage, "buf res");
                imagePanel.setImg(tempBufImage);
            }
        });

        meanShiftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                for (String nameTemplate : foodDetector.getFoodTemplateMap().keySet()) {
                    foodDetector.meanShiftDetection(imageFlieName, foodDetector.getFoodTemplateMap().get(nameTemplate), nameTemplate);
                }

                IplImage tempImage = cvLoadImage(imageFlieName);
                BufferedImage tempBufImage = tempImage.getBufferedImage();

                for (String s : foodDetector.resultMap.keySet()) {
                    tempBufImage = Utils.drawAndWriteOnImageResults(tempBufImage, Utils.toRectangle(foodDetector.resultMap.get(s)), s, Color.RED);

                    Utils.show(Utils.drawAndWriteOnImage(tempImage, Utils.toRectangle(foodDetector.resultMap.get(s)), s, Color.RED),
                            "Output in " + " inerations");

//                    resultMap.append(s + "\n");
                    menu.add(new JLabel(s));
                }
                foodDetector.resultMap = new HashMap<String, opencv_core.CvRect>();

                Utils.show(tempBufImage, "buf res");
                imagePanel.setImg(tempBufImage);
                menu.updateUI();
//                resultsSearchList.setText(namesResult.toString());
//                resultsSearchList.append(namesResult.toString());
            }
        });

        colorAlgButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                for (String nameTemplate : foodDetector.getFoodTemplateMap().keySet()) {
                    foodDetector.colorDetector(imageFlieName, foodDetector.getFoodTemplateMap().get(nameTemplate), nameTemplate);
                }

                IplImage tempImage = cvLoadImage(imageFlieName);
                BufferedImage tempBufImage = tempImage.getBufferedImage();

                for (String s : foodDetector.resultMap.keySet()) {
                    tempBufImage = Utils.drawAndWriteOnImageResults(tempBufImage, Utils.toRectangle(foodDetector.resultMap.get(s)), s, Color.RED);

                    Utils.show(Utils.drawAndWriteOnImage(tempImage, Utils.toRectangle(foodDetector.resultMap.get(s)), s, Color.RED),
                            "Output in " + " inerations");

//                    resultMap.append(s + "\n");
                    menu.add(new JLabel(s));
                }
                foodDetector.resultMap = new HashMap<String, opencv_core.CvRect>();

                Utils.show(tempBufImage, "buf res");
                imagePanel.setImg(tempBufImage);
                menu.updateUI();

            }
        });

        this.add(east, BorderLayout.EAST);
        this.add(imagePanel);
        this.pack();
    }




}
