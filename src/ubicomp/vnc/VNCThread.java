package ubicomp.vnc;

import gnu.vnc.VNCHost;

public class VNCThread implements Runnable {

    private String[] mArgs;
    
    public VNCThread(String[] args) {
        mArgs = args;
    }
    
    @Override
    public void run() {
        VNCHost.main(mArgs);
    }

}