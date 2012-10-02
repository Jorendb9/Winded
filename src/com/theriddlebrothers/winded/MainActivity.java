package com.theriddlebrothers.winded;

import java.util.Timer;
import java.util.TimerTask;

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
	
	private SoundMeter meter;
	private final int DEFAULT_METER_THRESHOLD = 20;
	private double currentMeterThreshold;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize instrument
        instrument = new Instrument((AudioManager)getSystemService(Context.AUDIO_SERVICE), MainActivity.this);
        findViewById(R.id.cKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.C));
        findViewById(R.id.cSharpKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.C));
        findViewById(R.id.dKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.D));
        findViewById(R.id.dSharpKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.D));
        findViewById(R.id.eKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.E));
        findViewById(R.id.fKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.F));
        findViewById(R.id.fSharpKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.F));
        findViewById(R.id.gKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.G));
        findViewById(R.id.gSharpKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.G));
        findViewById(R.id.aKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.A));
        findViewById(R.id.aSharpKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.A));
        findViewById(R.id.bKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.B));
        //findViewById(R.id.octaveKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.Octave));
        findViewById(R.id.sharpKey).setOnTouchListener(new KeyTouchListener(Instrument.Keys.Sharp));
        
        // Hide/show sharp keys
        findViewById(R.id.sharpKey).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				LinearLayout sharpKeysLayout = (LinearLayout)findViewById(R.id.sharpKeysLayout);
				LinearLayout standardKeysLayout = (LinearLayout)findViewById(R.id.standardKeysLayout);
				
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					// Show sharp keys
					sharpKeysLayout.setVisibility(View.VISIBLE);
					standardKeysLayout.setVisibility(View.GONE);
					instrument.pressKey(Keys.Sharp);
	            } else if (event.getAction() == MotionEvent.ACTION_UP) {
	            	// Show standard keys
					sharpKeysLayout.setVisibility(View.GONE);
					standardKeysLayout.setVisibility(View.VISIBLE);
					instrument.releaseKey(Keys.Sharp);
	            }
				return false;
			}
        });

        // Initialize sound meter
        meter = new SoundMeter();
        meter.start();
        new Timer().scheduleAtFixedRate(new MonitorDecibelsTask(), 100, 100);
        
        // Add buttons
        /*String[] keys = { "C", "D", "E", "F", "G" };
        for(int i = 0; i < keys.length; i++) {
        	String key = keys[i];
            LinearLayout keyLayout = (LinearLayout) findViewById(R.id.keyLayout);
            
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			
			layoutParams.setMargins(10, 10, 10, 10);

            Button btn = new Button(this);
            btn.setBackgroundResource(R.color.button_background);
            btn.setHeight(100);
            btn.setText(key);
            keyLayout.addView(btn, layoutParams);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    // Monitor external sound to determine breath level
    private class MonitorDecibelsTask extends TimerTask {
	   public void run() {
		   double amplitude = meter.getAmplitude();
		   Log.d(TAG, "Breath: " + Double.toString(amplitude));
		   instrument.play((float)amplitude);
	   }
	}
    
	
	public class KeyTouchListener implements OnTouchListener
	{
		Instrument.Keys keyValue;
		
		public KeyTouchListener(Instrument.Keys keyValue) {
			this.keyValue = keyValue;
		}
			
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "Key pressed: " + keyValue);
                instrument.pressKey(keyValue);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d(TAG, "Key released: " + keyValue);
                instrument.releaseKey(keyValue);
            }
			return false;
		}
	
	};
}
