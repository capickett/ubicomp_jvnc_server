package gnu.rfb.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;

/**
 * Waits on a standard VNC socket and creates an
 * {@link gnu.rfb.server.RFBServer RFBServer} implementation for each new,
 * authenticated client.
 **/

public class RFBHost implements Runnable {

    //
    // Construction
    //

    private int display;

    //
    // Operations
    //

    private String displayName;

    private int width;

    //
    // Runnable
    //

    private int height;

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    private RFBAuthenticator authenticator;
    private Constructor constructor;
    private RFBServer sharedServer = null;

    public RFBHost(int display, String displayName, Class rfbServerClass,
            int width, int height, RFBAuthenticator authenticator)
            throws NoSuchMethodException {
        // Get constructor
        constructor = rfbServerClass.getDeclaredConstructor(new Class[] {
                int.class, String.class, int.class, int.class });

        // Are we assignable to RFBServer
        if (!RFBServer.class.isAssignableFrom(rfbServerClass))
            throw new NoSuchMethodException("Class " + rfbServerClass
                    + " does not support RFBServer interface");

        this.display = display;
        this.displayName = displayName;
        this.width = width;
        this.height = height;
        this.authenticator = authenticator;

        // Start listener thread
        new Thread(this, "RFBHost-" + display).start();
    }

    public synchronized RFBServer getSharedServer() {
        return sharedServer;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(5900 + display);
            while (true) {
                // Create client for each connected socket
                //new RFBSocket( serverSocket.accept(), (RFBServer) constructor.newInstance( new Object[] { new Integer( display ), displayName } ) );
                new RFBSocket(serverSocket.accept(), constructor, new Object[] {
                        new Integer(display), displayName, new Integer(width),
                        new Integer(height) }, this, authenticator);
            }
        } catch (IOException x) {
        }
    }

    public synchronized void setSharedServer(RFBServer sharedServer) {
        this.sharedServer = sharedServer;
    }
}
