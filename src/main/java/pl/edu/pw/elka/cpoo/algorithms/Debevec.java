package pl.edu.pw.elka.cpoo.algorithms;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.pw.elka.cpoo.Utilities;
import pl.edu.pw.elka.cpoo.interfaces.HdrProcessor;

public class Debevec implements HdrProcessor {

	private int width;
    private int height;
    private float[][][] hdrContent;
	
	@Override
	public Image process(ImageWrapper imageWrapper) {
		List<Pixel[][]> images = new ArrayList<>();
		for (int i = 0; i < imageWrapper.getImages().size(); ++i) {
        	images.add(Utilities.createPixelArrayFromImage(imageWrapper.getImage(i)));
        }

		width = images.get(0)[0].length;
		height = images.get(0).length;
		double[][] relevances = new double[height][width];
        boolean isPixelSet;
        
        hdrContent = new float[height][width][3];
        
        double[] exposures = {-1.0f, 0.0f, 1.0f};

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
	        	isPixelSet = false;
	            for (int i = 0; i < images.size(); ++i) {
	            	Pixel p = images.get(i)[y][x];
	            	
	                double relevance = 1 - (((double)Math.abs(p.grayValue() - 127) + 0.5) / 127);
	                if (relevance > 0.05) {
                        isPixelSet = true;
                        hdrContent[y][x][0] += p.r * relevance / exposures[i];
                        hdrContent[y][x][1] += p.g * relevance / exposures[i];
                        hdrContent[y][x][2] += p.b * relevance / exposures[i];
                        
                        relevances[y][x] += relevance;
	                }
	            }
	            if (!isPixelSet) {
	            	Pixel p = images.get(0)[y][x];
	            	hdrContent[y][x][0] += p.r / exposures[0];
                    hdrContent[y][x][1] += p.g / exposures[0];
                    hdrContent[y][x][2] += p.b / exposures[0];
                    relevances[y][x] = 1;
	            }
            }
        }
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                hdrContent[y][x][0] /= relevances[y][x];
                hdrContent[y][x][1] /= relevances[y][x];
                hdrContent[y][x][2] /= relevances[y][x];
            }
        }
        
        return imageWrapper.getImage(0);
	}

	@Override
    public Image process(final Image image) {
        return process(new ImageWrapper(Arrays.asList(image)));
    }

	@Override
    public String getName() {
        return "Debevec HDR";
    }

}
