package pl.edu.pw.elka.cpoo.images;

import java.awt.BorderLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import pl.edu.pw.elka.cpoo.views.TabHdr;

public class HdrImage {

    private List<MyImage> images;
    PixelMap hdrDataMap;

    private int width;
    private int height;

    public HdrImage(List<MyImage> images) {
        if (images.isEmpty() == true) {
            throw new IllegalArgumentException();
        }

        this.images = images;
        calculateExposures();

        MyImage img = images.get(0);
        width = img.getWidth();
        height = img.getHeight();
    }

    // TODO calculate exposure for images
    private void calculateExposures() {
        // TODO EV calculation/extraction
        // It has to be relative exposure -> Most similar to EV = 0 should be 1
        // and more dark images lower, brighter -> higher -|| nie wiem jak to
        // ogarnac, to sa wartosci z toola co liczyl grubo, my uzyjemy alg ze
        // stronki
        try {
            // From tool
            if (true) {
                images.get(0).setExposure(0.29259689966586955);
                images.get(1).setExposure(0.8081045891269992);
                images.get(2).setExposure(1.0);
                images.get(3).setExposure(6.411333121849957);
            }
            // Real
            else {
                images.get(0).setExposure(-4.72);
                images.get(1).setExposure(-1.82);
                images.get(2).setExposure(1.51);
                images.get(3).setExposure(4.09);
            }
        } catch (IndexOutOfBoundsException e) {
            // ignore test
        }
    }

    public void process() {
        createHdr();
    }

    private void createHdr() {
        hdrDataMap = new PixelMap(width, height);

        double[][] relevances = new double[width][height];
        Pixel color;
        int red, green, blue;
        boolean isPixelSet;

        ArrayList<PixelMap> maps = new ArrayList<PixelMap>();
        for (MyImage e : images) {
            maps.add(new PixelMap(ImageWrapper.imageToPixelArray(e.getImage())));
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                isPixelSet = false;
                int i = 0;
                for (MyImage img : images) {
                    double lum = img.getLuminance(y * width + y);
                    // // lum = maps.get(i).getPixel(x, y).getLuminance();
                    double relevance = 1;// 1 - ((Math.abs(lum - 127) + 0.5) /
                                         // 127);
                    // if (relevance > 0.05) {
                    // isPixelSet = true;
                    color = maps.get(i++).getPixel(x, y);
                    red = color.r;
                    green = color.g;
                    blue = color.b;

                    Pixel m = hdrDataMap.getPixel(x, y);
                    m.r += red * relevance / img.getExposure();
                    m.g += green * relevance / img.getExposure();
                    m.b += blue * relevance / img.getExposure();

                    relevances[x][y] += relevance;
                    // }
                }
                // if (!isPixelSet) {
                // MyImage img = images.get(0);
                //
                // color = maps.get(0).getPixel(x, y);
                // red = color.r;
                // green = color.g;
                // blue = color.b;
                //
                // Pixel m = map.getPixel(x, y);
                // m.r += red / img.getExposure();
                // m.g += green / img.getExposure();
                // m.b += blue / img.getExposure();
                //
                // relevances[x][y] = 1;
                // }
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                Pixel m = hdrDataMap.getPixel(x, y);
                m.r /= relevances[x][y];
                m.g /= relevances[x][y];
                m.b /= relevances[x][y];
            }
        }

        System.out.println("success without exception");
    }

    public Image getExposedImage(double exposure) {
        PixelMap pm = new PixelMap(hdrDataMap.getPixels());

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                Pixel m = pm.getPixel(x, y);
                m.r = Math.min(255, Math.max(0, (int) (m.r * exposure)));
                m.g = Math.min(255, Math.max(0, (int) (m.g * exposure)));
                m.b = Math.min(255, Math.max(0, (int) (m.b * exposure)));
            }
        }

        return ImageWrapper.pixelArryToImage(pm.getPixels());
    }

    public void showRawTool() {
        JFrame a = new JFrame("RawTool");
        a.setBounds(0, 0, 500, 500);
        a.setLayout(new BorderLayout());
        a.add(new TabHdr(this), BorderLayout.CENTER);
        a.setVisible(true);
    }

}
