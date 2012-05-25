package ubicomp.rfb.server;

import gnu.rfb.server.RFBClient;

import java.io.IOException;

/**
 * To be implemented by RFB authentication models.
 **/

public interface RFBAuthenticator {

    // Operations

    public boolean authenticate(RFBClient client) throws IOException;
    
    // RFB v3.8
    
    public void setVersion(boolean v38);
}
