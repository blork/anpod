package com.blork.anpod.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

public class ImageViewTouch extends ImageViewTouchBase {
    private final Context mContext;
    private boolean mEnableTrackballScroll;

    public ImageViewTouch(Context context) {
        super(context);
        mContext = context;
    }

    public ImageViewTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setEnableTrackballScroll(boolean enable) {
        mEnableTrackballScroll = enable;
    }

    protected void postTranslateCenter(float dx, float dy) {
        super.postTranslate(dx, dy);
        center(true, true);
    }

    private static final float PAN_RATE = 20;

    // This is the time we allow the dpad to change the image position again.
    private long mNextChangePositionTime;

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (mViewImage.mPaused) return false;
//
//        // Don't respond to arrow keys if trackball scrolling is not enabled
//        if (!mEnableTrackballScroll) {
//            if ((keyCode >= KeyEvent.KEYCODE_DPAD_UP)
//                    && (keyCode <= KeyEvent.KEYCODE_DPAD_RIGHT)) {
//                return super.onKeyDown(keyCode, event);
//            }
//        }
//
//        int current = mViewImage.mCurrentPosition;
//
//        int nextImagePos = -2; // default no next image
//        try {
//            switch (keyCode) {
//                case KeyEvent.KEYCODE_DPAD_CENTER: {
//                    if (mViewImage.isPickIntent()) {
//                        IImage img = mViewImage.mAllImages
//                                .getImageAt(mViewImage.mCurrentPosition);
//                        mViewImage.setResult(ViewImage.RESULT_OK,
//                                 new Intent().setData(img.fullSizeImageUri()));
//                        mViewImage.finish();
//                    }
//                    break;
//                }
//                case KeyEvent.KEYCODE_DPAD_LEFT: {
//                    if (getScale() <= 1F && event.getEventTime()
//                            >= mNextChangePositionTime) {
//                        nextImagePos = current - 1;
//                        mNextChangePositionTime = event.getEventTime() + 500;
//                    } else {
//                        panBy(PAN_RATE, 0);
//                        center(true, false);
//                    }
//                    return true;
//                }
//                case KeyEvent.KEYCODE_DPAD_RIGHT: {
//                    if (getScale() <= 1F && event.getEventTime()
//                            >= mNextChangePositionTime) {
//                        nextImagePos = current + 1;
//                        mNextChangePositionTime = event.getEventTime() + 500;
//                    } else {
//                        panBy(-PAN_RATE, 0);
//                        center(true, false);
//                    }
//                    return true;
//                }
//                case KeyEvent.KEYCODE_DPAD_UP: {
//                    panBy(0, PAN_RATE);
//                    center(false, true);
//                    return true;
//                }
//                case KeyEvent.KEYCODE_DPAD_DOWN: {
//                    panBy(0, -PAN_RATE);
//                    center(false, true);
//                    return true;
//                }
//                case KeyEvent.KEYCODE_DEL:
//                    MenuHelper.deletePhoto(
//                            mViewImage, mViewImage.mDeletePhotoRunnable);
//                    break;
//            }
//        } finally {
//            if (nextImagePos >= 0
//                    && nextImagePos < mViewImage.mAllImages.getCount()) {
//                synchronized (mViewImage) {
//                    mViewImage.setMode(ViewImage.MODE_NORMAL);
//                    mViewImage.setImage(nextImagePos, true);
//                }
//           } else if (nextImagePos != -2) {
//               center(true, true);
//           }
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }
//}
}

// This is a cache for Bitmap displayed in ViewImage (normal mode, thumb only).
class BitmapCache implements ImageViewTouchBase.Recycler {
    public static class Entry {
        int mPos;
        Bitmap mBitmap;
        public Entry() {
            clear();
        }
        public void clear() {
            mPos = -1;
            mBitmap = null;
        }
    }

    private final Entry[] mCache;

    public BitmapCache(int size) {
        mCache = new Entry[size];
        for (int i = 0; i < mCache.length; i++) {
            mCache[i] = new Entry();
        }
    }

    // Given the position, find the associated entry. Returns null if there is
    // no such entry.
    private Entry findEntry(int pos) {
        for (Entry e : mCache) {
            if (pos == e.mPos) {
                return e;
            }
        }
        return null;
    }

    // Returns the thumb bitmap if we have it, otherwise return null.
    public synchronized Bitmap getBitmap(int pos) {
        Entry e = findEntry(pos);
        if (e != null) {
            return e.mBitmap;
        }
        return null;
    }

    public synchronized void put(int pos, Bitmap bitmap) {
        // First see if we already have this entry.
        if (findEntry(pos) != null) {
            return;
        }

        // Find the best entry we should replace.
        // See if there is any empty entry.
        // Otherwise assuming sequential access, kick out the entry with the
        // greatest distance.
        Entry best = null;
        int maxDist = -1;
        for (Entry e : mCache) {
            if (e.mPos == -1) {
                best = e;
                break;
            } else {
                int dist = Math.abs(pos - e.mPos);
                if (dist > maxDist) {
                    maxDist = dist;
                    best = e;
                }
            }
        }

        // Recycle the image being kicked out.
        // This only works because our current usage is sequential, so we
        // do not happen to recycle the image being displayed.
        if (best.mBitmap != null) {
            best.mBitmap.recycle();
        }

        best.mPos = pos;
        best.mBitmap = bitmap;
    }

    // Recycle all bitmaps in the cache and clear the cache.
    public synchronized void clear() {
        for (Entry e : mCache) {
            if (e.mBitmap != null) {
                e.mBitmap.recycle();
            }
            e.clear();
        }
    }

    // Returns whether the bitmap is in the cache.
    public synchronized boolean hasBitmap(int pos) {
        Entry e = findEntry(pos);
        return (e != null);
    }

    // Recycle the bitmap if it's not in the cache.
    // The input must be non-null.
    public synchronized void recycle(Bitmap b) {
        for (Entry e : mCache) {
            if (e.mPos != -1) {
                if (e.mBitmap == b) {
                    return;
                }
            }
        }
        b.recycle();
    }
}
