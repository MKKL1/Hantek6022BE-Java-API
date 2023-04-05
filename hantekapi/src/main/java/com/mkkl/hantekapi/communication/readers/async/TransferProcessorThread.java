package com.mkkl.hantekapi.communication.readers.async;

import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import org.usb4java.Transfer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class TransferProcessorThread extends Thread {
    private volatile boolean abort = false;
    private volatile boolean finish = false;
    private final Endpoint endpoint;
    private final BlockingQueue<Transfer> transferBlockingQueue;
    private final int outstandingTransfers;
    private final AtomicInteger transfersInKernel = new AtomicInteger(0);

    public TransferProcessorThread(Endpoint endpoint, BlockingQueue<Transfer> transferBlockingQueue, int outstandingTransfers) {
        super("Transfer Processor Thread");
        this.endpoint = endpoint;
        this.transferBlockingQueue = transferBlockingQueue;
        this.outstandingTransfers = outstandingTransfers;
    }

    public synchronized void notifyReceivedPacket() {
        transfersInKernel.decrementAndGet();
        notifyAll();
    }

    /**
     * Aborts the event handling thread.
     */
    public void abort() {
        this.abort = true;
    }

    public void finish() {
        if (transferBlockingQueue.isEmpty()) interrupt();
        this.finish = true;
    }

    @Override
    public void run() {
        while (!isInterrupted() && !abort) {
            try {
                //Waiting for transfers in kernel to be smaller than required value
                while (transfersInKernel.get() >= outstandingTransfers) {
                    synchronized (this) {
                        wait();
                    }
                }

                if (finish && transferBlockingQueue.isEmpty()) {
                    while (transfersInKernel.get() > 0) {
                        synchronized (this) {
                            wait();
                        }
                    }
                    interrupt();
                }

                Transfer transfer = transferBlockingQueue.take();
                //Submitting transfer
                endpoint.asyncReadPipe(transfer);

                //Incrementing transfers in kernel local value
                transfersInKernel.incrementAndGet();
            } catch (InterruptedException e) {
                interrupt();
            }

        }
    }
}
