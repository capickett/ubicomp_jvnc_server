package gnu.rfb;

import java.awt.color.ColorSpace;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;

/**
 * RFB pixel format information.
 **/

public class PixelFormat {

    //
    // Static attributes
    //

    public static final PixelFormat BGR233 = new PixelFormat(8, 8, false, true,
            7, 7, 3, 0, 3, 6);
    public static final PixelFormat RGB888 = new PixelFormat(32, 24, false,
            true, 0xFF, 0xFF, 0xFF, 16, 8, 0);

    //
    // Attributes
    //

    private static int fixColorModel(int max1, int max2, int mask) {
        int fix = 0;
        for (; fix < 8; fix++) {
            if (max1 == max2)
                break;

            max1 >>= 1;
        }

        while ((mask & 1) == 0) {
            fix++;
            mask >>= 1;
        }

        return fix;
    }

    public int bitsPerPixel; // 8, 16 or 32
    public int depth; // 8 to 32
    public boolean bigEndian;
    public boolean trueColour; // False -> requires colour map
    public int redMax; // Ignored if trueColor = false
    public int greenMax; // Ignored if trueColor = false
    public int blueMax; // Ignored if trueColor = false
    public int redShift; // Ignored if trueColor = false
    public int greenShift; // Ignored if trueColor = false

    //
    // Construction
    //

    public int blueShift; // Ignored if trueColor = false

    private DirectColorModel directColorModel = null;

    private int redMask = -1, greenMask = -1, blueMask = -1;

    private int redFix = -1, greenFix = -1, blueFix = -1;

    //
    // Operations
    //

    public PixelFormat() {
    }

    public PixelFormat(DataInput input) throws IOException {
        bitsPerPixel = input.readUnsignedByte();
        depth = input.readUnsignedByte();
        bigEndian = input.readUnsignedByte() == 1 ? true : false;
        trueColour = input.readUnsignedByte() == 1 ? true : false;
        redMax = input.readUnsignedShort();
        greenMax = input.readUnsignedShort();
        blueMax = input.readUnsignedShort();
        redShift = input.readUnsignedByte();
        greenShift = input.readUnsignedByte();
        blueShift = input.readUnsignedByte();
    }

    public PixelFormat(int bitsPerPixel, int depth, boolean bigEndian,
            boolean trueColour, int redMax, int greenMax, int blueMax,
            int redShift, int greenShift, int blueShift) {
        this.bitsPerPixel = bitsPerPixel;
        this.depth = depth;
        this.bigEndian = bigEndian;
        this.trueColour = trueColour;
        this.redMax = redMax;
        this.greenMax = greenMax;
        this.blueMax = blueMax;
        this.redShift = redShift;
        this.greenShift = greenShift;
        this.blueShift = blueShift;
    }

    public PixelFormat(PixelFormat pixelFormat) {
        this.bitsPerPixel = pixelFormat.bitsPerPixel;
        this.depth = pixelFormat.depth;
        this.bigEndian = pixelFormat.bigEndian;
        this.trueColour = pixelFormat.trueColour;
        this.redMax = pixelFormat.redMax;
        this.greenMax = pixelFormat.greenMax;
        this.blueMax = pixelFormat.blueMax;
        this.redShift = pixelFormat.redShift;
        this.greenShift = pixelFormat.greenShift;
        this.blueShift = pixelFormat.blueShift;
    }

    public DirectColorModel getDirectColorModel() {
        return directColorModel;
    }

    public void print(PrintStream stream) {
        stream.println("Bits-per-pixel: " + bitsPerPixel);
        stream.println("Depth:          " + depth);
        stream.println("Big Endian:     " + bigEndian);
        stream.println("True Colour:    " + trueColour);
        if (trueColour) {
            stream.println("R max:   " + redMax);
            stream.println("G max:   " + greenMax);
            stream.println("B max:   " + blueMax);
            stream.println("R shift: " + redShift);
            stream.println("G shift: " + greenShift);
            stream.println("B shift: " + blueShift);
        }
    }

    public void setDirectColorModel(DirectColorModel directColorModel) {
        this.directColorModel = directColorModel;
        setMasks(directColorModel.getRedMask(),
                directColorModel.getGreenMask(), directColorModel.getBlueMask());
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    public void setMasks(int redMask, int greenMask, int blueMask) {
        this.redMask = redMask;
        this.greenMask = greenMask;
        this.blueMask = blueMask;

        redFix = fixColorModel(0xFF, redMax, redMask);
        greenFix = fixColorModel(0xFF, greenMax, greenMask);
        blueFix = fixColorModel(0xFF, blueMax, blueMask);

        if ((redFix == redShift) && (greenFix == greenShift)
                && (blueFix == blueShift)) {
            redFix = -1;
            greenFix = -1;
            blueFix = -1;
        }
    }

    public DirectColorModel toDirectColorModel() {
        return new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                depth, redMax << redShift, greenMax << greenShift,
                blueMax << blueShift, 0, true, DataBuffer.TYPE_INT);
    }

    public int translatePixel(int pixel) {
        if (redFix != -1) {
            return ((pixel & redMask) >> redFix << redShift)
                    | ((pixel & greenMask) >> greenFix << greenShift)
                    | ((pixel & blueMask) >> blueFix << blueShift);
        } else {
            return pixel;
        }
    }

    public void writeData(DataOutput output) throws IOException {
        output.writeByte(bitsPerPixel);
        output.writeByte(depth);
        output.writeByte(bigEndian ? 1 : 0);
        output.writeByte(trueColour ? 1 : 0);
        output.writeShort(redMax);
        output.writeShort(greenMax);
        output.writeShort(blueMax);
        output.writeByte(redShift);
        output.writeByte(greenShift);
        output.writeByte(blueShift);
    }
}
