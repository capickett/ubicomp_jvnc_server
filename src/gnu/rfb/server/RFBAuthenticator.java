package gnu.rfb.server;

import java.io.IOException;

/**
 * To be implemented by RFB authentication models.
 **/

public interface RFBAuthenticator {

    // Operations

    public boolean authenticate(RFBClient client) throws IOException;
}
