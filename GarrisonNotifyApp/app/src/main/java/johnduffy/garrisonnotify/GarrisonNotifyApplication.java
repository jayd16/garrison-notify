package johnduffy.garrisonnotify;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.File;

/**
 * Created by Jay on 1/17/2015.
 */
public class GarrisonNotifyApplication extends Application {
    public static ImageLoader VOLLEY_IMAGE_LOADER;
    public static RequestQueue VOLLEY_REQUEST_QUEUE;

    @Override
    public void onCreate() {
        super.onCreate();
        VOLLEY_REQUEST_QUEUE = Volley.newRequestQueue(this);
        VOLLEY_IMAGE_LOADER = new ImageLoader(Volley.newRequestQueue(this), new SimpleImageCache());
    }

    private static class SimpleImageCache implements ImageLoader.ImageCache {
        private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(16000000) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

        @Override
        public Bitmap getBitmap(String s) {
            return mCache.get(s);
        }

        @Override
        public void putBitmap(String s, Bitmap bitmap) {
            mCache.put(s, bitmap);
        }
    }

}
