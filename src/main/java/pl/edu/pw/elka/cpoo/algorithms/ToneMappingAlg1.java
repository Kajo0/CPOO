package pl.edu.pw.elka.cpoo.algorithms;

import java.awt.Image;
import java.util.Arrays;
import java.util.Random;

import pl.edu.pw.elka.cpoo.Utilities;
import pl.edu.pw.elka.cpoo.interfaces.HdrProcessor;

public class ToneMappingAlg1 implements HdrProcessor {

    @Override
    public Image process(final ImageWrapper imageWrapper) {
        Image img = imageWrapper.getImage(new Random().nextInt(imageWrapper.getImages().size()));
        Pixel[][] pixels = Utilities.createPixelArrayFromImage(img);

        return Utilities.createImageFromPixels(Utilities.rankinkgFilterPixels(pixels, 5, 9));
    }

    @Override
    public Image process(final Image image) {
        return process(new ImageWrapper(Arrays.asList(image)));
    }

    @Override
    public String getName() {
        return "Tonal Mapping 1";
    }

}
