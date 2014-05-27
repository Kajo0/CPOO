package pl.edu.pw.elka.cpoo;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Collections;

import pl.edu.pw.elka.cpoo.images.Pixel;

/**
 * Static class mostly with filters and creating image from array of pixels\
 * 
 * @author Mikolaj Markiewicz
 */
public class Utilities {

    /**
     * Invert image colors
     * 
     * @param pixels
     *            Image as pixels
     * @return Inverted image as pixels
     */
    public static Pixel[][] invertPixels(final Pixel[][] pixels) {
        Pixel[][] result = new Pixel[pixels.length][pixels[0].length];
        for (int x = 0; x < pixels.length; ++x)
            for (int y = 0; y < pixels[0].length; ++y)
                result[x][y] = new Pixel(Math.abs(pixels[x][y].r - 255),
                        Math.abs(pixels[x][y].g - 255), Math.abs(pixels[x][y].b - 255));

        return result;
    }

    /**
     * Increase image contrast by stretching histogram and next increasing
     * contrast of 'stretched' image
     * 
     * @param pixels
     *            Image as pixels
     * @param a
     *            Amount of increasing
     * @return Increased contrast image as pixels
     * @deprecated useless
     */
    @Deprecated
    public static Pixel[][] increaseContrastPixels(final Pixel[][] pixels, final double a) {
        /*
         * Create empty lut table for RGB components
         */
        int[][] lut = new int[3][256];

        for (int i = 0; i < 256; ++i) {
            lut[0][i] = 0;
            lut[1][i] = 0;
            lut[2][i] = 0;
        }

        /*
         * Find min and max value of each component, building histogram
         */
        int rMin = 255;
        int gMin = 255;
        int bMin = 255;
        int rMax = 0;
        int gMax = 0;
        int bMax = 0;

        for (int x = 0; x < pixels.length; ++x)
            for (int y = 0; y < pixels[0].length; ++y) {

                ++lut[0][pixels[x][y].r];
                ++lut[1][pixels[x][y].g];
                ++lut[2][pixels[x][y].b];

                rMin = (pixels[x][y].r < rMin ? pixels[x][y].r : rMin);
                gMin = (pixels[x][y].g < gMin ? pixels[x][y].g : gMin);
                bMin = (pixels[x][y].b < bMin ? pixels[x][y].b : bMin);
                rMax = (pixels[x][y].r > rMax ? pixels[x][y].r : rMax);
                gMax = (pixels[x][y].g > gMax ? pixels[x][y].g : gMax);
                bMax = (pixels[x][y].b > bMax ? pixels[x][y].b : bMax);
            }

        /*
         * Stretch histogram
         */
        lut[0] = updateLUT(255.0 / (rMax - rMin), -rMin);
        lut[1] = updateLUT(255.0 / (gMax - gMin), -gMin);
        lut[2] = updateLUT(255.0 / (bMax - bMin), -bMin);

        /*
         * Increase consrast
         */
        for (int i = 0; i < 256; ++i) {
            double l;
            l = a * (i - rMax / 2) + rMax / 2;
            lut[0][i] = (int) (l < 0 ? 0 : (l <= rMax ? l : rMax));
            lut[0][i] = (lut[0][i] < 0 ? 0 : lut[0][i] > 255 ? 255 : lut[0][i]);

            l = a * (i - gMax / 2) + gMax / 2;
            lut[1][i] = (int) (l < 0 ? 0 : (l <= gMax ? l : gMax));
            lut[2][i] = (lut[1][i] < 0 ? 0 : lut[1][i] > 255 ? 255 : lut[1][i]);

            l = a * (i - bMax / 2) + bMax / 2;
            lut[2][i] = (int) (l < 0 ? 0 : (l <= bMax ? l : bMax));
            lut[2][i] = (lut[2][i] < 0 ? 0 : lut[2][i] > 255 ? 255 : lut[2][i]);
        }

        /*
         * Create result pixels image
         */
        Pixel[][] result = new Pixel[pixels.length][pixels[0].length];
        for (int x = 0; x < pixels.length; ++x)
            for (int y = 0; y < pixels[0].length; ++y)
                result[x][y] = new Pixel(lut[0][pixels[x][y].r], lut[1][pixels[x][y].g],
                        lut[2][pixels[x][y].b]);

        return result;
    }

    /**
     * Used to stretch histogram in lut table
     * 
     * @param a
     *            First variable
     * @param b
     *            Second variable
     * @return Streatched lut table
     */
    public static int[] updateLUT(final double a, final int b) {
        int[] lut = new int[256];

        for (int i = 0; i < 256; i++)
            if ((a * (i + b)) > 255)
                lut[i] = 255;
            else if ((a * (i + b)) < 0)
                lut[i] = 0;
            else
                lut[i] = (int) (a * (i + b));

        return lut;
    }

    /********************
     * Unsharp Begin *
     *******************/

    /**
     * Filter image by unsharp mask
     * 
     * @param pixels
     *            Image as pixels
     * @param amount
     *            Amount
     * @param radius
     *            Radius of blur
     * @param threshold
     *            Threshold
     * @return Unsharped image as pixels
     */
    public static Pixel[][] unsharpPixels(final Pixel[][] pixels, final float amount,
            final int radius, final int threshold) {
        int width = pixels.length;
        int height = pixels[0].length;

        Pixel[][] result = new Pixel[width][height];

        int[] tmp1 = new int[width * height];
        int[] tmp2 = new int[width * height];

        for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j)
                tmp1[i * width + j] = pixels[j][i].toIntPixel();

        float[] kernel = unsharpPixelsCreateGaussianKernel(radius);

        // Horizontal pass
        tmp2 = unsharpPixelsBlur(tmp1, width, height, kernel, radius);
        // Vertical pass
        tmp2 = unsharpPixelsBlur(tmp2, height, width, kernel, radius);

        tmp1 = unsharpPixelsSharpen(tmp1, tmp2, width, height, amount, threshold);

        for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j)
                result[j][i] = new Pixel(tmp1[i * width + j]);

        return result;
    }

    /**
     * Create Gaussian kernel for unsharp filter
     * 
     * @param radius
     *            Radius
     * @return Array of calculated variables
     */
    static float[] unsharpPixelsCreateGaussianKernel(final int radius) {
        if (radius < 1)
            throw new IllegalArgumentException("Radius must be >= 1");

        float[] data = new float[radius * 2 + 1];

        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        for (int i = -radius; i <= radius; ++i) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }

        for (int i = 0; i < data.length; ++i)
            data[i] /= total;

        return data;
    }

    /**
     * Specific blur by pixels with Gaussian kernel
     * 
     * @param pixels
     *            Image as integer value of pixels
     * @param width
     *            Width of image
     * @param height
     *            Height of image
     * @param kernel
     *            Gaussian kernel
     * @param radius
     *            Radius
     * @return Blurred image as integer value pixels
     */
    static int[] unsharpPixelsBlur(final int[] pixels, final int width, final int height,
            final float[] kernel, final int radius) {
        int[] result = new int[width * height];

        float a;
        float r;
        float g;
        float b;

        int ca;
        int cr;
        int cg;
        int cb;

        for (int y = 0; y < height; ++y) {
            int index = y;
            int offset = y * width;

            for (int x = 0; x < width; ++x) {
                a = r = g = b = 0.0f;

                for (int i = -radius; i <= radius; i++) {
                    int subOffset = x + i;
                    if (subOffset < 0 || subOffset >= width) {
                        subOffset = (x + width) % width;
                    }

                    int pixel = pixels[offset + subOffset];
                    float blurFactor = kernel[radius + i];

                    a += blurFactor * ((pixel >> 24) & 0xFF);
                    r += blurFactor * ((pixel >> 16) & 0xFF);
                    g += blurFactor * ((pixel >> 8) & 0xFF);
                    b += blurFactor * ((pixel) & 0xFF);
                }

                ca = (int) (a + 0.5f);
                cr = (int) (r + 0.5f);
                cg = (int) (g + 0.5f);
                cb = (int) (b + 0.5f);

                result[index] = ((ca > 255 ? 255 : ca) << 24) | ((cr > 255 ? 255 : cr) << 16)
                        | ((cg > 255 ? 255 : cg) << 8) | (cb > 255 ? 255 : cb);
                index += height;
            }
        }

        return result;
    }

    /**
     * Specific sharpen filter used by unsharp filter
     * 
     * @param original
     *            Image as integer values of original pixels
     * @param blurred
     *            Image as integer values of blurred image pixels
     * @param width
     *            Width of image
     * @param height
     *            Height of image
     * @param amount
     *            Amount
     * @param threshold
     *            Threshold
     * @return Sharpen image as integer values of pixels
     */
    static int[] unsharpPixelsSharpen(final int[] original, final int[] blurred, final int width,
            final int height, final float amount, final int threshold) {
        int[] result = new int[width * height];

        int index = 0;

        int srcR, srcB, srcG;
        int dstR, dstB, dstG;

        float localAmount = amount * 1.6f;

        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x) {
                int srcColor = original[index];
                srcR = (srcColor >> 16) & 0xFF;
                srcG = (srcColor >> 8) & 0xFF;
                srcB = (srcColor) & 0xFF;

                int dstColor = blurred[index];
                dstR = (dstColor >> 16) & 0xFF;
                dstG = (dstColor >> 8) & 0xFF;
                dstB = (dstColor) & 0xFF;

                if (Math.abs(srcR - dstR) >= threshold) {
                    srcR = (int) (localAmount * (srcR - dstR) + srcR);
                    srcR = srcR > 255 ? 255 : srcR < 0 ? 0 : srcR;
                }

                if (Math.abs(srcG - dstG) >= threshold) {
                    srcG = (int) (localAmount * (srcG - dstG) + srcG);
                    srcG = srcG > 255 ? 255 : srcG < 0 ? 0 : srcG;
                }

                if (Math.abs(srcB - dstB) >= threshold) {
                    srcB = (int) (localAmount * (srcB - dstB) + srcB);
                    srcB = srcB > 255 ? 255 : srcB < 0 ? 0 : srcB;
                }

                int alpha = srcColor & 0xFF000000;
                result[index] = alpha | (srcR << 16) | (srcG << 8) | srcB;

                index++;
            }

        return result;
    }

    /****************
     * Unsharp End *
     ***************/

    /**
     * Ranking filter on given image
     * 
     * @param pixels
     *            Image as pixels
     * @param maskSize
     *            Odd size of mask
     * @param value
     *            Value to get from ranking array
     * @return 'Ranked' image as pixels
     * @deprecated useless
     */
    @Deprecated
    public static Pixel[][] rankinkgFilterPixels(final Pixel[][] pixels, final int maskSize,
            final int value) {
        if (maskSize % 2 == 0)
            throw new IllegalArgumentException("Mask size % 2 = 0!");

        if (value < 0 || value >= maskSize * maskSize)
            throw new IllegalArgumentException("Value out of bounds!");

        int width = pixels.length;
        int height = pixels[0].length;

        Pixel[][] tmp = new Pixel[width][height];
        ArrayList<Pixel> pixelMask = new ArrayList<>(maskSize * maskSize);

        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x) {
                if (y < maskSize / 2 || y > height - 1 - maskSize / 2 || x < maskSize / 2
                        || x > width - 1 - maskSize / 2) {
                    tmp[x][y] = pixels[x][y];
                    continue;
                }

                pixelMask.clear();
                for (int i = -maskSize / 2; i <= maskSize / 2; ++i)
                    for (int j = -maskSize / 2; j <= maskSize / 2; ++j)
                        pixelMask.add(pixels[x + i][y + j]);

                Collections.sort(pixelMask);

                tmp[x][y] = pixelMask.get(value);
            }

        return tmp;
    }

    /**
     * Convert RGB values of pixel int HSV palette
     * 
     * @param red
     *            Red value
     * @param green
     *            Green value
     * @param blue
     *            Blue value
     * @return Array of HSV values of pixel
     */
    public static double[] rgbToHsv(final double red, final double green, final double blue) {
        double hue = -1, sat = -1, val = -1;
        double result[] = new double[3];

        double tmp = Math.min(Math.min(red, green), blue);
        val = Math.max(Math.max(red, green), blue);

        if (tmp == val)
            hue = 0;
        else {
            if (red == val)
                hue = 0 + ((green - blue) * 60 / (val - tmp));
            else if (green == val)
                hue = 120 + ((blue - red) * 60 / (val - tmp));
            else
                hue = 240 + ((red - green) * 60 / (val - tmp));
        }

        if (hue < 0)
            hue += 360;

        if (val == 0)
            sat = 0;
        else
            sat = (val - tmp) * 100 / val;

        val = (100 * val) / 255;
        result[0] = hue;
        result[1] = sat;
        result[2] = val;

        return result;
    }

    /**
     * Thresholding filter
     * 
     * @param pixels
     *            Image as pixels
     * @param threshold
     *            Threshold value
     * @return Binarized image as pixels
     */
    public static Pixel[][] binarizePixels(final Pixel[][] pixels, final int threshold) {
        int width = pixels.length;
        int height = pixels[0].length;

        Pixel[][] tmp = new Pixel[width][height];

        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x) {
                int r = pixels[x][y].r;
                int g = pixels[x][y].g;
                int b = pixels[x][y].b;

                double[] hsv = rgbToHsv(r, g, b);

                if (r > threshold && g > threshold && b > threshold)
                    tmp[x][y] = new Pixel(255, 255, 255);
                else if ((hsv[0] > 85 && hsv[0] < 195/* || hsv[0] > 290 */) && hsv[2] > 50) // Green
                                                                                            // or
                                                                                            // inverse
                                                                                            // fox
                                                                                            // boxy
                    tmp[x][y] = new Pixel(255, 255, 255);
                else
                    tmp[x][y] = new Pixel(0, 0, 0);
            }

        return tmp;
    }

    /**
     * Blur filter by mask Nothing special made from that
     * 
     * @param pixels
     *            Image as pixels
     * @return Blurred image as pixels
     * @deprecated useless
     */
    @Deprecated
    public static Pixel[][] blurPixels(final Pixel[][] pixels) {
        int[][] blurMask = { { 1, 2, 1 }, { 2, 4, 2 }, { 1, 2, 1 } };

        int width = pixels.length;
        int height = pixels[0].length;
        int maskSize = blurMask.length;

        Pixel[][] tmp = new Pixel[width][height];

        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x) {

                if (y < maskSize / 2 || y > height - maskSize / 2 - 1 || x < maskSize / 2
                        || x > width - maskSize / 2 - 1) {
                    tmp[x][y] = pixels[x][y];
                    continue;
                }

                int maskSum = 0;

                long red = pixels[x - 1][y - 1].r * blurMask[0][0] + pixels[x][y - 1].r
                        * blurMask[0][1] + pixels[x + 1][y - 1].r * blurMask[0][2]
                        + pixels[x - 1][y].r * blurMask[0][0] + pixels[x][y].r * blurMask[0][1]
                        + pixels[x + 1][y].r * blurMask[0][2] + pixels[x - 1][y + 1].r
                        * blurMask[0][0] + pixels[x][y + 1].r * blurMask[0][1]
                        + pixels[x + 1][y + 1].r * blurMask[0][2];
                long green = pixels[x - 1][y - 1].g * blurMask[0][0] + pixels[x][y - 1].g
                        * blurMask[0][1] + pixels[x + 1][y - 1].g * blurMask[0][2]
                        + pixels[x - 1][y].g * blurMask[0][0] + pixels[x][y].g * blurMask[0][1]
                        + pixels[x + 1][y].g * blurMask[0][2] + pixels[x - 1][y + 1].g
                        * blurMask[0][0] + pixels[x][y + 1].g * blurMask[0][1]
                        + pixels[x + 1][y + 1].g * blurMask[0][2];
                long blue = pixels[x - 1][y - 1].b * blurMask[0][0] + pixels[x][y - 1].b
                        * blurMask[0][1] + pixels[x + 1][y - 1].b * blurMask[0][2]
                        + pixels[x - 1][y].b * blurMask[0][0] + pixels[x][y].b * blurMask[0][1]
                        + pixels[x + 1][y].b * blurMask[0][2] + pixels[x - 1][y + 1].b
                        * blurMask[0][0] + pixels[x][y + 1].b * blurMask[0][1]
                        + pixels[x + 1][y + 1].b * blurMask[0][2];

                for (int i = 0; i < maskSize; ++i)
                    for (int j = 0; j < maskSize; ++j)
                        maskSum += blurMask[i][j];

                red /= maskSum;
                green /= maskSum;
                blue /= maskSum;

                // normalize values
                red = (red < 0 ? 0 : red > 255 ? 255 : red);
                green = (green < 0 ? 0 : green > 255 ? 255 : green);
                blue = (blue < 0 ? 0 : blue > 255 ? 255 : blue);

                tmp[x][y] = new Pixel((int) red, (int) green, (int) blue);
            }

        return tmp;
    }

    /**
     * Create image from array of pixels
     * 
     * @param pixels
     *            Image as pixels
     * @return Created image from pixels
     */
    public static BufferedImage createImageFromPixels(final Pixel[][] pixels) {
        int[] tmp = new int[pixels.length * pixels[0].length];

        for (int y = 0; y < pixels[0].length; ++y)
            for (int x = 0; x < pixels.length; ++x)
                tmp[x + y * pixels.length] = pixels[x][y].toIntPixel();

        Image img = Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(pixels.length, pixels[0].length, tmp, 0, pixels.length));

        BufferedImage bufImg = new BufferedImage(img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufImg.getGraphics();
        g.drawImage(img, 0, 0, null);

        return bufImg;
    }

    /**
     * Grab pixels from given image and store as {@link Pixel} array
     * 
     * @param img
     *            Image to grab pixels from
     * @return Grabbed pixels array
     */
    public static Pixel[][] createPixelArrayFromImage(final Image img) {
        int width = img.getWidth(null);
        int height = img.getHeight(null);

        PixelGrabber grabber = new PixelGrabber(img, 0, 0, width, height, false);

        try {
            if (!grabber.grabPixels())
                throw new RuntimeException("Unable to grab pixels.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int[] rawPixels = (int[]) grabber.getPixels();
        grabber.abortGrabbing();
        grabber = null; // info 4 GC

        Pixel[][] pixels = new Pixel[width][height];

        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                pixels[x][y] = new Pixel(rawPixels[x + y * width]);

        return pixels;
    }

}
