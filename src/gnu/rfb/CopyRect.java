package gnu.rfb;

import java.io.DataOutput;
import java.io.IOException;

/**
 * CopyRect encoding.
 **/

public class CopyRect extends Rect {

    //
    // Attributes
    //

    public int srcX;
    public int srcY;

    //
    // Construction
    //

    public CopyRect(int x, int y, int w, int h, int srcX, int srcY) {
        super(x, y, w, h);
        this.srcX = srcX;
        this.srcY = srcY;
    }

    //
    // Rect
    //

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeInt(rfb.EncodingCopyRect);
        output.writeShort(srcX);
        output.writeShort(srcY);
    }
}
