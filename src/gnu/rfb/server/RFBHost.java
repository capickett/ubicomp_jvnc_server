package gnu.rfb.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

import ubicomp.rfb.server.RFBAuthenticator;

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
    private Thread mThread;
    private Set<RFBSocket> mSockets;
    private boolean mRunning;
    private ServerSocket mCurrentSocket;

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

        mSockets = new HashSet<RFBSocket>();

        // Start listener thread
        mThread = new Thread(this, "RFBHost-" + display);
        mThread.start();
    }

    public synchronized RFBServer getSharedServer() {
        return sharedServer;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(5900 + display);
            mCurrentSocket = serverSocket;
            mRunning = true;
            while (mRunning) {
                // Create client for each connected socket
                //new RFBSocket( serverSocket.accept(), (RFBServer) constructor.newInstance( new Object[] { new Integer( display ), displayName } ) );
                mSockets.add(new RFBSocket(serverSocket.accept(), constructor,
                        new Object[] { new Integer(display), displayName,
                                new Integer(width), new Integer(height) },
                        this, authenticator));
            }
        } catch (IOException x) {
        }
    }

    public synchronized void setSharedServer(RFBServer sharedServer) {
        this.sharedServer = sharedServer;
    }

    public synchronized void stop() {
        for (RFBSocket socket : mSockets) {
            socket.stop();
        }
        try {
            mCurrentSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRunning = false;
    }
}
