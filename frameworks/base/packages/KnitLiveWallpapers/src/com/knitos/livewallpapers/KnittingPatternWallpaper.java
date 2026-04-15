package com.knitos.livewallpapers;

import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;

/**
 * Knitting Pattern Live Wallpaper
 * Visualizes dynamic knitting patterns that respond to system state
 */
public class KnittingPatternWallpaper extends WallpaperService {
    
    private static final String TAG = "KnitOS_KnittingWP";
    
    @Override
    public Engine onCreateEngine() {
        return new KnittingEngine();
    }
    
    class KnittingEngine extends Engine {
        private final Handler mHandler = new Handler();
        private final Paint mPaint = new Paint();
        private boolean mVisible = false;
        private float mTime = 0;
        
        private static final int FRAME_DELAY = 16; // ~60 FPS
        
        private final Runnable mDrawRunnable = new Runnable() {
            @Override
            public void run() {
                if (mVisible) {
                    drawFrame();
                    mHandler.postDelayed(this, FRAME_DELAY);
                }
            }
        };
        
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            Log.d(TAG, "KnittingPatternWallpaper created");
        }
        
        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                mHandler.post(mDrawRunnable);
            } else {
                mHandler.removeCallbacks(mDrawRunnable);
            }
        }
        
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, 
                                     int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            Log.d(TAG, "Surface changed: " + width + "x" + height);
        }
        
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mDrawRunnable);
        }
        
        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    // Clear background
                    canvas.drawColor(Color.parseColor("#1a1a2e"));
                    
                    // Draw dynamic knitting pattern
                    drawKnittingPattern(canvas);
                    
                    // Update time for animation
                    mTime += 0.016f;
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
        
        private void drawKnittingPattern(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            
            mPaint.setStrokeWidth(2);
            mPaint.setStyle(Paint.Style.STROKE);
            
            // Draw interwoven threads
            for (int i = 0; i < 20; i++) {
                float phase = mTime + i * 0.3f;
                int color = Color.HSVToColor(new float[]{
                    (phase * 50) % 360, 0.7f, 0.8f
                });
                mPaint.setColor(color);
                
                // Draw wavy thread pattern
                for (int x = 0; x < width; x += 10) {
                    float y = height / 2 + 
                              (float)Math.sin((x + phase * 100) * 0.02) * 100 +
                              (float)Math.cos(i * 0.5) * 50;
                    
                    if (x == 0) {
                        canvas.drawPoint(x, y, mPaint);
                    } else {
                        float prevY = height / 2 + 
                                     (float)Math.sin((x - 10 + phase * 100) * 0.02) * 100 +
                                     (float)Math.cos(i * 0.5) * 50;
                        canvas.drawLine(x - 10, prevY, x, y, mPaint);
                    }
                }
            }
        }
    }
}
