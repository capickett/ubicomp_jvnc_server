package gnu.vnc.awt;

import gnu.rfb.Colour;
import gnu.rfb.PixelFormat;
import gnu.rfb.Rect;
import gnu.rfb.keysym;
import gnu.rfb.rfb;
import gnu.rfb.server.RFBClient;
import gnu.rfb.server.RFBServer;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.io.IOException;

/**
 * A very limited implementation of a {@link java.awt.Robot} that supports RFB
 * clients.
 **/

public class VNCRobot extends Component implements RFBServer {

    //
    // Construction
    //

    private String displayName;

    //
    // RFBServer
    //

    // Clients

    private GraphicsDevice device;

    private Robot robot;

    // Attributes

    private int mouseModifiers = 0;

    public VNCRobot(int display, String displayName, int width, int height) {
        this.displayName = displayName;
        device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        try {
            robot = new Robot(device);
        } catch (AWTException x) {
        }
    }

    @Override
    public void addClient(RFBClient client) {
    }

    @Override
    public boolean allowShared() {
        return true;
    }

    @Override
    public void clientCutText(RFBClient client, String text) throws IOException {
    }

    // Messages from client to server

    @Override
    public void fixColourMapEntries(RFBClient client, int firstColour,
            Colour[] colourMap) throws IOException {
    }

    @Override
    public void frameBufferUpdateRequest(RFBClient client, boolean incremental,
            int x, int y, int w, int h) throws IOException {
        // If you really really want automatic refreshes, comment out the following two lines.
        // BEWARE, it will send the entire screen, and probably be unusably slow. For now,
        // you must "request screen refresh" manually from your VNC viewer.
        //if (incremental)
        //    return;

        // Create image
        BufferedImage image = robot.createScreenCapture(new Rectangle(x, y, w,
                h));

        // Encode image
        Rect r = Rect.encode(client.getPreferredEncoding(),
                client.getPixelFormat(), image, x, y, w, h);

        // Write to client
        Rect[] rects = { r };
        try {
            client.writeFrameBufferUpdate(rects);
        } catch (IOException xx) {
            xx.printStackTrace();
        }
    }

    @Override
    public String getDesktopName(RFBClient client) {
        return displayName;
    }

    @Override
    public int getFrameBufferHeight(RFBClient client) {
        return device.getDefaultConfiguration().getBounds().height;
    }

    @Override
    public int getFrameBufferWidth(RFBClient client) {
        return device.getDefaultConfiguration().getBounds().width;
    }

    @Override
    public PixelFormat getPreferredPixelFormat(RFBClient client) {
        return PixelFormat.RGB888;
    }

    @Override
    public void keyEvent(RFBClient client, boolean down, int key)
            throws IOException {
        int[] vk = new int[2];
        keysym.toVKall(key, vk);
        if (vk[0] != KeyEvent.VK_UNDEFINED) {
            if (down)
                robot.keyPress(vk[0]);
            else
                robot.keyRelease(vk[0]);
        }
    }

    @Override
    public void pointerEvent(RFBClient client, int buttonMask, int x, int y)
            throws IOException {
        // Modifiers		
        int newMouseModifiers = 0;
        if ((buttonMask & rfb.Button1Mask) != 0)
            newMouseModifiers |= InputEvent.BUTTON1_MASK;
        if ((buttonMask & rfb.Button2Mask) != 0)
            newMouseModifiers |= InputEvent.BUTTON2_MASK;
        if ((buttonMask & rfb.Button3Mask) != 0)
            newMouseModifiers |= InputEvent.BUTTON3_MASK;

        if (newMouseModifiers != mouseModifiers) {
            // Change of button state
            if (mouseModifiers == 0) {
                robot.keyPress(newMouseModifiers);
            } else {
                robot.keyRelease(newMouseModifiers);
            }

            mouseModifiers = newMouseModifiers;
        }

        robot.mouseMove(x, y);
    }

    @Override
    public void removeClient(RFBClient client) {
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
        pixelFormat.setDirectColorModel((DirectColorModel) Toolkit
                .getDefaultToolkit().getColorModel());
    }

    @Override
    public void setShared(RFBClient client, boolean shared) throws IOException {
    }
}
