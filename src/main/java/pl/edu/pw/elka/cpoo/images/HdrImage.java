package pl.edu.pw.elka.cpoo.images;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;

import pl.edu.pw.elka.cpoo.views.TabHdr;

import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class HdrImage {

    private List<MyImage> images;
    float[] hdrData;

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

    private void calculateExposures() {
        // From tool
        if (!allImagesHasExif(images)) {
            System.out.println("Unknown parameters ii");
            calculateExposureBasedOnImageAverageLuminance();
        }
        // Real
        else {
            System.out.println("Known parameters !!");
            for (MyImage image : images) {
                image.setExposure(calculateExposureFromExif(image.getExif()));
            }
            scaleExposure();
        }
    }

    Comparator<MyImage> comp = new Comparator<MyImage>() {

        @Override
        public int compare(MyImage o1, MyImage o2) {
            return o1.getExposure() > o2.getExposure() ? 1 : -1;
        }
    };

    private void calculateExposureBasedOnImageAverageLuminance() {
        for (MyImage img : images) {
            img.setExposure(calculateAverageLuminance(img));
        }
        scaleExposure();
    }

    private void scaleExposure() {
        Collections.sort(images, comp);

        double min = Collections.min(images, comp).getExposure();
        double max = Collections.max(images, comp).getExposure();

        for (MyImage img : images) {
            img.setExposure((img.getExposure() - min) / (max - min) + 1);
        }
    }

    private float calculateExposureFromExif(ExifSubIFDDirectory exif) {
        float fNumber = 0.1f;
        try {
            fNumber = exif.getFloat(ExifSubIFDDirectory.TAG_FNUMBER);
        } catch (MetadataException e) {
        }
        float exposureTime = 0.005f;
        try {
            exposureTime = exif.getFloat(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
        } catch (MetadataException e) {
        }
        float iso = 100;
        try {
            iso = exif.getFloat(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
        } catch (MetadataException e) {
        }

        if (fNumber == 0) {
            fNumber = 0.1f;
        }
        if (iso == 0) {
            iso = 100;
        }

        float value = (float) (Math.log(exposureTime / (fNumber * fNumber) * iso / 100.0) / Math
                .log(2));

        // Theoretically middle one should be set to 0 and other related to
        // it
        // eg. -1, -5, -3 => should be sorted => -5, -3, -1 and 'related' =>
        // -2,0,+2 but is's done in different place in different way
        System.out.println(fNumber + " / " + exposureTime + " / " + iso + " == " + value);

        return value;
    }

    private boolean allImagesHasExif(List<MyImage> images) {
        if (images == null)
            return false;

        for (MyImage image : images) {
            if (image.getExif() == null)
                return false;
        }
        return true;
    }

    private float calculateAverageLuminance(MyImage image) {
        float avLum = 0.0f;
        int size = image.getWidth() * image.getHeight();

        for (int i = 0; i < size; i++) {
            avLum += Math.log(image.getLuminance(i) + 1e-4);
        }
        avLum = (float) Math.exp(avLum / size);
        return avLum;
    }

    public void process() {
        createHdr();
    }

    private void createHdr() {
        hdrData = new float[width * height * 3];

        double[] relevances = new double[width * height];
        int color;
        double red, green, blue;
        boolean isPixelSet;
        for (int i = 0; i < width * height; i++) {
            isPixelSet = false;
            for (MyImage img : images) {
                double relevance = 1 - ((Math.abs(img.getLuminance(i) - 127) + 0.5) / 255);
                if (relevance > 0.05) {
                    isPixelSet = true;
                    color = img.getBufferedImage().getRGB(i % width, i / width);
                    red = (color & 0x00ff0000) >> 16;
                    green = (color & 0x0000ff00) >> 8;
                    blue = color & 0x000000ff;
                    hdrData[i * 3 + 0] += red * relevance / img.getExposure();
                    hdrData[i * 3 + 1] += green * relevance / img.getExposure();
                    hdrData[i * 3 + 2] += blue * relevance / img.getExposure();

                    relevances[i] += relevance;
                }
            }
            if (!isPixelSet) {
                MyImage img = images.get(images.size() / 2);
                color = img.getBufferedImage().getRGB(i % width, i / width);
                red = (color & 0x00ff0000) >> 16;
                green = (color & 0x0000ff00) >> 8;
                blue = color & 0x000000ff;
                hdrData[i * 3 + 0] += red / img.getExposure();
                hdrData[i * 3 + 1] += green / img.getExposure();
                hdrData[i * 3 + 2] += blue / img.getExposure();
                relevances[i] = 1;
            }
        }
        for (int i = 0; i < width * height; i++) {
            hdrData[i * 3 + 0] /= relevances[i];
            hdrData[i * 3 + 1] /= relevances[i];
            hdrData[i * 3 + 2] /= relevances[i];
        }

        System.out.println("done");
    }

    public Image getExposedImage(double exposure) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < width * height; i++) {
            int rgb = 0;

            rgb |= Math.min(255, Math.max(0, (int) (hdrData[i * 3 + 0] * exposure))) << 16;
            rgb |= Math.min(255, Math.max(0, (int) (hdrData[i * 3 + 1] * exposure))) << 8;
            rgb |= Math.min(255, Math.max(0, (int) (hdrData[i * 3 + 2] * exposure)));

            image.setRGB(i % width, i / width, rgb);
        }

        return image;
    }

    public void showRawTool() {
        JFrame a = new JFrame("RawTool");
        a.setBounds(0, 0, 500, 500);
        a.setLayout(new BorderLayout());
        a.add(new TabHdr(this), BorderLayout.CENTER);
        a.setVisible(true);
    }

    public float[] getHdrData() {
        return hdrData;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
