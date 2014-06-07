package pl.edu.pw.elka.cpoo.algorithms;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.pw.elka.cpoo.images.HdrImage;
import pl.edu.pw.elka.cpoo.images.ImageWrapper;
import pl.edu.pw.elka.cpoo.images.MyImage;
import pl.edu.pw.elka.cpoo.interfaces.HdrProcessor;

public class ToneMappingAlg2 implements HdrProcessor {

	private static final double[] RGB2XYZ = { 0.5141364, 0.3238786, 0.16036376, 0.265068, 0.67023428, 0.06409157, 0.0241188, 0.1228178, 0.84442666 };
    private static final double[] XYZ2RGB = { 2.5651, -1.1665, -0.3986, -1.0217, 1.9777, 0.0439, 0.0753, -0.2543, 1.1892 };
      
    private HdrImage hdrImage;

    @Override
    public Image process(final ImageWrapper imageWrapper) {
        List<MyImage> images = new ArrayList<MyImage>();
        for (Image img : imageWrapper.getImages()) {
            images.add(new MyImage(img));
        }

        hdrImage = new HdrImage(images);
        hdrImage.process();

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
    
    private Image doTonalMappingAlg2(HdrImage hdrImage) {

        // dane hdr
        float[] rgb = hdrImage.getHdrData();
        int size = (rgb.length / 3);
        
        double max = 0;
        double avg = 0;
        
        double maxLuminance;
        double avgLuminance;
        double divider;

        // konwersja obrazu hdr do Yxy i wyliczenie maksymalnej, sredniej oraz
        // średniej logaryticznej wartości luminancji
        for (int pix = 0; pix < rgb.length; pix += 3)
        {           
           final double y = RGB2XYZ[3] * rgb[pix] + RGB2XYZ[4] * rgb[pix + 1] + RGB2XYZ[5] * rgb[pix + 2];
           avg += Math.log(y + 1e-4);
           max = (y > max) ? y : max ;
        }
        avgLuminance = Math.exp(avg / size);
       
        // Normalize
        maxLuminance = max / avgLuminance;
       
        // Set divider
        divider = Math.log10(maxLuminance + 1.0);

        final double biasP = Math.log(1) / -0.693147;

        return scaleLum(biasP, maxLuminance, avgLuminance, divider, rgb, hdrImage.getWidth(), hdrImage.getHeight());
    }

    /**
     * skaluje wartosci luminancji wg odpowiedniego wspolczynnika (Drago)
     * 
     * @param scale
     *            wspolczynnik skali
     * @param rgb
     *            tablica wartości obrazu hdr w RGB
     * @param height
     *            wysokosc obrazy
     * @param width
     *            szerokosc obrazu
     * @return Image obraz po przeskalowaniu i konwersji do RGB
     */
    private static final Image scaleLum(final double biasP, double maxLuminance, double avgLuminance, double divider,
    		final float[] rgb, int width, int height)
    {
    	double luminanceAvgRatio = 0;
    	double newLum = 0;

        // pusty obraz wynikowy o odpowiednim rozmiarze
        BufferedImage retImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        // konwersja do Yxy i skalowanie
        for (int pix = 0; pix < rgb.length; pix += 3)
        {
            double Y = RGB2XYZ[3] * rgb[pix] + RGB2XYZ[4] * rgb[pix + 1] + RGB2XYZ[5] * rgb[pix + 2];
            if (Y > 0)
            {
                double x = RGB2XYZ[0] * rgb[pix] + RGB2XYZ[1] * rgb[pix + 1] + RGB2XYZ[2] * rgb[pix + 2];
                double z = RGB2XYZ[6] * rgb[pix] + RGB2XYZ[7] * rgb[pix + 1] + RGB2XYZ[8] * rgb[pix + 2];

                luminanceAvgRatio = Y / avgLuminance;
                newLum = (Math.log(luminanceAvgRatio + 1.0) / Math.log(2.0 + Math.pow(luminanceAvgRatio / maxLuminance, biasP) * 8.0)) / divider;
                       
                // Re-scale to new luminance
                double scale = newLum / Y;
                Y *= scale;
                x *= scale;
                z *= scale;

                // konwersja z powrotem do RGB
                final float r = (float) (XYZ2RGB[0] * x + XYZ2RGB[1] * Y + XYZ2RGB[2] * z);
                final float g = (float) (XYZ2RGB[3] * x + XYZ2RGB[4] * Y + XYZ2RGB[5] * z);
                final float b = (float) (XYZ2RGB[6] * x + XYZ2RGB[7] * Y + XYZ2RGB[8] * z);

                int rgbValue = 0;

                rgbValue |= Math.min(255, Math.max(0, (int) (r * 255))) << 16;
                rgbValue |= Math.min(255, Math.max(0, (int) (g * 255))) << 8;
                rgbValue |= Math.min(255, Math.max(0, (int) (b * 255)));

                // zapisanie do obrazu wynikowego
                retImg.setRGB((pix / 3) % width, (pix / 3) / width, rgbValue);
            }
            else
            {
                retImg.setRGB((pix / 3) % width, (pix / 3) / width, 0);
            }
        }

        return retImg;
    }
}
