package pl.edu.pw.elka.cpoo.algorithms;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import pl.edu.pw.elka.cpoo.Utilities;

public class ImageWrapper {

    protected List<Image> images;

    public ImageWrapper() {
        images = new ArrayList<>();
    }

    public ImageWrapper(final List<Image> images) {
        this();

        this.images.addAll(images);
    }

    public List<Image> getImages() {
        return images;
    }

    public Image getImage(final int index) {
        return images.get(index);
    }

    public Pixel[][] getPixelArray(final int index) {
        return ImageWrapper.imageToPixelArray(getImage(index));
    }

    public static Pixel[][] imageToPixelArray(final Image img) {
        return Utilities.createPixelArrayFromImage(img);
    }

    public static Image pixelArryToImage(final Pixel[][] pixelArray) {
        return Utilities.createImageFromPixels(pixelArray);
    }

}
