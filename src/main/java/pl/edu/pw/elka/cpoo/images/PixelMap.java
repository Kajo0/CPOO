package pl.edu.pw.elka.cpoo.images;

import java.awt.Color;

public class PixelMap {

    protected Pixel[][] pixels;
    protected int width;
    protected int height;

    public PixelMap(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.pixels = new Pixel[width][height];

        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                this.pixels[x][y] = new Pixel(0);
    }

    public PixelMap(final Pixel[][] pixels) {
        // this.pixels = pixels;
        this.width = pixels.length;
        this.height = pixels[0].length;

        this.pixels = new Pixel[width][height];
        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                this.pixels[x][y] = new Pixel(pixels[x][y]);
    }

    public Pixel getPixel(final int x, final int y) {
        return pixels[x][y];
    }

    public void setPixel(final int x, final int y, final int r, final int g, final int b) {
        setPixel(x, y, new Pixel(r, g, b));
        // pixels[x][y].r = r;
        // pixels[x][y].g = g;
        // pixels[x][y].b = b;
    }

    public void setPixel(final int x, final int y, final Pixel pixel) {
        pixels[x][y] = pixel;
    }

    public void setPixel(final int x, final int y, final Color color) {
        setPixel(x, y, color.getRed(), color.getGreen(), color.getBlue());
    }

    public Pixel[][] getPixels() {
        return pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
