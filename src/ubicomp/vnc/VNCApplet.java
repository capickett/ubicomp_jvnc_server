/**
 * 
 */
package ubicomp.vnc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author patricklarson, Cameron Pickett
 * 
 */
public class VNCApplet extends JApplet {

    private VNCHost mVNCHost = null;

    // Called when this applet is loaded into the browser.
    public void init() {
        final String serverIP = getParameter("serverIP");
        final int displayPort = Integer.parseInt(getParameter("displayPort"));
        final String displayName = getParameter("displayName");
        final int width = Integer.parseInt(getParameter("displayWidth"));
        final int height = Integer.parseInt(getParameter("displayHeight"));
        final boolean autostart = Boolean.parseBoolean(getParameter("autostart"));

        // Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    final JButton button = new JButton("Start");
                    if (autostart) {
                        mVNCHost = new VNCHost(serverIP, displayName,
                                displayPort, height, width);
                        mVNCHost.start();
                        button.setText("Stop");
                    }
                    button.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            if (mVNCHost == null) {
                                mVNCHost = new VNCHost(serverIP, displayName,
                                        displayPort, height, width);
                                mVNCHost.start();
                                button.setText("Stop");
                            } else {
                                mVNCHost.stop();
                                mVNCHost = null;
                                button.setText("Start");
                            }
                        }
                    });
                    getContentPane().add(button);
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully");
        }
    }

    private static final long serialVersionUID = 2612737252933759945L;

}