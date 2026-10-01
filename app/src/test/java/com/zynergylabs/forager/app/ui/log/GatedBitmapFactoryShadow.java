package com.zynergylabs.forager.app.ui.log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Replaces {@code BitmapFactory.decodeFile} for the test classes that register it, so a test can
 * hold {@code DecodedPhoto}'s decode until it chooses. See {@code GatedDecode}. In Java, not
 * Kotlin, because Robolectric needs a real static method on the shadow class itself.
 */
@Implements(BitmapFactory.class)
public class GatedBitmapFactoryShadow {
    @Implementation
    public static Bitmap decodeFile(String pathName, BitmapFactory.Options opts) {
        try {
            if (GatedDecode.INSTANCE.getHold() != null) {
                GatedDecode.INSTANCE.getHold().await(GatedDecode.WAIT_SECONDS, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bitmap bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
        if (GatedDecode.INSTANCE.getFinished() != null) {
            GatedDecode.INSTANCE.getFinished().countDown();
        }
        return bitmap;
    }
}
