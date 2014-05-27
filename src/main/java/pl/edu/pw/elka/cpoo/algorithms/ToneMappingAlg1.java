package pl.edu.pw.elka.cpoo.algorithms;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.pw.elka.cpoo.images.HdrImage;
import pl.edu.pw.elka.cpoo.images.ImageWrapper;
import pl.edu.pw.elka.cpoo.images.MyImage;
import pl.edu.pw.elka.cpoo.interfaces.HdrProcessor;

public class ToneMappingAlg1 implements HdrProcessor {

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

        // TODO tone mapping

        return hdrImage.getExposedImage(1);
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
