package gnu.rfb.server;

import gnu.rfb.Colour;
import gnu.rfb.PixelFormat;
import gnu.rfb.Rect;

import java.io.IOException;
import java.net.InetAddress;

/**
 * To be implemented by RFB client models.
 **/

public interface RFBClient {

    // Attributes

    public void close() throws IOException;

    public void flush() throws IOException;

    public int[] getEncodings();

    public InetAddress getInetAddress();

    public PixelFormat getPixelFormat();

    public int getPreferredEncoding();

    public String getProtocolVersionMsg();

    // Messages from server to client

    public boolean getShared();

    public void read(byte bytes[]) throws IOException;

    public void setPreferredEncoding(int encoding);

    public void write(byte bytes[]) throws IOException;

    public void write(int integer) throws IOException;

    // Operations

    public void writeBell() throws IOException;

    public void writeConnectionFailed(String text) throws IOException;

    public void writeFrameBufferUpdate(Rect rects[]) throws IOException;

    public void writeServerCutText(String text) throws IOException;

    public void writeSetColourMapEntries(int firstColour, Colour colours[])
            throws IOException;
}
