package com.uninet.wirelessstore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncLock {
	
	private CountDownLatch mCacheSingal = new CountDownLatch(1);
	
	SyncLock(){
		
	}
	
    void waitForReady() {
        waitForLatch(mCacheSingal);
    }

    private static void waitForLatch(CountDownLatch latch) {
        if (latch == null) {
            return;
        }

        for (;;) {
            try {
                if(latch.await(5000, TimeUnit.MILLISECONDS)) {
                    return;
                } else {
//                    Log.d(TAG, "Thread " + Thread.currentThread().getName() + " still waiting for Cache Thread ready...");
                }
            } catch (InterruptedException e) {
//            	Log.d(TAG, "Interrupt while waiting for MountService to be ready.");
            }
        }
    }
    
    void setSync(){
    	mCacheSingal.countDown();
    }
}