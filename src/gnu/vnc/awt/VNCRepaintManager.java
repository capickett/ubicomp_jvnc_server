package gnu.vnc.awt;

import gnu.awt.PixelsOwner;
import gnu.rfb.server.RFBClient;
import gnu.rfb.server.RFBClients;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

/**
 * A {@link javax.swing.RepaintManager JFC repaint manager} that writes to
 * {@link gnu.vnc.VNCQueue VNC queues}. Internally creates a
 * {@link gnu.vnc.awt.VNCOwnerRepaintManager} for each managed
 * {@link gnu.awt.PixelsOwner}..
 **/

class VNCRepaintManager extends RepaintManager {

    //
    // Static operations
    //

    private static VNCRepaintManager repaintManager = null;

    //
    // Operations
    //

    public static VNCRepaintManager currentManager() {
        if (repaintManager == null) {
            repaintManager = new VNCRepaintManager();
            RepaintManager.setCurrentManager(repaintManager);
        }
        return repaintManager;
    }

    private static PixelsOwner getPixelsOwner(Component component) {
        if (component instanceof PixelsOwner) {
            return (PixelsOwner) component;
        } else {
            Component parent = component.getParent();
            if (parent == null)
                return null;
            return getPixelsOwner(parent);
        }
    }

    private Map<PixelsOwner, VNCOwnerRepaintManager> managers = Collections
            .synchronizedMap(new HashMap<PixelsOwner, VNCOwnerRepaintManager>());

    //
    // RepaintManager
    //

    private VNCRepaintManager() {
        super();
        setDoubleBufferingEnabled(false);
    }

    @Override
    public void addDirtyRegion(JComponent component, int x, int y, int w, int h) {
        // Find owner
        PixelsOwner pixelsOwner = getPixelsOwner(component);
        VNCOwnerRepaintManager manager = managers.get(pixelsOwner);
        if (manager == null)
            return;

        manager.addDirtyRegion(component, x, y, w, h);
    }

    @Override
    public void addInvalidComponent(JComponent component) {
        // Find owner
        PixelsOwner pixelsOwner = getPixelsOwner(component);
        VNCOwnerRepaintManager manager = managers.get(pixelsOwner);
        if (manager == null)
            return;

        manager.addInvalidComponent(component);
    }

    public void frameBufferUpdate(PixelsOwner pixelsOwner, RFBClient client,
            boolean incremental, int x, int y, int w, int h) throws IOException {
        VNCOwnerRepaintManager manager = managers.get(pixelsOwner);
        manager.frameBufferUpdate(client, incremental, x, y, w, h, pixelsOwner);
    }

    @Override
    public java.awt.Rectangle getDirtyRegion(JComponent component) {
        // Find owner
        PixelsOwner pixelsOwner = getPixelsOwner(component);
        VNCOwnerRepaintManager manager = managers.get(pixelsOwner);
        if (manager == null)
            return null;

        return manager.getDirtyRegion(component);
    }

    public void manage(PixelsOwner pixelsOwner, RFBClients clients) {
        managers.put(pixelsOwner, new VNCOwnerRepaintManager(pixelsOwner,
                clients));
    }

    @Override
    public void markCompletelyClean(JComponent component) {
        // Find owner
        PixelsOwner pixelsOwner = getPixelsOwner(component);
        VNCOwnerRepaintManager manager = managers.get(pixelsOwner);
        if (manager == null)
            return;

        manager.markCompletelyClean(component);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    @Override
    public void paintDirtyRegions() {
        // This is usually called from a thread in SystemEventQueueUtilities,
        // but we want to handle this ourselves
    }

    @Override
    public void removeInvalidComponent(JComponent component) {
        // Find owner
        PixelsOwner pixelsOwner = getPixelsOwner(component);
        VNCOwnerRepaintManager manager = managers.get(pixelsOwner);
        if (manager == null)
            return;

        manager.removeInvalidComponent(component);
    }

    //
    // Construction
    //

    public void unmanage(PixelsOwner pixelsOwner) {
        managers.remove(pixelsOwner);
    }

    @Override
    public void validateInvalidComponents() {
        // This is usually called from a thread in SystemEventQueueUtilities,
        // but we want to handle this ourselves
    }
}
