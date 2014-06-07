package pl.edu.pw.elka.cpoo.images;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;

import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import pl.edu.pw.elka.cpoo.Utilities;
import pl.edu.pw.elka.cpoo.views.TabHdr;

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
            calculateExposureBasedOnImageAverageLuminance();
        }
        // Real
        else {
            for (MyImage image : images)
                image.setExposure(calculateExposureFromExif(image.getExif()) + 9.0f);

            scaleExposure();
        }
    }

    private void calculateExposureBasedOnImageAverageLuminance() {
    	float[] avgLuminances = new float[images.size()];
    	float minimalLuminance = Float.MAX_VALUE;
    	
    	for(int i = 0 ; i < images.size(); ++i) {
    		avgLuminances[i] = calculateAverageLuminance(images.get(i));
    		minimalLuminance = Math.min(minimalLuminance, avgLuminances[i]);
    	}
    	
    	for(float luminance : avgLuminances) {
    		System.out.println("" + Utilities.log2(luminance/minimalLuminance));
    	}
    	
	}

	private void scaleExposure() {
    	float minExp = Float.MAX_VALUE;
    	for(MyImage img : images) 
    		minExp = (float) Math.min(img.getExposure(), minExp);
    	for(MyImage img : images)
    		img.setExposure(img.getExposure() + Math.abs(minExp) + 1);
	}

	private float calculateExposureFromExif(ExifSubIFDDirectory exif) {
		try {
			float fNumber = exif.getFloat(ExifSubIFDDirectory.TAG_FNUMBER);
			float exposureTime = exif.getFloat(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
			return Utilities.log2(exposureTime/(float) Math.pow(fNumber, 2.0));
		} catch (MetadataException e) {
		}
		//protect before divinding by 0
		return 0.01f;
    }
    
    private boolean allImagesHasExif(List<MyImage> images) {
    	if(images == null) 
    		return false;
    	
    	for(MyImage image : images) {
    		if(image.getExif() == null)
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
                double relevance = 1 - ((Math.abs(img.getLuminance(i) - 127) + 0.5) / 127);
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
