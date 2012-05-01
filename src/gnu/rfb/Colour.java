package gnu.rfb;

import java.io.DataInput;
import java.io.IOException;

/**
 * RFB color information.
 **/

public class Colour {

    //
    // Attributes
    //

    public int r;
    public int g;
    public int b;

    //
    // Operations
    //

    public void readData(DataInput input) throws IOException {
        r = input.readUnsignedShort();
        g = input.readUnsignedShort();
        b = input.readUnsignedShort();
    }
}
