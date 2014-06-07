package pl.edu.pw.elka.cpoo.images;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.drew.metadata.exif.ExifSubIFDDirectory;

public class ImageWrapper {

    protected List<Image> images;
    private Map<Image, ExifSubIFDDirectory> imageToExifMap;

    public ImageWrapper() {
        images = new ArrayList<>();
    }

    public ImageWrapper(final List<Image> images) {
        this();

        this.images.addAll(images);
    }

    public ImageWrapper(List<Image> images, Map<Image, ExifSubIFDDirectory> imageToExifMap) {
        this(images);
        this.imageToExifMap = imageToExifMap;
    }

    public List<Image> getImages() {
        return images;
    }

    public Image getImage(final int index) {
        return images.get(index);
    }

    public Map<Image, ExifSubIFDDirectory> getImageToExifMap() {
        return imageToExifMap;
    }

    public void setImageToExifMap(Map<Image, ExifSubIFDDirectory> imageToExifMap) {
        this.imageToExifMap = imageToExifMap;
    }

}
