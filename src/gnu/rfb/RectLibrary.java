package gnu.rfb;

import java.util.Hashtable;

/**
 * Manages a library of {@link gnu.rfb.Rect RFB rectangles}.
 **/

public class RectLibrary {

    //
    // Construction
    //

    private Hashtable<Integer, Rect> rects = new Hashtable<Integer, Rect>();

    //
    // Operations
    //

    private Rect defaultRect = null;

    public RectLibrary() {
    }

    public Rect getRect(int key) {
        Rect rect = rects.get(new Integer(key));
        if (rect == null)
            rect = defaultRect;
        return rect;
    }

    public Rect getRect(int key, int originX, int originY) {
        Rect rect = getRect(key);
        if (rect != null) {
            try {
                rect = (Rect) rect.clone();
            } catch (CloneNotSupportedException x) {
            }
            rect.transform(originX, originY);
        }
        return rect;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    public void putDefaultRect(Rect rect) {
        defaultRect = rect;
    }

    public void putRect(int key, Rect rect) {
        rects.put(new Integer(key), rect);
    }
}
