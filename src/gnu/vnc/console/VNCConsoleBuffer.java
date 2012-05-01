package gnu.vnc.console;

import gnu.rfb.Raw;
import gnu.rfb.Rect;
import gnu.rfb.RectFont;
import gnu.rfb.server.RFBClient;
import gnu.rfb.server.RFBClients;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Manages console buffer for {@link gnu.vnc.console.VNCConsole} supporting
 * multiple clients.
 **/

class VNCConsoleBuffer {

    //
    // Construction
    //

    private class ConsoleInputStream extends InputStream {

        @Override
        public int available() throws IOException {
            synchronized (inputLock) {
                return inputBuffer.size();
            }
        }

        @Override
        public int read() throws IOException {
            synchronized (inputLock) {
                if (inputBuffer.size() == 0) {
                    home.x = cursor.x;
                    home.y = cursor.y;
                    inputIndex = 0;

                    try {
                        inputLock.wait();
                    } catch (InterruptedException x) {
                        x.printStackTrace();
                        return -1;
                    }
                }

                Integer c = inputBuffer.elementAt(0);
                inputBuffer.removeElementAt(0);
                return c.intValue();
            }
        }
    }

    //
    // Attributes
    //

    private class ConsoleOutputStream extends OutputStream {

        @Override
        public void close() throws IOException {
            super.close();
        }

        @Override
        public void write(int b) throws IOException {
            output((char) b);
        }
    }

    private RFBClients clients;

    private int columns;

    //
    // Operations
    //

    private int rows;

    private Dimension charSize;

    private Point cursor = new Point();

    private Point home = new Point();

    private char[] outputBuffer;

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    private boolean[] outputMaskWork;
    private Vector<Integer> inputBuffer = new Vector<Integer>();
    private int inputIndex = 0;
    private Object outputLock = new Object();

    private Object inputLock = new Object();

    public VNCConsoleBuffer(int columns, int rows, Dimension charSize,
            RFBClients clients) {
        this.columns = columns;
        this.rows = rows;
        this.charSize = charSize;
        this.clients = clients;

        outputBuffer = new char[columns * rows];
        outputMaskWork = new boolean[columns * rows];
    }

    public RectFont getFont(RFBClient client) {
        RectFont font = (RectFont) clients.getProperty(client, "font");
        if (font == null) {
            font = new RectFont(charSize, client.getPixelFormat(),
                    client.getPreferredEncoding());
            clients.setProperty(client, "font", font);
        }

        return font;
    }

    private boolean[] getOutputMask(RFBClient client) {
        boolean[] outputMask = (boolean[]) clients.getProperty(client, "mask");
        if (outputMask == null) {
            outputMask = new boolean[columns * rows];
            clients.setProperty(client, "mask", outputMask);
        }

        return outputMask;
    }

    public Rect[] getRects(RFBClient client, boolean incremental) {
        RectFont font = getFont(client);

        synchronized (outputLock) {
            boolean[] outputMask = getOutputMask(client);

            if (!incremental) {
                for (int i = 0; i < outputMask.length; i++)
                    outputMask[i] = false;
            }

            Rect[] rects = font.getRects(outputBuffer, outputMask, 0,
                    outputBuffer.length, 0, 0, font.getCharSize().width
                            * columns);

            if (rects.length == 0) {
                // Encode empty raw rect
                rects = new Rect[1];
                rects[0] = new Raw(0, 0, 0, 0, client.getPixelFormat(),
                        new byte[0]);
                //rects[0] = font.getRect( '_', cursor.x * charSize.width, cursor.y * charSize.height );
            }

            return rects;
        }
    }

    public void input(int c) {
        output(c);

        synchronized (inputLock) {
            if (inputIndex < inputBuffer.size())
                inputBuffer.removeElementAt(inputIndex);
            inputBuffer.insertElementAt(new Integer(c), inputIndex++);
        }
    }

    public InputStream inputStream() {
        return new ConsoleInputStream();
    }

    public void inputVK(int vk) {
        synchronized (outputLock) {
            int p = columns * cursor.y + cursor.x;
            switch (vk) {
            case KeyEvent.VK_ENTER:
                cursor.x = 0;
                if (++cursor.y == rows) {
                    scrollDown();
                    cursor.y = rows - 1;
                }
                break;

            case KeyEvent.VK_BACK_SPACE:
                if (!cursor.equals(home)) {
                    if (--p != -1) {
                        outputBuffer[p] = '\0';
                        invalidateOutputMasks(p);
                    }

                    if (--cursor.x == -1) {
                        cursor.x = columns - 1;
                        if (--cursor.y == -1) {
                            cursor.x = 0;
                            cursor.y = 0;
                        }
                    }
                }
                break;

            case KeyEvent.VK_HOME:
                cursor.x = home.x;
                cursor.y = home.y;
                break;

            case KeyEvent.VK_LEFT:
                if (!cursor.equals(home)) {
                    if (--cursor.x == -1) {
                        cursor.x = columns - 1;
                        if (--cursor.y == -1) {
                            cursor.x = 0;
                            cursor.y = 0;
                        }
                    }
                }
                break;

            case KeyEvent.VK_RIGHT:
                if (++cursor.x == columns) {
                    cursor.x = 0;
                    if (++cursor.y == rows) {
                        scrollDown();
                        cursor.y = rows - 1;
                    }
                }
                break;
            }
        }

        synchronized (inputLock) {
            switch (vk) {
            case KeyEvent.VK_ENTER:
                inputBuffer.addElement(new Integer('\n'));
                inputBuffer.addElement(new Integer(-1));
                inputLock.notifyAll();
                inputIndex = 0;
                break;

            case KeyEvent.VK_BACK_SPACE:
                if (inputIndex > 0)
                    inputBuffer.removeElementAt(--inputIndex);
                break;

            case KeyEvent.VK_HOME:
                inputIndex = 0;
                break;

            case KeyEvent.VK_LEFT:
                if (inputIndex > 0)
                    inputIndex--;
                break;

            case KeyEvent.VK_RIGHT:
                if (inputIndex < inputBuffer.size())
                    inputIndex++;
                else
                    inputBuffer.insertElementAt(new Integer(' '), inputIndex++);
                break;
            }
        }
    }

    private void invalidateOutputMasks(int p) {
        boolean[] outputMask;
        for (Enumeration<RFBClient> e = clients.elements(); e.hasMoreElements();) {
            outputMask = getOutputMask(e.nextElement());
            outputMask[p] = false;
        }
    }

    public void output(int c) {
        synchronized (outputLock) {
            int p = columns * cursor.y + cursor.x;
            switch (c) {
            case '\r':
                break;

            case '\n':
                cursor.x = 0;
                if (++cursor.y == rows) {
                    scrollDown();
                    cursor.y = rows - 1;
                }
                break;

            case '\t':
                break;

            default:
                outputBuffer[p] = (char) c;
                invalidateOutputMasks(p);
                if (++cursor.x == columns) {
                    cursor.x = 0;
                    if (++cursor.y == rows) {
                        scrollDown();
                        cursor.y = rows - 1;
                    }
                }
                break;
            }
        }
    }

    public PrintStream printStream() {
        return new PrintStream(new ConsoleOutputStream());
    }

    public void scrollDown() {
        synchronized (outputLock) {
            int i = 0;
            for (; i < columns * (rows - 1); i++) {
                if (outputBuffer[i] != outputBuffer[i + columns]) {
                    outputBuffer[i] = outputBuffer[i + columns];
                    outputMaskWork[i] = false;
                } else {
                    outputMaskWork[i] = true;
                }
            }
            for (; i < columns * rows; i++) {
                if (outputBuffer[i] != ' ') {
                    outputBuffer[i] = ' ';
                    outputMaskWork[i] = false;
                } else {
                    outputMaskWork[i] = true;
                }
            }

            updateOutputMasks();
        }
    }

    private void updateOutputMasks() {
        boolean[] outputMask;
        int i;
        for (Enumeration<RFBClient> e = clients.elements(); e.hasMoreElements();) {
            outputMask = getOutputMask(e.nextElement());
            for (i = 0; i < columns * rows; i++)
                if (!outputMaskWork[i])
                    outputMask[i] = false;
        }
    }
}
