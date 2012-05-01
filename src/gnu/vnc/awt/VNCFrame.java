package gnu.vnc.awt;

import gnu.awt.virtual.VirtualFrame;
import gnu.rfb.Colour;
import gnu.rfb.PixelFormat;
import gnu.rfb.server.RFBClient;
import gnu.rfb.server.RFBClients;
import gnu.rfb.server.RFBServer;
import gnu.vnc.VNCQueue;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.DirectColorModel;
import java.io.IOException;

/**
 * A {@link gnu.awt.virtual.VirtualFrame virtual AWT frame} that supports
 * multiple RFB clients.
 **/

public class VNCFrame extends VirtualFrame implements RFBServer {

    //
    // Construction
    //

    private RFBClients clients = new RFBClients();

    //
    // Frame
    //

    private VNCEvents events;

    //
    // RFBServer
    //

    // Clients

    private boolean shared = false;

    protected VNCQueue queue;

    // Attributes

    public VNCFrame(Toolkit toolkit, String name, int width, int height) {
        super(toolkit, name);

        events = new VNCEvents(this, clients);
        queue = new VNCQueue(clients);

        // VNC frames cannot change size
        setSize(width, height);
        super.setResizable(false);

        show();
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

    @Override
    public void dispose() {
        clients.closeAll();
        super.dispose();
    }

    // Messages from client to server

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
        return getTitle();
    }

    @Override
    public int getFrameBufferHeight(RFBClient client) {
        Insets insets = getInsets();
        return getHeight() - insets.top - insets.bottom;
    }

    @Override
    public int getFrameBufferWidth(RFBClient client) {
        Insets insets = getInsets();
        return getWidth() - insets.left - insets.right;
    }

    @Override
    public PixelFormat getPreferredPixelFormat(RFBClient client) {
        return PixelFormat.RGB888;
    }

    @Override
    public void keyEvent(RFBClient client, boolean down, int key)
            throws IOException {
        events.translateKeyEvent(client, down, key);
    }

    @Override
    public void pointerEvent(RFBClient client, int buttonMask, int x, int y)
            throws IOException {
        events.translatePointerEvent(client, buttonMask, x, y);
    }

    @Override
    public void removeClient(RFBClient client) {
        clients.removeClient(client);
        if (clients.isEmpty() && !shared)
            dispose();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    @Override
    public void setClientProtocolVersionMsg(RFBClient client,
            String protocolVersionMsg) throws IOException {
    }

    @Override
    public void setEncodings(RFBClient client, int[] encodings)
            throws IOException {
    }

    @Override
    public void setPixelFormat(RFBClient client, PixelFormat pixelFormat)
            throws IOException {
        pixelFormat.setDirectColorModel((DirectColorModel) getToolkit()
                .getColorModel());
    }

    @Override
    public void setShared(RFBClient client, boolean shared) throws IOException {
        if (shared)
            this.shared = true;
    }
}
