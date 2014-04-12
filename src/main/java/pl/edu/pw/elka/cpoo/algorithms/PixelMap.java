package pl.edu.pw.elka.cpoo.algorithms;

import java.awt.Color;

public class PixelMap {

    protected Pixel[][] pixels;
    protected int width;
    protected int height;

    public PixelMap(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.pixels = new Pixel[height][width];

        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                this.pixels[y][x] = new Pixel(0);
    }

    public PixelMap(final Pixel[][] pixels) {
        this.pixels = pixels;
        this.height = pixels.length;
        this.width = pixels[0].length;
    }

    public Pixel getPixel(final int x, final int y) {
        return pixels[y][x];
    }

    public void setPixel(final int x, final int y, final int r, final int g, final int b) {
        setPixel(x, y, new Pixel(r, g, b));
        // pixels[y][x].r = r;
        // pixels[y][x].g = g;
        // pixels[y][x].b = b;
    }

    public void setPixel(final int x, final int y, final Pixel pixel) {
        pixels[y][x] = pixel;
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
