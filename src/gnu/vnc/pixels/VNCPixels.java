package gnu.vnc.pixels;

import gnu.awt.PixelsOwner;
import gnu.rfb.Colour;
import gnu.rfb.PixelFormat;
import gnu.rfb.server.RFBClient;
import gnu.rfb.server.RFBClients;
import gnu.rfb.server.RFBServer;
import gnu.vnc.VNCQueue;

import java.io.IOException;

/**
 * A raw pixel raster supporting multiple RFB clients.
 **/

public class VNCPixels implements RFBServer, PixelsOwner {

    //
    // Construction
    //

    private String name;

    private int width;

    //
    // Operations
    //

    private int height;

    //
    // RFBServer
    //

    // Clients

    private int redMask;

    private int greenMask;

    // Attributes

    private int blueMask;

    private RFBClients clients = new RFBClients();

    private boolean shared = false;

    private int[] pixelArray = null;

    protected VNCQueue queue;

    // Messages from client to server

    public VNCPixels(String name, int width, int height) {
        this(name, width, height, 0xFF0000, 0xFF00, 0xFF);
    }

    public VNCPixels(String name, int width, int height, int redMask,
            int greenMask, int blueMask) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.redMask = redMask;
        this.greenMask = greenMask;
        this.blueMask = blueMask;

        queue = new VNCQueue(clients);

        pixelArray = new int[width * height];
    }

    @Override
    public void addClient(RFBClient client) {
        clients.addClient(client);
    }

    @Override
    public boolean allowShared() {
        return true;
    }

    @Override
    public void clientCutText(RFBClient client, String text) throws IOException {
    }

    public void dispose() {
    }

    @Override
    public void fixColourMapEntries(RFBClient client, int firstColour,
            Colour[] colourMap) throws IOException {
    }

    @Override
    public void frameBufferUpdateRequest(RFBClient client, boolean incremental,
            int x, int y, int w, int h) throws IOException {
        queue.frameBufferUpdate(client, incremental, x, y, w, h, this);
    }

    @Override
    public String getDesktopName(RFBClient client) {
        return name;
    }

    //
    // PixelsOwner
    //

    @Override
    public int getFrameBufferHeight(RFBClient client) {
        return height;
    }

    @Override
    public int getFrameBufferWidth(RFBClient client) {
        return width;
    }

    @Override
    public int getPixelHeight() {
        return height;
    }

    @Override
    public int[] getPixels() {
        return pixelArray;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    @Override
    public int getPixelWidth() {
        return width;
    }

    @Override
    public PixelFormat getPreferredPixelFormat(RFBClient client) {
        return PixelFormat.RGB888;
    }

    @Override
    public void keyEvent(RFBClient client, boolean down, int key)
            throws IOException {
    }

    @Override
    public void pointerEvent(RFBClient client, int buttonMask, int x, int y)
            throws IOException {
    }

    @Override
    public void removeClient(RFBClient client) {
        clients.removeClient(client);
        if (clients.isEmpty() && !shared) {
            clients.closeAll();
            dispose();
        }
    }

    @Override
    public void setClientProtocolVersionMsg(RFBClient client,
            String protocolVersionMsg) throws IOException {
    }

    @Override
    public void setEncodings(RFBClient client, int[] encodings)
            throws IOException {
    }

    @Override
    public void setPixelArray(int[] pixelArray, int pixelWidth, int pixelHeight) {
        this.pixelArray = pixelArray;
        this.width = pixelWidth;
        this.height = pixelHeight;
    }

    @Override
    public void setPixelFormat(RFBClient client, PixelFormat pixelFormat)
            throws IOException {
        pixelFormat.setMasks(redMask, greenMask, blueMask);
    }

    @Override
    public void setShared(RFBClient client, boolean shared) throws IOException {
        if (shared)
            this.shared = true;
    }
}
