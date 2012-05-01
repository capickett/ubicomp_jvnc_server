package gnu.rfb.server;

import gnu.rfb.Colour;
import gnu.rfb.PixelFormat;

import java.io.IOException;

/**
 * To be implemented by RFB servers, which must also define a constructor that
 * accepts an integer (for the display number), a string (for the display name),
 * and then two integers (suggested width and height).
 **/

public interface RFBServer {

    // Clients

    public void addClient(RFBClient client);

    public boolean allowShared();

    // Attributes

    public void clientCutText(RFBClient client, String text) throws IOException;

    public void fixColourMapEntries(RFBClient client, int firstColour,
            Colour[] colourMap) throws IOException;

    public void frameBufferUpdateRequest(RFBClient client, boolean incremental,
            int x, int y, int w, int h) throws IOException;

    public String getDesktopName(RFBClient client);

    public int getFrameBufferHeight(RFBClient client);

    // Messages from client to server

    public int getFrameBufferWidth(RFBClient client);

    public PixelFormat getPreferredPixelFormat(RFBClient client);

    public void keyEvent(RFBClient client, boolean down, int key)
            throws IOException;

    public void pointerEvent(RFBClient client, int buttonMask, int x, int y)
            throws IOException;

    public void removeClient(RFBClient client);

    public void setClientProtocolVersionMsg(RFBClient client,
            String protocolVersionMsg) throws IOException;

    public void setEncodings(RFBClient client, int[] encodings)
            throws IOException; // not supported

    public void setPixelFormat(RFBClient client, PixelFormat pixelFormat)
            throws IOException;

    public void setShared(RFBClient client, boolean shared) throws IOException;
}
