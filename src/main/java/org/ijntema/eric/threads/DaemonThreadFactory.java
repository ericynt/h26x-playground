package org.ijntema.eric.threads;

import java.util.concurrent.ThreadFactory;

// Thread that closes when the application shuts down
public class DaemonThreadFactory implements ThreadFactory {

    public Thread newThread(Runnable r) {

        Thread thread = new Thread(r);
        thread.setDaemon(true);

        return thread;
    }
}
