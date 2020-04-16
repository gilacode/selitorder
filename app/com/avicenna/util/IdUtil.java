package com.avicenna.util;

import com.google.inject.Singleton;
import play.Logger;

import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class IdUtil {

    private static int count = 1;

    private final AtomicLong uniqueId = new AtomicLong(0);

    public IdUtil() {
        Logger.debug(this.getClass().getSimpleName() + " instantiated "+count+" time(s)");
        count++;
    }

    public synchronized String getShortUniqueId() {
        //generateSingle shorter id
        long now;
        long prev;
        do {
            prev = uniqueId.get();
            now = System.currentTimeMillis();
            // make sure now is moving ahead and unique
            if (now <= prev) {
                now = prev + 1;
            }
            // loop if someone else has updated the id
        } while (!uniqueId.compareAndSet(prev, now));

        // shuffle it
        long result = shuffleBits(now);
        return Long.toHexString(result);
    }

    private long shuffleBits(long val) {
        long result = 0;
        result |= (val & 0xFF00000000000000L) >> 56;
        result |= (val & 0x00FF000000000000L) >> 40;
        result |= (val & 0x0000FF0000000000L) >> 24;
        result |= (val & 0x000000FF00000000L) >> 8;
        result |= (val & 0x00000000FF000000L) << 8;
        result |= (val & 0x0000000000FF0000L) << 24;
        result |= (val & 0x000000000000FF00L) << 40;
        result |= (val & 0x00000000000000FFL) << 56;
        return result;
    }
}
