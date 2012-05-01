package gnu.rfb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Vector;

/**
 * Library of {@link gnu.rfb.Rect RFB rectangles} created from an AWT font.
 **/

public class RectFont extends RectLibrary {

    //
    // Construction
    //

    private Dimension charSize = new Dimension();

    private DirectColorModel colorModel;

    public RectFont(Dimension charSize, PixelFormat pixelFormat, int encoding) {
        this(new Font("monospaced", Font.PLAIN, charSize.height), charSize,
                pixelFormat, encoding);
    }

    public RectFont(Font font, Dimension charSize, char[] chars,
            PixelFormat pixelFormat, int encoding, DirectColorModel colorModel) {
        init(font, charSize, chars, pixelFormat, encoding, colorModel);
    }

    //
    // Operations
    //

    public RectFont(Font font, Dimension charSize, PixelFormat pixelFormat,
            int encoding) {
        this(font, charSize, pixelFormat, encoding, new DirectColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB), 24, 0xFF0000,
                0xFF00, 0xFF, 0, true, DataBuffer.TYPE_INT));
    }

    public RectFont(Font font, Dimension charSize, PixelFormat pixelFormat,
            int encoding, DirectColorModel colorModel) {
        char[] chars = new char[256];
        for (int c = 0; c < chars.length; c++)
            chars[c] = (char) c;

        init(font, charSize, chars, pixelFormat, encoding, colorModel);
    }

    public Dimension getCharSize() {
        return charSize;
    }

    public DirectColorModel getDirectColorModel() {
        return colorModel;
    }

    public Rect getRect(char c) {
        return super.getRect(c);
    }

    public Rect getRect(char c, int originX, int originY) {
        return super.getRect(c, originX, originY);
    }

    public Rect[] getRects(char[] chars, boolean[] valid, int offset,
            int length, int originX, int originY, int xLimit) {
        Vector<Rect> rects = new Vector<Rect>();
        int x = originX;
        int y = originY;
        Rect rect;
        for (int i = offset; i < offset + length; i++) {
            if (!valid[i]) {
                rect = getRect(chars[i], x, y);
                if (rect != null)
                    rects.addElement(rect);

                valid[i] = true;
            }

            x += charSize.width;
            if (x >= xLimit) {
                x = originX;
                y += charSize.height;
            }
        }

        // Convert to array
        Rect[] array = new Rect[rects.size()];
        rects.toArray(array);
        return array;
    }

    public Rect[] getRects(char[] chars, int offset, int length, int originX,
            int originY, int xLimit) {
        Rect[] rects = new Rect[length];
        int x = originX;
        int y = originY;
        for (int i = offset; i < offset + length; i++) {
            rects[i] = getRect(chars[i], x, y);
            x += charSize.width;
            if (x >= xLimit) {
                x = originX;
                y += charSize.height;
            }
        }
        return rects;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    public Rect[] getRects(String string, int originX, int originY) {
        int length = string.length();
        Rect[] rects = new Rect[length];
        for (int i = 0; i < length; i++) {
            rects[i] = getRect(string.charAt(i), originX, originY);
            originX += charSize.width;
        }
        return rects;
    }

    private void init(Font font, Dimension charSize, char[] chars,
            PixelFormat pixelFormat, int encoding, DirectColorModel colorModel) {
        this.charSize = charSize;
        this.colorModel = colorModel;

        // Pixel data
        int[] pixels = new int[charSize.width * charSize.height];
        DataBuffer dataBuffer = new DataBufferInt(pixels, pixels.length);

        // Sample model
        SampleModel sampleModel = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, charSize.width, charSize.height,
                colorModel.getMasks());

        // Raster				
        WritableRaster raster = Raster.createWritableRaster(sampleModel,
                dataBuffer, null);

        // Image
        BufferedImage image = new BufferedImage(colorModel, raster, true, null);
        Graphics g = image.getGraphics();
        g.setFont(font);
        g.setColor(Color.yellow);
        FontMetrics fontMetrics = g.getFontMetrics();
        int baseline = charSize.height - fontMetrics.getDescent();

        Rect rect;
        int ii;
        for (int i = 0; i < chars.length; i++) {
            if (font.canDisplay(chars[i])) {
                // Clean
                for (ii = 0; ii < pixels.length; ii++)
                    pixels[ii] = 0;

                // Draw
                g.drawChars(chars, i, 1, 0, baseline);

                // Encode
                rect = Rect.encode(encoding, pixels, pixelFormat,
                        charSize.width, 0, 0, charSize.width, charSize.height);
                putRect(chars[i], rect);
            }
        }

        // Null rect
        putRect(0, getRect(' '));

        // Default
        putDefaultRect(getRect('?'));
    }

    public void putRect(char c, Rect rect) {
        super.putRect(c, rect);
    }
}
