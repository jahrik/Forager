package com.zynergylabs.forager.app.ui.log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Records the thread each {@code BitmapFactory.decodeFile} call runs on, for
 * {@code PhotoDecodeThreadTest}. In Java for the same reason as {@code GatedBitmapFactoryShadow}.
 */
@Implements(BitmapFactory.class)
public class ThreadRecordingBitmapFactoryShadow {
    public static final CopyOnWriteArrayList<Thread> DECODE_THREADS = new CopyOnWriteArrayList<>();

    @Implementation
    public static Bitmap decodeFile(String pathName, BitmapFactory.Options opts) {
        DECODE_THREADS.add(Thread.currentThread());
        return Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
    }
}
