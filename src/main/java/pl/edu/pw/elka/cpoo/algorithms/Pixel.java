package pl.edu.pw.elka.cpoo.algorithms;

/**
 * Class representing pixel entity with RGB components
 * 
 * @author Mikolaj Markiewicz
 */
public class Pixel implements Comparable<Pixel> {

    /** Red component */
    public int r;

    /** Green component */
    public int g;

    /** Blue component */
    public int b;

    /**
     * C-tor Create pixel instance by integer value of color
     * 
     * @param intPixel
     *            Pixel described as integer value
     */
    public Pixel(final int intPixel) {
        this.r = ((intPixel >> 16) & 0xFF);
        this.g = ((intPixel >> 8) & 0xFF);
        this.b = ((intPixel >> 0) & 0xFF);
    }

    /**
     * C-tor Create pixel by components
     * 
     * @param r
     *            Red value
     * @param g
     *            Green value
     * @param b
     *            Blue value
     */
    public Pixel(final int r, final int g, final int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Copying C-tor
     * 
     * @param pixel
     *            Another pixel to get values from
     */
    public Pixel(final Pixel pixel) {
        this.r = pixel.r;
        this.g = pixel.g;
        this.b = pixel.b;
    }

    /**
     * Make integer value of pixel from RGB components
     * 
     * @return Integer value of pixel
     */
    public int toIntPixel() {
        return (0xFF << 24 | r << 16 | g << 8 | b << 0);
    }

    /**
     * Get gray value of pixel
     * 
     * @return Gray value of pixel
     */
    public int grayValue() {
        return (r + g + b) / 3;
    }

    /**
     * Check whether pixel is WHITE or not Only for binary pixels! (x>0 or 0 per
     * each color)
     * 
     * @return true if White, false otherwise
     */
    public boolean isWhite() {
        return (r > 0 || g > 0 || b > 0) ? true : false;
    }

    /**
     * Compare pixels by gray value
     * 
     * @param other
     *            Pixel to compare with
     * @return -1 if first darker than second, 0 if equal luminosity, 1 if first
     *         lighter than second
     */
    @Override
    public int compareTo(final Pixel other) {
        int thisGray = this.grayValue();
        int thatGray = other.grayValue();

        return (thisGray < thatGray ? -1 : thisGray == thatGray ? 0 : 1);
    }

}
