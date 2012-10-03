package com.theriddlebrothers.winded;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.theriddlebrothers.winded.Instrument.Keys;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	protected final String TAG = "WindedMainActivity";
	private Instrument instrument;
    private DemoView view;
    private SoundMeter meter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new DemoView(this);

        setContentView(R.layout.activity_main);

        instrument = new Instrument((AudioManager)getSystemService(Context.AUDIO_SERVICE), MainActivity.this);

        // Initialize sound meter
        meter = new SoundMeter();
        meter.start();
        new Timer().scheduleAtFixedRate(new MonitorDecibelsTask(), 100, 100);
    }
    // Monitor external sound to determine breath level
    private class MonitorDecibelsTask extends TimerTask {
        public void run() {
            double amplitude = meter.getAmplitude();
            //Log.d(TAG, "Breath: " + Double.toString(amplitude));
            instrument.play((float)amplitude);
        }
    }

    private class DemoView extends View implements OnTouchListener {

        private ArrayList<Key> keys;
        private Drawable drawableArea;

        public DemoView(Context context){
            super(context);

            keys = new ArrayList<Key>();
            keys.add(new Key(200, 80));
            keys.add(new Key(200, 200));
            keys.add(new Key(200, 320));
            keys.add(new Key(200, 440));

        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Log.d(TAG, "You click at x = " + event.getX() + " and y = " + event.getY());
            float x = event.getX();
            float y = event.getY();
            CheckCollision(x, y);
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Log.d(TAG, "You click at x = " + event.getX() + " and y = " + event.getY());
            float x = event.getX();
            float y = event.getY();
            CheckCollision(x, y);
            return false;
        }

        public void CheckCollision(float x, float y) {
            for(int i = 0; i < keys.size(); i++) {
                if (keys.get(i).IsTouching(x, y)) {
                    Log.d(TAG, "You are touching key " + i);
                }
            }
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for(int i = 0; i < keys.size(); i++) {
                this.keys.get(i).Draw(canvas);
            }
        }
    }

    private class Key {
        private int x;
        private int y;
        private int width;
        private int height;
        private Rect rect;

        public Key(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 100;
            this.height = 100;
            rect = new Rect(this.x, this.y, this.x + this.width, this.y + this.height);
        }

        public boolean IsTouching(float x, float y) {
            if (this.x < x
                    && x < (this.x + this.width)
                    && this.y < y
                    && y < (this.y + this.height)) {
                return true;
            }
            return false;
        }

        public void Draw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);
            canvas.drawRect(rect, paint);
        }
    }
}
