package gnu.vnc.awt;

import gnu.rfb.Colour;
import gnu.rfb.PixelFormat;
import gnu.rfb.server.RFBClient;
import gnu.rfb.server.RFBClients;
import gnu.rfb.server.RFBServer;
import gnu.swing.virtual.VirtualJFrame;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.DirectColorModel;
import java.io.IOException;

/**
 * A {@link gnu.swing.virtual.VirtualJFrame virtual JFC frame} that supports
 * multiple RFB clients.
 **/

public class VNCJFrame extends VirtualJFrame implements RFBServer {

    //
    // Construction
    //

    private RFBClients clients = new RFBClients();

    //
    // JFrame
    //

    private VNCEvents events;

    //
    // RFBServer
    //

    // Clients

    private boolean shared = false;

    public VNCJFrame(Toolkit toolkit, String name, int width, int height) {
        super(toolkit, name);

        events = new VNCEvents(this, clients);
        VNCRepaintManager.currentManager().manage(this, clients);

        // VNC frames cannot change size
        setSize(width, height);
        setResizable(false);

        show();
    }

    // Attributes

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
        VNCRepaintManager.currentManager().unmanage(this);
        clients.closeAll();
        super.dispose();
    }

    @Override
    public void fixColourMapEntries(RFBClient client, int firstColour,
            Colour[] colourMap) throws IOException {
    }

    // Messages from client to server

    @Override
    public void frameBufferUpdateRequest(RFBClient client, boolean incremental,
            int x, int y, int w, int h) throws IOException {
        VNCRepaintManager.currentManager().frameBufferUpdate(this, client,
                incremental, x, y, w, h);
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

    @Override
    public void setClientProtocolVersionMsg(RFBClient client,
            String protocolVersionMsg) throws IOException {
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

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
