package gnu.vnc.awt;

import gnu.awt.virtual.swing.VirtualDesktop;
import gnu.rfb.Colour;
import gnu.rfb.PixelFormat;
import gnu.rfb.server.RFBClient;
import gnu.rfb.server.RFBClients;
import gnu.rfb.server.RFBServer;

import java.awt.Insets;
import java.awt.image.DirectColorModel;
import java.io.IOException;

/**
 * AWT toolkit implemented entirely with JFC peers supporting multiple RFB
 * client, thus allowing a lightweight remote simulation of the operating system
 * desktop.
 **/

public class VNCDesktop extends VirtualDesktop implements RFBServer {

    //
    // Construction
    //

    private RFBClients clients = new RFBClients();

    private VNCEvents events;

    //
    // VirtualDesktop
    //

    private boolean shared = false;

    //
    // RFBServer
    //

    // Clients

    public VNCDesktop(int bitsPerPixel, int rMask, int gMask, int bMask,
            String title, int width, int height) {
        super(bitsPerPixel, rMask, gMask, bMask, title, width, height);
        init();
    }

    public VNCDesktop(String title, int width, int height) {
        super(title, width, height);
        init();
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
        VNCRepaintManager.currentManager().frameBufferUpdate(desktopFrame,
                client, incremental, x, y, w, h);
    }

    @Override
    public String getDesktopName(RFBClient client) {
        return desktopFrame.getTitle();
    }

    @Override
    public int getFrameBufferHeight(RFBClient client) {
        Insets insets = desktopFrame.getInsets();
        return desktopFrame.getHeight() - insets.top - insets.bottom;
    }

    @Override
    public int getFrameBufferWidth(RFBClient client) {
        Insets insets = desktopFrame.getInsets();
        return desktopFrame.getWidth() - insets.left - insets.right;
    }

    @Override
    public PixelFormat getPreferredPixelFormat(RFBClient client) {
        return PixelFormat.RGB888;
    }

    private void init() {
        events = new VNCEvents(desktopFrame, clients);
        VNCRepaintManager.currentManager().manage(desktopFrame, clients);

        // VNC frames cannot change size
        desktopFrame.setResizable(false);

        show();
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
        pixelFormat.setDirectColorModel((DirectColorModel) getColorModel());
    }

    @Override
    public void setShared(RFBClient client, boolean shared) throws IOException {
        if (shared)
            this.shared = true;
    }
}
