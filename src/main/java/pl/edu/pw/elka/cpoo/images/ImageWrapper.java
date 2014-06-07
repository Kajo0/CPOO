package pl.edu.pw.elka.cpoo.images;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.drew.metadata.exif.ExifSubIFDDirectory;

import pl.edu.pw.elka.cpoo.Utilities;

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

    public Pixel[][] getPixelArray(final int index) {
        return ImageWrapper.imageToPixelArray(getImage(index));
    }

    public static Pixel[][] imageToPixelArray(final Image img) {
        return Utilities.createPixelArrayFromImage(img);
    }

    public static Image pixelArryToImage(final Pixel[][] pixelArray) {
        return Utilities.createImageFromPixels(pixelArray);
    }

	public Map<Image, ExifSubIFDDirectory> getImageToExifMap() {
		return imageToExifMap;
	}

	public void setImageToExifMap(Map<Image, ExifSubIFDDirectory> imageToExifMap) {
		this.imageToExifMap = imageToExifMap;
	}

}
