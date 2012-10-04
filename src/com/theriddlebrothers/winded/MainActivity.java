package com.theriddlebrothers.winded;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.*;
import android.view.*;
import com.theriddlebrothers.winded.Instrument.Keys;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	protected final String TAG = "WindedMainActivity";
    private CanvasView canvasView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canvasView = new CanvasView(this);

        setContentView(R.layout.activity_main);
        LinearLayout canvas = (LinearLayout)findViewById(R.id.instrumentCanvas);
        canvas.addView(canvasView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int numPointers = event.getPointerCount();
        ArrayList<KeyPresenter> keysBeingPressed = new ArrayList<KeyPresenter>();
        Log.d(TAG, numPointers + " fingers being pressed.");
        for(int i = 0; i < numPointers; i++) {
            int pointerId = event.getPointerId(i);
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // @todo remove try/catch once i figure out what is wrong here
                    try {
                        float x = event.getX(pointerId);
                        float y = event.getY(pointerId);
                        keysBeingPressed.addAll(canvasView.CheckCollision(x, y));
                    } catch(Exception ex) {

                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    //canvasView.CheckCollision(0, 0);
                    break;
            }
        }

        // Now that we know all the keys that are being pressed, we
        // have to remove any that are no longer pressing.
        for(int i = 0; i < canvasView.keys.size(); i++) {
            KeyPresenter keyToRelease = canvasView.keys.get(i);
            if (!keysBeingPressed.contains(keyToRelease))         {
                KeyPresenter key = canvasView.keys.get(i);
                canvasView.ReleaseKey(key);
            }
        }

        canvasView.ReDraw();

        return true;
    }

    private class CanvasView extends View  {

        private GestureDetector gestureDetector;
        private ArrayList<KeyPresenter> keys;
        private Instrument instrument;
        private SoundMeter meter;

        public CanvasView(Context context){
            super(context);

            instrument = new Instrument((AudioManager)getSystemService(Context.AUDIO_SERVICE), MainActivity.this);

            WindowManager mWinMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Point outset = new Point();
            mWinMgr.getDefaultDisplay().getSize(outset);

            // Initialize sound meter
            meter = new SoundMeter();
            meter.start();
            new Timer().scheduleAtFixedRate(new MonitorDecibelsTask(), 0, 50);


            int numKeys = 7;
            // keys should span 40% of screen
            int keyWidth = (outset.x / 10) * 4;

            int keyMargin = 10;
            int totalMargin = (numKeys) * keyMargin;
            int keyHeight = (outset.y - totalMargin) / numKeys;
            int rightPos =  outset.x - keyWidth;

            // @todo refactor this crap
            keys = new ArrayList<KeyPresenter>();
            keys.add(new KeyPresenter(rightPos, 0, keyWidth, keyHeight, Instrument.Keys.B));
            keys.add(new KeyPresenter(rightPos, keyHeight + (keyMargin), keyWidth, keyHeight, Instrument.Keys.A));
            keys.add(new KeyPresenter(rightPos, keyHeight * 2 + (keyMargin) * 2, keyWidth, keyHeight, Instrument.Keys.G));
            keys.add(new KeyPresenter(rightPos, keyHeight * 3 + (keyMargin) * 3, keyWidth, keyHeight, Instrument.Keys.F));
            keys.add(new KeyPresenter(rightPos, keyHeight * 4 + (keyMargin) * 4, keyWidth, keyHeight, Instrument.Keys.E));
            keys.add(new KeyPresenter(rightPos, keyHeight * 5 + (keyMargin) * 5, keyWidth, keyHeight, Instrument.Keys.D));
            keys.add(new KeyPresenter(rightPos, keyHeight * 6 + (keyMargin) * 6, keyWidth, keyHeight, Instrument.Keys.C));
        }

        // Monitor external sound to determine breath level
        private class MonitorDecibelsTask extends TimerTask {
            public void run() {
                double amplitude = meter.getAmplitude();
                //Log.d(TAG, "Breath: " + Double.toString(amplitude));
                instrument.play((float)amplitude);
            }
        }

        public ArrayList<KeyPresenter> CheckCollision(float x, float y) {
            boolean redraw = false;
            ArrayList<KeyPresenter> keysBeingPressed = new ArrayList<KeyPresenter>();
            for(int i = 0; i < keys.size(); i++) {
                if (keys.get(i).IsTouching(x, y)) {
                    Log.d(TAG, "You are touching key " + i);
                    instrument.pressKey(keys.get(i).key);
                    keysBeingPressed.add(keys.get(i));
                    redraw = true;
                }
            }
            return keysBeingPressed;
        }

        public void ReDraw() {
            this.invalidate();
        }

        public void ReleaseKey(KeyPresenter key) {
            key.ReleaseKey();
            canvasView.instrument.releaseKey(key.key);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // Clear canvas
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            canvas.drawColor(Color.WHITE);

            for(int i = 0; i < keys.size(); i++) {
                this.keys.get(i).Draw(canvas);
            }
        }
    }

    /**
     * Class used to present an instrument's keys to the view.
     */
    private class KeyPresenter {
        private int x;
        private int y;
        private int width;
        private int height;
        private Rect rect;
        private boolean isTouching = false;
        private Keys key;

        public KeyPresenter(int x, int y, int width, int height, Keys key) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.key = key;
            rect = new Rect(this.x, this.y, this.x + this.width, this.y + this.height);
        }

        /**
         * Mark a key as not being touched.
         */
        public void ReleaseKey() {
            isTouching = false;
        }

        public boolean IsTouching(float x, float y) {
            isTouching = false;
            if (x >= this.x
                    && x <= (this.x + this.width)
                    && y >= this.y
                    && y <= (this.y + this.height)) {
                isTouching = true;
                Log.d(TAG, "x=" + this.x + ", pointerx=" + x + ", y=" + this.y + " pointery=" + y);
            }
            return isTouching;
        }

        public void Draw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            if (isTouching) paint.setColor(Color.BLUE);
            else paint.setColor(Color.RED);
            canvas.drawRect(rect, paint);
        }
    }
}
