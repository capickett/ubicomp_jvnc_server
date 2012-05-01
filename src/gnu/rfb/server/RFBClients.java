package gnu.rfb.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Manages a group of RFB clients with individual properties.
 **/

public class RFBClients {

    //
    // Construction
    //

    Hashtable<RFBClient, Hashtable> clients = new Hashtable<RFBClient, Hashtable>();

    //
    // Operations
    //

    public RFBClients() {
    }

    public void addClient(RFBClient client) {
        clients.put(client, new Hashtable());
    }

    public void closeAll() {
        RFBClient client;
        for (Enumeration<RFBClient> e = elements(); e.hasMoreElements();) {
            client = e.nextElement();
            try {
                client.close();
            } catch (IOException x) {
            }
        }

        clients.clear();
    }

    public Enumeration<RFBClient> elements() {
        return clients.keys();
    }

    public Object getProperty(RFBClient client, String key) {
        Hashtable properties = clients.get(client);
        if (properties == null)
            return null;

        return properties.get(key);
    }

    public boolean isEmpty() {
        return clients.isEmpty();
    }

    public void removeClient(RFBClient client) {
        clients.remove(client);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    public void setProperty(RFBClient client, String key, Object value) {
        Hashtable<String, Object> properties = clients.get(client);
        if (properties == null)
            return;

        properties.put(key, value);
    }
}
