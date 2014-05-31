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

public class ToneMappingAlg1 implements HdrProcessor {

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

        hdrImage.showRawTool();

        return doTonalMappingAlg1(hdrImage);
    }

    @Override
    public Image process(final Image image) {
        return process(new ImageWrapper(Arrays.asList(image)));
    }

    @Override
    public String getName() {
        return "Tonal Mapping 1";
    }

    /**
     * pierwszy algorytm do tonal mappingu, zaproponowany przez Reinharda
     * zrodlo: Ferwerda J, Reinhard E, Shirley P, Stark M 'Photographic Tone Reproduction for Digital Images'
     * 
     * @param hdrImage
     *            obraz HDR
     * @return obraz LDR
     */
    private Image doTonalMappingAlg1(HdrImage hdrImage) {

        // dane hdr
        float[] rgb = hdrImage.getHdrData();
        
        double logAvg = 0;
        double maxLum = 0;
        float avgLum = 0;

        // konwersja obrazu hdr do Yxy i wyliczenie maksymalnej, sredniej oraz
        // œredniej logaryticznej wartoœci luminancji
        for (int pix = 0; pix < rgb.length; pix += 3)
        {
           double gray = 0.21*rgb[pix]+0.71*rgb[pix+1]+0.07*rgb[pix+2];
           avgLum += gray;
           
           final double y = RGB2XYZ[3] * rgb[pix] + RGB2XYZ[4] * rgb[pix + 1] + RGB2XYZ[5] * rgb[pix + 2];
           logAvg += Math.log(1e-4 + y);
           if (y > maxLum)
              maxLum = y;
        }
        logAvg = Math.exp(logAvg / (rgb.length / 3.0));
        avgLum = (avgLum / (rgb.length / 3)) / 255;

        // srednia luminancja w obrazie - jesli obliczenie wychodza poza zakres
        // (10%;90%) wtedy wartosc domyslna 18%
        if (avgLum < 0.1 || avgLum > 0.9)
            avgLum = 0.18f;

        final double scale = avgLum / logAvg;

        return scaleLum(scale, rgb, maxLum, hdrImage.getWidth(), hdrImage.getHeight());
    }

    /**
     * skaluje wartosci luminancji wg odpowiedniego wspolczynnika (Reinharda)
     * 
     * @param scale
     *            wspolczynnik skali
     * @param rgb
     *            tablica wartoœci obrazu hdr w RGB
     * @param whiteLum
     *            maksymalna wartosc luminancji w obrazie.
     * @param height
     *            wysokosc obrazy
     * @param width
     *            szerokosc obrazu
     * @return Image obraz po przeskalowaniu i konwersji do RGB
     */
    private static final Image scaleLum(final double scale, final float[] rgb,
            final double whiteLum, int width, int height)
    {
        // pusty obraz wynikowy o odpowiednim rozmiarze
        BufferedImage retImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        // kolor bialy zeskalowany
        final double whiteSq = whiteLum * scale * whiteLum * scale;
        
        // konwersja do Yxy i skalowanie
        for (int pix = 0; pix < rgb.length; pix += 3)
        {
            double Y = RGB2XYZ[3] * rgb[pix] + RGB2XYZ[4] * rgb[pix + 1] + RGB2XYZ[5] * rgb[pix + 2];
            if (Y > 0)
            {
                double x = RGB2XYZ[0] * rgb[pix] + RGB2XYZ[1] * rgb[pix + 1] + RGB2XYZ[2] * rgb[pix + 2];
                double z = RGB2XYZ[6] * rgb[pix] + RGB2XYZ[7] * rgb[pix + 1] + RGB2XYZ[8] * rgb[pix + 2];

                final double sum = Y + x + z;
                x = (float) (x / sum);
                final double y = (float) (Y / sum);

                // skalowanie
                Y *= scale;

                // zastosowanie wspolczynnika Reinharda. Jesli wartosc
                // przeskalowana jest wieksza niz kolor bialy, jest mu
                // przypisywany bialy
                if (whiteSq > 0)
                    Y *= (1.0 + (Y / whiteSq)) / (1.0 + Y);
                else
                    Y *= 1.0 / (1.0 + Y);
                // konwersja z Yxy do CIE XYZ
                if (y > 0.0 && x > 0.0 && y > 0.0)
                {
                    x *= Y / y;
                    z = (Y / y) - x - Y;
                }
                else
                {
                    x = z = 0.0;
                }

                // konwersja z powrotem do RGB
                final float r = (float) Math.min(1.0, (XYZ2RGB[0] * x + XYZ2RGB[1] * Y + XYZ2RGB[2] * z));
                final float g = (float) Math.min(1.0, (XYZ2RGB[3] * x + XYZ2RGB[4] * Y + XYZ2RGB[5] * z));
                final float b = (float) Math.min(1.0, (XYZ2RGB[6] * x + XYZ2RGB[7] * Y + XYZ2RGB[8] * z));

                int rgbValue = 0;

                rgbValue |= Math.min(255, Math.max(0, (int) (r * 255.0))) << 16;
                rgbValue |= Math.min(255, Math.max(0, (int) (g * 255.0))) << 8;
                rgbValue |= Math.min(255, Math.max(0, (int) (b * 255.0)));

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
