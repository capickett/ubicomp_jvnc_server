package ubicomp.vnc;

import gnu.rfb.server.DefaultRFBAuthenticator;
import gnu.rfb.server.RFBHost;
import gnu.vnc.WebServer;
import gnu.vnc.awt.VNCRobot;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Main method creates a group of {@link gnu.rfb.server.RFBServer RFB servers}
 * and {@link gnu.vnc.WebServer VNC-viewer-applet-serving web servers} according
 * to information provided in the 'vnc.properties' file.
 * 
 * Edited by Cameron Pickett
 **/

public class VNCHost implements Runnable {

    private static final String sServerClassName = "gnu.vnc.awt.VNCRobot";
    private final String sServerIp;
    private final String sDisplayName;
    private final int sDisplayPort;
    private final int sWidth;
    private final int sHeight;
    
    public VNCHost(String serverIp, String displayName, int displayPort, int height, int width) {
        sServerIp = serverIp;
        sDisplayName = displayName;
        sDisplayPort = displayPort;
        sHeight = height;
        sWidth = width;
    }

    public void run() {
        // Create RFB hosts and web servers
        int display = sDisplayPort;
        String serverClassName = sServerClassName;
        String displayName = sDisplayName;
        int height = sHeight;
        int width = sWidth;
        String password = null;
        String restrictedTo = sServerIp;
        String noPasswordFor = sServerIp;

        System.out.println(displayName);
        Class<VNCRobot> serverClass = VNCRobot.class;
        // RFB host
        try {
            new RFBHost(display, displayName, serverClass, width, height, 
                    new DefaultRFBAuthenticator(password, restrictedTo, noPasswordFor));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        // Webserver
        new WebServer(display, displayName, width, height);

        System.out.println("  VNC display " + display);
        System.out.println("  Web server on port " + (5800 + display));
        System.out.println("  Class: " + serverClassName);
    }
}
