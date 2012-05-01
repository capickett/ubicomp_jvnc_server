package gnu.vnc.console;

import gnu.rfb.Colour;
import gnu.rfb.PixelFormat;
import gnu.rfb.keysym;
import gnu.rfb.server.RFBClient;
import gnu.rfb.server.RFBClients;
import gnu.rfb.server.RFBServer;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Base class for console emulators supporting multiple RFB clients.
 **/

public abstract class VNCConsole implements RFBServer, Runnable {

    //
    // Construction
    //

    private String displayName;

    //
    // Operations
    //

    private int columns;

    private int rows;

    //
    // RFBServer
    //

    // Clients

    private Dimension charSize;

    private VNCConsoleBuffer buffer;

    // Attributes

    private RFBClients clients = new RFBClients();

    private boolean shared = false;

    protected InputStream in;

    protected PrintStream out;

    public VNCConsole(String displayName, int columns, int rows, int charW,
            int charH) {
        this.displayName = displayName;
        this.columns = columns;
        this.rows = rows;
        charSize = new Dimension(charW, charH);

        buffer = new VNCConsoleBuffer(columns, rows, charSize, clients);

        in = buffer.inputStream();
        out = buffer.printStream();

        // Start thread
        new Thread(this, "VNCConsole-" + displayName).start();
    }

    // Messages from client to server

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
        clients.closeAll();
    }

    @Override
    public void fixColourMapEntries(RFBClient client, int firstColour,
            Colour[] colourMap) throws IOException {
    }

    @Override
    public void frameBufferUpdateRequest(RFBClient client, boolean incremental,
            int x, int y, int w, int h) throws IOException {
        client.writeFrameBufferUpdate(buffer.getRects(client, incremental));
    }

    @Override
    public String getDesktopName(RFBClient client) {
        return displayName;
    }

    @Override
    public int getFrameBufferHeight(RFBClient client) {
        return rows * charSize.height;
    }

    @Override
    public int getFrameBufferWidth(RFBClient client) {
        return columns * charSize.width;
    }

    //
    // Runnable
    //

    @Override
    public PixelFormat getPreferredPixelFormat(RFBClient client) {
        return PixelFormat.BGR233;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    @Override
    public void keyEvent(RFBClient client, boolean down, int key)
            throws IOException {
        // Ignore modifiers
        int[] mask = new int[2];
        keysym.toMask(key, mask);
        if (mask[0] != KeyEvent.VK_UNDEFINED)
            return;

        if (down) {
            int[] vk = new int[2];
            keysym.toVK(key, vk);
            if (vk[0] == KeyEvent.VK_UNDEFINED)
                // Standard key
                buffer.input(key);
            else
                // Virtual key
                buffer.inputVK(vk[0]);
        }
    }

    public abstract void main();

    @Override
    public void pointerEvent(RFBClient client, int buttonMask, int x, int y)
            throws IOException {
    }

    @Override
    public void removeClient(RFBClient client) {
        clients.removeClient(client);
        if (clients.isEmpty() && !shared)
            dispose();
    }

    @Override
    public void run() {
        main();
        dispose();
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
    public void setPixelFormat(RFBClient client, PixelFormat pixelFormat)
            throws IOException {
        pixelFormat.setDirectColorModel(buffer.getFont(client)
                .getDirectColorModel());
    }

    @Override
    public void setShared(RFBClient client, boolean shared) throws IOException {
        if (shared)
            this.shared = true;
    }
}
