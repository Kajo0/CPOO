package pl.edu.pw.elka.cpoo.interfaces;

import java.awt.Image;

import pl.edu.pw.elka.cpoo.images.ImageWrapper;

public interface HdrProcessor {

    Image process(ImageWrapper imageWrapper);

    Image process(Image image);

    String getName();

}
