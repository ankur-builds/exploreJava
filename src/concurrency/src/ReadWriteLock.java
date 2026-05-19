public class ReadWriteLock {

    private int activeReaders = 0;

    private int activeWriters = 0;

    private int waitingWriters = 0;

    public synchronized void lockRead()
            throws InterruptedException {

        while(activeWriters > 0
                || waitingWriters > 0) {

            wait();
        }

        activeReaders++;
    }

    public synchronized void unlockRead() {

        activeReaders--;

        notifyAll();
    }

    public synchronized void lockWrite()
            throws InterruptedException {

        waitingWriters++;

        try {

            while(activeReaders > 0
                    || activeWriters > 0) {

                wait();
            }

            activeWriters++;

        } finally {

            waitingWriters--;
        }
    }

    public synchronized void unlockWrite() {

        activeWriters--;

        notifyAll();
    }
}
