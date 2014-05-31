package pl.edu.pw.elka.cpoo.algorithms;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.pw.elka.cpoo.ArrayTools;
import pl.edu.pw.elka.cpoo.images.HdrImage;
import pl.edu.pw.elka.cpoo.images.ImageWrapper;
import pl.edu.pw.elka.cpoo.images.MyImage;
import pl.edu.pw.elka.cpoo.interfaces.HdrProcessor;

public class ToneMappingAlg2 implements HdrProcessor {

    private HdrImage hdrImage;
    private double bias;

    @Override
    public Image process(final ImageWrapper imageWrapper) {
        List<MyImage> images = new ArrayList<MyImage>();
        for (Image img : imageWrapper.getImages()) {
            images.add(new MyImage(img));
        }

        hdrImage = new HdrImage(images);
        hdrImage.process();

        hdrImage.showRawTool();

        return doTonalMappingAlg2(hdrImage);
    }

    @Override
    public Image process(final Image image) {
        return process(new ImageWrapper(Arrays.asList(image)));
    }

    @Override
    public String getName() {
        return "Tonal Mapping 2";
    }
    
    private float[] calculateLuminance(int size, float[] Y) {
        float avLum = 0.0f;
        float maxLum = 0.0f;

        for (int i = 0; i < size; i++) {
            avLum += Math.log(Y[i] + 1e-4);
            maxLum = (Y[i] > maxLum) ? Y[i] : maxLum;
        }
        avLum = (float) Math.exp(avLum / size);
        return new float[]{avLum, maxLum};
    }

    public double biasFunc(double b, double x) {
        return Math.pow(x, b);          // pow(x, log(bias)/log(0.5)
    }
    
    /**
     * Drago tonal mapping alghoritm
     * 
     * @param hdrImage
     * 
     * @return LDR image
     */
    private Image doTonalMappingAlg2(HdrImage hdrImage) {

        float[] pixels = hdrImage.getHdrData();
        
        int length = hdrImage.getWidth() * hdrImage.getHeight();
        double[][] out = new double[hdrImage.getWidth()][hdrImage.getHeight()];
        float[] luminance = calculateLuminance(length, pixels);
        double maxLum = luminance[1];
        double avLum = luminance[0];
        maxLum /= avLum;
        float[][] pixels2D = ArrayTools.convert1D2D(pixels, hdrImage.getWidth(), hdrImage.getHeight());
        System.out.println(pixels2D.length);
        System.out.println(pixels2D[1].length);
        double LOG05 = -0.693147; // log(0.5)
        float biasP = (float) (Math.log(bias) / LOG05);

        double divider = Math.log10(maxLum + 1.0);
        //System.out.println(pixels2D[126][128]);

        System.out.println("Image width " + hdrImage.getWidth() + ", height " + hdrImage.getHeight());

        int i, j;
        for (int y = 0; y + 3 < hdrImage.getHeight(); y += 3) {
            for (int x = 0; x + 3 < hdrImage.getWidth(); x += 3) {
                //System.out.println(x);
                double average = 0.0;
                for (i = 0; i < 3; i++) {
                    for (j = 0; j < 3; j++) {
                        //System.out.println(x + " " + (x + i) + " " + (y + j));
                        average += pixels2D[x + i][y + j] / avLum; //(*Y)(x+i,y+j) / avLum;
                    }
                }
                average = average / 9.0 - pixels2D[x][y]; //(*Y)(x,y);
                if (average > -1.0f && average < 1.0) {
                    double interpol = Math.log(2.0 + biasFunc(biasP, pixels2D[x + 1][y + 1] / maxLum) * 8.0);
                    for (i = 0; i < 3; i++) {
                        for (j = 0; j < 3; j++) {
                            double Yw = pixels2D[x + i][y + j]; //(*Y)(x+i,y+j);
                            if (Yw < 1.0f) {
                                double L = Yw * (6.0 + Yw) / (6.0 + 4.0 * Yw);
                                Yw = (L / interpol) / divider;
                            } else if (Yw >= 1.0f && Yw < 2.0f) {
                                double L = Yw * (6.0 + 0.7662 * Yw) / (5.9897 + 3.7658 * Yw);
                                Yw = (L / interpol) / divider;
                            } else {
                                Yw = (Math.log(Yw + 1.0) / interpol) / divider;
                            }

                            out[x + i][y + j] = Yw;
                        //(*L)(x+i,y+j) = Yw;
                        }
                    }
                } else {
                    for (i = 0; i < 3; i++) {
                        for (j = 0; j < 3; j++) {
                            double Yw = pixels2D[x + i][y + j];//(*Y)(x+i,y+j);
                            double interpol = Math.log(2.0f + biasFunc(biasP, Yw / maxLum) * 8.0);
                            // (*L)(x+i,y+j)
                            out[x + i][y + j] = (Math.log(Yw + 1.0) / interpol) / divider;
                        }
                    }
                }
            }
        }

        for (int x = 0; x < hdrImage.getWidth(); x++) {
            for (int y = 0; y < hdrImage.getHeight(); y++) {
                double scale = out[x][y] / pixels2D[x][y];
                pixels2D[x][y] *= scale;
            }
        }
        
        BufferedImage retImg = new BufferedImage(hdrImage.getWidth(), hdrImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster raster = retImg.getRaster();
        raster.setPixels(raster.getMinX(), raster.getMinY(), raster.getWidth(), raster.getHeight(), ArrayTools.convert2D1D(pixels2D, hdrImage.getWidth(), hdrImage.getHeight()));

        return retImg;
    }
}
