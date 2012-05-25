package gnu.rfb.server;

import gnu.rfb.rfb;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import ubicomp.rfb.server.RFBAuthenticator;

/**
 * Free-access RFB authentication models.
 * 
 * Modified by Cameron Pickett 2012
 **/

public class DefaultRFBAuthenticator implements RFBAuthenticator {

    //
    // Construction
    //

    private static void addInetAddresses(Set<InetAddress> set, String string) {
        if (string == null)
            return;

        InetAddress[] addresses;
        for (StringTokenizer t = new StringTokenizer(string, ","); t
                .hasMoreElements();) {
            try {
                addresses = InetAddress.getAllByName(t.nextToken());
                for (int i = 0; i < addresses.length; i++) {
                    set.add(addresses[i]);
                }
            } catch (UnknownHostException x) {
            }
        }
    }

    protected String password;

    //
    // RFBAuthenticator
    //

    private boolean restrict;
    
    private boolean v38;

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    private Set<InetAddress> restrictedTo = new HashSet<InetAddress>();
    private Set<InetAddress> noPasswordFor = new HashSet<InetAddress>();

    public DefaultRFBAuthenticator() {
        this(null, null, null, false);
    }

    public DefaultRFBAuthenticator(String password, String restrictedTo,
            String noPasswordFor) {
        this(password, restrictedTo, noPasswordFor, false);
    }
    
    public DefaultRFBAuthenticator(String password, String restrictedTo,
            String noPasswordFor, boolean v38) {
        restrict = (restrictedTo != null && restrictedTo.length() > 0);
        addInetAddresses(this.restrictedTo, restrictedTo);
        addInetAddresses(this.noPasswordFor, noPasswordFor);
        this.password = password;
        this.v38 = v38;
    }

    @Override
    public boolean authenticate(RFBClient client) throws IOException {
        if (isRestricted(client)) {
            client.writeConnectionFailed("Your address is blocked");
            return false;
        }
        if (password != null && password.length() > 0
                && isChallengeRequired(client)) {
            return challenge(client);
        } else {
            noChallenge(client);
            return true;
        }
    }

    private boolean challenge(RFBClient client) throws IOException {
        if (v38) {
            // Server requires authorization
            client.writeByte(1);
            client.writeByte(rfb.VncAuth);
            
            // Discard client reply
            byte[] response = new byte[1];
            client.read(response);
        } else {
            client.writeInt(rfb.VncAuth);
        }
        
        // Write 16 byte challenge
        byte[] challenge = new byte[16];
        client.writeBytes(challenge);
        client.flush();

        // Read 16 byte response
        byte[] response = new byte[16];
        client.read(response);

        // Create key (password padded with zeros)
        byte[] key = new byte[8];
        int i;
        for (i = 0; i < password.length(); i++) {
            key[i] = (byte) password.charAt(i);
        }
        for (; i < 8; i++) {
            key[i] = 0;
        }
        DesCipher des = new DesCipher(key);

        // Cipher challenge
        des.encrypt(challenge, 0, challenge, 0);
        des.encrypt(challenge, 8, challenge, 8);

        // Compare ciphers
        if (Arrays.equals(challenge, response)) {
            client.writeSecurityResult(true, "");
            return true;
        } else {
            client.writeSecurityResult(false, "Password Incorrect!");
            return false;
        }
    }

    protected boolean isChallengeRequired(RFBClient client) {
        return !noPasswordFor.contains(client.getInetAddress());
    }

    protected boolean isRestricted(RFBClient client) {
        if (restrict)
            return !restrictedTo.contains(client.getInetAddress());
        else
            return false;
    }
    
    private void noChallenge(RFBClient client) throws IOException {
        if(v38) {
            client.writeByte(1);
            client.writeByte(rfb.NoAuth);
            client.flush();
            
            byte[] response = new byte[1];
            client.read(response);
            
            client.writeInt(rfb.VncAuthOK);
            client.flush();
        } else {
            client.writeInt(rfb.NoAuth);
            client.flush();
        }
    }

    @Override
    public void setVersion(boolean v38) {
        this.v38 = v38;       
    }    
}
